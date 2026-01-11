package io.github.sasori_256.town_planning.entity.model;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.DoubleConsumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import io.github.sasori_256.town_planning.common.core.GameLoop;
import io.github.sasori_256.town_planning.common.core.SimulationStep;
import io.github.sasori_256.town_planning.common.event.EventBus;
import io.github.sasori_256.town_planning.common.event.events.DayPassedEvent;
import io.github.sasori_256.town_planning.common.event.events.DisasterOccurredEvent;
import io.github.sasori_256.town_planning.common.event.events.MapUpdatedEvent;
import io.github.sasori_256.town_planning.common.event.events.ResidentBornEvent;
import io.github.sasori_256.town_planning.common.event.events.SoulChangedEvent;
import io.github.sasori_256.town_planning.common.event.events.SoulHarvestedEvent;
import io.github.sasori_256.town_planning.entity.building.Building;
import io.github.sasori_256.town_planning.entity.building.BuildingType;
import io.github.sasori_256.town_planning.entity.disaster.Disaster;
import io.github.sasori_256.town_planning.entity.resident.Resident;
import io.github.sasori_256.town_planning.entity.resident.ResidentState;
import io.github.sasori_256.town_planning.map.model.GameMap;

/**
 * ゲームの環境情報を管理するモデルクラス。
 * GameContextの実装であり、GameLoopのホストでもある。
 * 
 * <h2>Thread Safety and Locking</h2>
 * This class uses a ReadWriteLock to ensure thread-safe access to game state.
 * To prevent nested locking issues and potential deadlocks, entity lifecycle
 * operations
 * (spawnEntity/removeEntity) are automatically deferred when called during the
 * update cycle.
 * 
 * <p>
 * During the update cycle:
 * <ul>
 * <li>Calls to spawnEntity() are queued and processed after all entity updates
 * complete</li>
 * <li>Calls to removeEntity() are queued and processed after all entity updates
 * complete</li>
 * <li>This prevents entity update methods from attempting to acquire locks that
 * are already held</li>
 * </ul>
 * 
 * <p>
 * This design allows entity update strategies to safely call GameContext
 * methods like
 * removeEntity() without causing deadlocks or nested lock acquisitions.
 */
public class GameModel implements GameContext, SimulationStep {
  private final EventBus eventBus;
  private final GameMap gameMap;
  private final ReadWriteLock stateLock = new ReentrantReadWriteLock();
  private GameLoop gameLoop; // 現在は未使用だが、startGameLoopで使われることを想定

  // スレッドセーフなリストを使用
  private final List<Building> buildingEntities = new CopyOnWriteArrayList<>();
  private final List<Resident> residentEntities = new CopyOnWriteArrayList<>();
  private final List<Disaster> disasterEntities = new CopyOnWriteArrayList<>();

  private int souls = 100;
  private final GameTime gameTime = new GameTime();
  /**
   * 住民再配置で最低限成立させる世帯人数。
   * 2人未満の家は空き家/移住対象として扱う。
   */
  private static final int MIN_RESIDENTS_PER_HOUSE = 2;
  // ANIMATION_STEP is 1/6s.
  private static final double ANIMATION_STEP = 1.0 / 6.0;
  private double animationAccumulator = 0.0;

  private double lastDeltaTime = 0;

  // Thread-local flag to track if we're currently in an update cycle
  private final ThreadLocal<Boolean> inUpdateCycle = ThreadLocal.withInitial(() -> false);

  // Lists to defer entity lifecycle operations during update to prevent nested
  // locking
  private final List<BaseGameEntity> entitiesToSpawn = new CopyOnWriteArrayList<>();
  private final List<BaseGameEntity> entitiesToRemove = new CopyOnWriteArrayList<>();

  public GameModel(int mapWidth, int mapHeight, EventBus eventBus) {
    this.eventBus = eventBus;

    // マップサイズ 100x100
    this.gameMap = new GameMap(mapWidth, mapHeight, eventBus);
    // Event Subscriptions
    // SoulHarvestedEventを購読
    this.eventBus.subscribe(SoulHarvestedEvent.class, event -> {
      addSouls(event.amount());
    });

    // GameLoopはstartGameLoopでインスタンス化されるためここではnull
    this.gameLoop = null;
  }

  public void startGameLoop(Runnable renderCallback) {
    DoubleConsumer updateCallback = this::step;
    // GameLoopのインスタンスは新しいものが作られるため、既存のgameLoopフィールドとの整合性は要検討
    this.gameLoop = new GameLoop(updateCallback, renderCallback);
    this.gameLoop.start();
  }

  // --- GameContext Implementation ---

  @Override
  public EventBus getEventBus() {
    return eventBus;
  }

  @Override
  public GameMap getMap() {
    return gameMap;
  }

  // GameContextの定義に合わせて実装
  @Override
  public Stream<Building> getBuildingEntities() {
    return buildingEntities.stream();
  }

  @Override
  public Stream<Resident> getResidentEntities() {
    return residentEntities.stream();
  }

  @Override
  public Stream<Disaster> getDisasterEntities() {
    return disasterEntities.stream();
  }

  @Override
  public double getDeltaTime() {
    return withReadLock(() -> lastDeltaTime);
  }

  @Override
  public int getDay() {
    return withReadLock(() -> gameTime.getDayCount());
  }

  @Override
  public double getTimeOfDaySeconds() {
    return withReadLock(() -> gameTime.getTimeOfDaySeconds());
  }

  @Override
  public double getTimeOfDayNormalized() {
    return withReadLock(() -> gameTime.getTimeOfDayNormalized());
  }

  @Override
  public double getDayLengthSeconds() {
    return withReadLock(() -> gameTime.getDayLengthSeconds());
  }

  /**
   * Spawns a new entity into the game world.
   * 
   * <p>
   * If called during an update cycle (i.e., from within an entity's update
   * method),
   * the spawn operation is automatically deferred and will be processed after all
   * entity updates complete. This prevents nested lock acquisition and potential
   * deadlocks.
   * 
   * <p>
   * If called outside an update cycle, the entity is spawned immediately.
   * 
   * @param entity The entity to spawn
   * @param <T>    The entity type
   */
  @Override
  public <T extends BaseGameEntity> void spawnEntity(T entity) {
    // If called during an update cycle, defer the operation to avoid nested locking
    if (inUpdateCycle.get()) {
      entitiesToSpawn.add(entity);
      return;
    }

    if (entity instanceof Resident) {
      addResidentEntity((Resident) entity);
    } else if (entity instanceof Building) {
      addBuildingEntity((Building) entity);
    } else if (entity instanceof Disaster) {
      addDisasterEntity((Disaster) entity);
    }
  }

  /**
   * Internal method to spawn an entity without acquiring locks.
   * Should only be called when a write lock is already held.
   */
  private <T extends BaseGameEntity> void spawnEntityInternal(T entity) {
    if (entity instanceof Resident) {
      addResidentEntityInternal((Resident) entity);
    } else if (entity instanceof Building) {
      addBuildingEntityInternal((Building) entity);
    } else if (entity instanceof Disaster) {
      addDisasterEntityInternal((Disaster) entity);
    }
  }

  /**
   * Removes an entity from the game world.
   * 
   * <p>
   * If called during an update cycle (i.e., from within an entity's update
   * method),
   * the remove operation is automatically deferred and will be processed after
   * all
   * entity updates complete. This prevents nested lock acquisition and potential
   * deadlocks.
   * 
   * <p>
   * If called outside an update cycle, the entity is removed immediately.
   * 
   * <p>
   * The entity's onRemove() lifecycle method will be called before removal.
   * 
   * @param entity The entity to remove
   * @param <T>    The entity type
   */
  @Override
  public <T extends BaseGameEntity> void removeEntity(T entity) {
    if (entity == null) {
      return;
    }

    // If called during an update cycle, defer the operation to avoid nested locking
    if (inUpdateCycle.get()) {
      entitiesToRemove.add(entity);
      return;
    }

    withWriteLock(() -> {
      // ライフサイクルメソッドの呼び出し
      entity.onRemove(this);

      if (entity instanceof Resident) {
        residentEntities.remove(entity);
      } else if (entity instanceof Building) {
        buildingEntities.remove(entity);
      } else if (entity instanceof Disaster) {
        disasterEntities.remove(entity);
      }
    });
  }

  // --- Game Logic API ---

  public void addResidentEntity(Resident entity) {
    withWriteLock(() -> {
      addResidentEntityInternal(entity);
    });
  }

  /**
   * Internal method to add a resident entity without acquiring locks.
   * Should only be called when a write lock is already held.
   */
  private void addResidentEntityInternal(Resident entity) {
    residentEntities.add(entity);
    entity.onSpawn(this);
    eventBus.publish(new ResidentBornEvent(entity.getPosition()));
  }

  public void addBuildingEntity(Building entity) {
    withWriteLock(() -> {
      addBuildingEntityInternal(entity);
    });
  }

  /**
   * Internal method to add a building entity without acquiring locks.
   * Should only be called when a write lock is already held.
   */
  private void addBuildingEntityInternal(Building entity) {
    buildingEntities.add(entity);
    entity.onSpawn(this);
    eventBus.publish(new MapUpdatedEvent(entity.getPosition()));
  }

  public void addDisasterEntity(Disaster entity) {
    withWriteLock(() -> {
      addDisasterEntityInternal(entity);
    });
  }

  /**
   * Internal method to add a disaster entity without acquiring locks.
   * Should only be called when a write lock is already held.
   */
  private void addDisasterEntityInternal(Disaster entity) {
    disasterEntities.add(entity);
    entity.onSpawn(this);
    eventBus.publish(new DisasterOccurredEvent(entity.getType()));
  }

  public void removeBuildingEntity(Building entity) {
    withWriteLock(() -> {
      gameMap.removeBuilding(entity.getPosition());
      eventBus.publish(new MapUpdatedEvent(entity.getPosition()));
    });
  }

  public int getSouls() {
    return withReadLock(() -> souls);
  }

  public void addSouls(int amount) {
    withWriteLock(() -> {
      addSoulsInternal(amount);
    });
  }

  /**
   * Internal method to add souls without acquiring locks.
   * Should only be called when a write lock is already held.
   */
  private void addSoulsInternal(int amount) {
    this.souls += amount;
    eventBus.publish(new SoulChangedEvent(souls));
  }

  /**
   * 指定座標付近の死体から魂を刈り取る。
   */
  public boolean harvestSoulAt(java.awt.geom.Point2D pos) {
    double harvestRadius = 1.0;

    java.util.Optional<Resident> target = residentEntities.stream()
        .filter(e -> {
          ResidentState state = e.getState();
          return state == ResidentState.DEAD;
        })
        .filter(e -> e.getPosition().distance(pos) <= harvestRadius)
        .findFirst();

    if (target.isPresent()) {
      Resident deadResident = target.get();

      int soulAmount = 10;
      int faith = deadResident.getFaith();
      soulAmount += faith / 5;

      // 魂回収イベント発行
      eventBus.publish(new SoulHarvestedEvent(soulAmount));
      removeEntity(deadResident);
      return true;
    }
    return false;
  }

  public boolean constructBuilding(Point2D.Double pos, BuildingType type) {
    return withWriteLock(() -> {
      if (souls < type.getCost()) {
        return false;
      }

      if (!gameMap.isValidPosition(pos) || !gameMap.getCell(pos).canBuild()) {
        return false;
      }

      addSoulsInternal(-type.getCost());

      Building building = new Building(pos, type);

      if (gameMap.placeBuilding(pos, building)) {
        spawnEntityInternal(building);
        return true;
      } else {
        addSoulsInternal(type.getCost());
        return false;
      }
    });
  }

  /**
   * 住民数を住宅に均等化し、引っ越し対象を決める。
   *
   * <p>全住宅に均等配分しつつ、総住民数が少ない場合は
   * {@link #MIN_RESIDENTS_PER_HOUSE} 人単位で満たせる家を優先する。
   */
  private void rebalanceResidents() {
    List<Building> houses = new ArrayList<>();
    for (Building building : buildingEntities) {
      if (building.getType().getCategory() == CategoryType.RESIDENTIAL) {
        houses.add(building);
      }
    }
    if (houses.isEmpty()) {
      return;
    }

    Map<HomeKey, HouseStats> statsByHome = new HashMap<>();
    int totalResidents = 0;
    for (Resident resident : residentEntities) {
      if (resident.getState() == ResidentState.DEAD) {
        continue;
      }
      Point2D.Double assignedHome = resident.hasRelocationTarget()
          ? resident.getRelocationTarget()
          : resident.getHomePosition();
      if (assignedHome == null) {
        continue;
      }
      HomeKey key = HomeKey.from(assignedHome);
      HouseStats stats = statsByHome.computeIfAbsent(key, k -> new HouseStats());
      stats.assignedCount++;
      if (!resident.hasRelocationTarget()) {
        stats.movableResidents.add(resident);
      }
      totalResidents++;
    }
    if (totalResidents == 0) {
      for (Building building : houses) {
        building.setCurrentPopulation(0);
      }
      return;
    }

    List<HouseInfo> houseInfos = new ArrayList<>();
    for (Building building : houses) {
      HomeKey key = HomeKey.from(building.getPosition());
      HouseStats stats = statsByHome.get(key);
      int assignedCount = stats != null ? stats.assignedCount : 0;
      List<Resident> movable = stats != null ? new ArrayList<>(stats.movableResidents)
          : new ArrayList<>();
      houseInfos.add(new HouseInfo(building, assignedCount, movable));
    }
    houseInfos.sort(Comparator.comparingInt((HouseInfo info) -> info.assignedCount).reversed());

    int houseCount = houseInfos.size();
    List<Integer> targets = new ArrayList<>(houseCount);
    if (totalResidents >= MIN_RESIDENTS_PER_HOUSE * houseCount) {
      int base = totalResidents / houseCount;
      int remainder = totalResidents % houseCount;
      for (int i = 0; i < houseCount; i++) {
        targets.add(base + (i < remainder ? 1 : 0));
      }
    } else {
      int fullHouseCount = totalResidents / MIN_RESIDENTS_PER_HOUSE;
      int remainder = totalResidents % MIN_RESIDENTS_PER_HOUSE;
      for (int i = 0; i < houseCount; i++) {
        targets.add(i < fullHouseCount ? MIN_RESIDENTS_PER_HOUSE : 0);
      }
      if (remainder > 0) {
        targets.set(0, targets.get(0) + remainder);
      }
    }

    List<Resident> movers = new ArrayList<>();
    List<HouseNeed> needs = new ArrayList<>();
    for (int i = 0; i < houseCount; i++) {
      HouseInfo info = houseInfos.get(i);
      int target = targets.get(i);
      int current = info.assignedCount;
      if (current > target) {
        int excess = current - target;
        for (int j = 0; j < excess && !info.movableResidents.isEmpty(); j++) {
          movers.add(info.movableResidents.remove(info.movableResidents.size() - 1));
          info.assignedCount--;
        }
      } else if (current < target) {
        needs.add(new HouseNeed(info, target - current));
      }
    }

    for (HouseNeed need : needs) {
      while (need.remaining > 0 && !movers.isEmpty()) {
        Resident resident = movers.remove(movers.size() - 1);
        Point2D.Double newHome = need.house.building.getPosition();
        resident.requestRelocation(newHome);
        resident.setState(ResidentState.RELOCATING);
        need.house.assignedCount++;
        need.remaining--;
      }
    }

    for (HouseInfo info : houseInfos) {
      info.building.setCurrentPopulation(info.assignedCount);
    }
  }

  private static final class HomeKey {
    private final int x;
    private final int y;

    private HomeKey(int x, int y) {
      this.x = x;
      this.y = y;
    }

    private static HomeKey from(Point2D.Double pos) {
      return new HomeKey((int) Math.round(pos.getX()), (int) Math.round(pos.getY()));
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (!(obj instanceof HomeKey)) {
        return false;
      }
      HomeKey other = (HomeKey) obj;
      return x == other.x && y == other.y;
    }

    @Override
    public int hashCode() {
      return 31 * x + y;
    }
  }

  private static final class HouseInfo {
    private final Building building;
    private int assignedCount;
    private final List<Resident> movableResidents;

    private HouseInfo(Building building, int assignedCount, List<Resident> movableResidents) {
      this.building = building;
      this.assignedCount = assignedCount;
      this.movableResidents = movableResidents;
    }
  }

  private static final class HouseNeed {
    private final HouseInfo house;
    private int remaining;

    private HouseNeed(HouseInfo house, int count) {
      this.house = house;
      this.remaining = count;
    }
  }

  private static final class HouseStats {
    private int assignedCount;
    private final List<Resident> movableResidents = new ArrayList<>();
  }

  // getters / setters
  public GameMap getGameMap() {
    return gameMap;
  }

  public ReadWriteLock getStateLock() {
    return stateLock;
  }

  public GameLoop getGameLoop() {
    return gameLoop;
  } // 現状、startGameLoopで新しいループが作られるので、このgetterの用途は不明

  public void setSouls(int souls) {
    withWriteLock(() -> {
      this.souls = souls;
    });
  }

  public void setDay(int day) {
    withWriteLock(() -> {
      gameTime.setDayCount(day);
    });
  }

  public double getLastDeltaTime() {
    return withReadLock(() -> lastDeltaTime);
  }

  public void setLastDeltaTime(double lastDeltaTime) {
    withWriteLock(() -> {
      this.lastDeltaTime = lastDeltaTime;
    });
  }

  private <T> T withReadLock(Supplier<T> supplier) {
    Lock readLock = stateLock.readLock();
    readLock.lock();
    try {
      return supplier.get();
    } finally {
      readLock.unlock();
    }
  }

  private void withWriteLock(Runnable action) {
    withWriteLock(() -> {
      action.run();
      return null;
    });
  }

  private <T> T withWriteLock(Supplier<T> supplier) {
    Lock writeLock = stateLock.writeLock();
    writeLock.lock();
    try {
      return supplier.get();
    } finally {
      writeLock.unlock();
    }
  }

  /**
   * Updates the game state for the current step.
   *
   * <p>
   * This method acquires a write lock and updates all entities. To prevent nested
   * locking issues, any calls to spawnEntity() or removeEntity() made during
   * entity
   * updates are automatically deferred and processed after all updates complete.
   * 
   * <p>
   * The update process:
   * <ol>
   * <li>Acquire write lock</li>
   * <li>Update game time and state</li>
   * <li>Update all residents, buildings, and disasters (which may queue
   * spawn/remove operations)</li>
   * <li>Advance animations</li>
   * <li>Process all deferred spawn/remove operations</li>
   * <li>Release write lock</li>
   * </ol>
   * 
   * @param dt Delta time in seconds since the last step
   */
  @Override
  public void step(double dt) {
    stepInternal(this, dt);
  }

  private void stepInternal(GameContext context, double dt) {
    withWriteLock(() -> {
      // Set flag to indicate we're in an update cycle
      inUpdateCycle.set(true);

      try {
        this.lastDeltaTime = dt;

        int previousDay = gameTime.getDayCount();
        int daysAdvanced = gameTime.advance(dt);
        if (daysAdvanced > 0) {
          int currentDay = gameTime.getDayCount();
          for (int dayNumber = previousDay + 1; dayNumber <= currentDay; dayNumber++) {
            eventBus.publish(new DayPassedEvent(dayNumber));
            rebalanceResidents();
          }
        }

        for (Resident resident : residentEntities) {
          resident.update(context);
        }

        for (Building building : buildingEntities) {
          building.update(context);
        }

        for (Disaster disaster : disasterEntities) {
          disaster.update(context);
        }

        animationAccumulator += dt;
        while (animationAccumulator >= ANIMATION_STEP) {
          animationAccumulator -= ANIMATION_STEP;
          advanceAnimations(ANIMATION_STEP);
        }

        // Process deferred entity lifecycle operations
        processDeferredOperations();
      } finally {
        // Clear flag when exiting update cycle
        inUpdateCycle.set(false);
      }
    });
  }

  private void advanceAnimations(double dt) {
    for (Resident resident : residentEntities) {
      resident.advanceAnimation(dt);
    }

    for (Building building : buildingEntities) {
      building.advanceAnimation(dt);
    }

    for (Disaster disaster : disasterEntities) {
      disaster.advanceAnimation(dt);
    }
  }

  /**
   * Process deferred entity lifecycle operations that were queued during the
   * update cycle.
   * This method should only be called while holding the write lock.
   */
  private void processDeferredOperations() {
    // Process entities to remove first
      for (BaseGameEntity entity : entitiesToRemove) {
        // Call lifecycle method
        entity.onRemove(this);

      // Remove from appropriate list
      if (entity instanceof Resident) {
        residentEntities.remove(entity);
      } else if (entity instanceof Building) {
        buildingEntities.remove(entity);
      } else if (entity instanceof Disaster) {
        disasterEntities.remove(entity);
      }
    }
    entitiesToRemove.clear();

    // Process entities to spawn
    for (BaseGameEntity entity : entitiesToSpawn) {
      spawnEntityInternal(entity);
    }
    entitiesToSpawn.clear();
  }
}
