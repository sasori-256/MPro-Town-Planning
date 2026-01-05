package io.github.sasori_256.town_planning.entity.model;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.DoubleConsumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import io.github.sasori_256.town_planning.common.core.GameLoop;
import io.github.sasori_256.town_planning.common.core.Updatable;
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
 * To prevent nested locking issues and potential deadlocks, entity lifecycle operations
 * (spawnEntity/removeEntity) are automatically deferred when called during the update cycle.
 * 
 * <p>During the update cycle:
 * <ul>
 *   <li>Calls to spawnEntity() are queued and processed after all entity updates complete</li>
 *   <li>Calls to removeEntity() are queued and processed after all entity updates complete</li>
 *   <li>This prevents entity update methods from attempting to acquire locks that are already held</li>
 * </ul>
 * 
 * <p>This design allows entity update strategies to safely call GameContext methods like
 * removeEntity() without causing deadlocks or nested lock acquisitions.
 */
public class GameModel implements GameContext, Updatable {
  private final EventBus eventBus;
  private final GameMap gameMap;
  private final ReadWriteLock stateLock = new ReentrantReadWriteLock();
  private GameLoop gameLoop; // 現在は未使用だが、startGameLoopで使われることを想定

  // スレッドセーフなリストを使用
  private final List<Building> buildingEntities = new CopyOnWriteArrayList<>();
  private final List<Resident> residentEntities = new CopyOnWriteArrayList<>();
  private final List<Disaster> disasterEntities = new CopyOnWriteArrayList<>();

  private int souls = 100;
  private int day = 1;
  private double dayTimer = 0;
  private static final double DAY_LENGTH = 10.0; // 10秒で1日
  // UPDATE_STEP is 1/30s and ANIMATION_STEP is 1/6s, so animations advance every 5 update steps.
  private static final double UPDATE_STEP = 1.0 / 30.0;
  private static final double ANIMATION_STEP = 1.0 / 6.0;
  private double animationAccumulator = 0.0;

  private double lastDeltaTime = 0;

  // Thread-local flag to track if we're currently in an update cycle
  private final ThreadLocal<Boolean> inUpdateCycle = ThreadLocal.withInitial(() -> false);
  
  // Lists to defer entity lifecycle operations during update to prevent nested locking
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
    DoubleConsumer updateCallback = dt -> update(this, dt);
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

  /**
   * Spawns a new entity into the game world.
   * 
   * <p>If called during an update cycle (i.e., from within an entity's update method),
   * the spawn operation is automatically deferred and will be processed after all
   * entity updates complete. This prevents nested lock acquisition and potential deadlocks.
   * 
   * <p>If called outside an update cycle, the entity is spawned immediately.
   * 
   * @param entity The entity to spawn
   * @param <T> The entity type
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
   * <p>If called during an update cycle (i.e., from within an entity's update method),
   * the remove operation is automatically deferred and will be processed after all
   * entity updates complete. This prevents nested lock acquisition and potential deadlocks.
   * 
   * <p>If called outside an update cycle, the entity is removed immediately.
   * 
   * <p>The entity's onRemoved() lifecycle method will be called before removal.
   * 
   * @param entity The entity to remove
   * @param <T> The entity type
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
      entity.onRemoved();

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

  // getters / setters
  public int getDay() {
    return withReadLock(() -> day);
  }

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
      this.day = day;
    });
  }

  public double getDayTimer() {
    return withReadLock(() -> dayTimer);
  }

  public void setDayTimer(double dayTimer) {
    withWriteLock(() -> {
      this.dayTimer = dayTimer;
    });
  }

  public static double getDayLength() {
    return DAY_LENGTH;
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

  @Override
  public void update(GameContext context) {
    update(context, UPDATE_STEP);
  }

  /**
   * Updates the game state for the current frame.
   * 
   * <p>This method acquires a write lock and updates all entities. To prevent nested
   * locking issues, any calls to spawnEntity() or removeEntity() made during entity
   * updates are automatically deferred and processed after all updates complete.
   * 
   * <p>The update process:
   * <ol>
   *   <li>Acquire write lock</li>
   *   <li>Update day timer and game state</li>
   *   <li>Update all residents, buildings, and disasters (which may queue spawn/remove operations)</li>
   *   <li>Advance animations</li>
   *   <li>Process all deferred spawn/remove operations</li>
   *   <li>Release write lock</li>
   * </ol>
   * 
   * @param context The game context
   * @param dt Delta time in seconds since the last update
   */
  public void update(GameContext context, double dt) {
    withWriteLock(() -> {
      // Set flag to indicate we're in an update cycle
      inUpdateCycle.set(true);
      
      try {
        this.lastDeltaTime = dt;

        dayTimer += dt;
        if (dayTimer >= DAY_LENGTH) {
          dayTimer = 0;
          day++;
          eventBus.publish(new DayPassedEvent(day));
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
          advanceAnimations();
        }
        
        // Process deferred entity lifecycle operations
        processDeferredOperations();
      } finally {
        // Clear flag when exiting update cycle
        inUpdateCycle.set(false);
      }
    });
  }

  private void advanceAnimations() {
    for (Resident resident : residentEntities) {
      resident.advanceAnimation();
    }

    for (Building building : buildingEntities) {
      building.advanceAnimation();
    }

    for (Disaster disaster : disasterEntities) {
      disaster.advanceAnimation();
    }
  }
  
  /**
   * Process deferred entity lifecycle operations that were queued during the update cycle.
   * This method should only be called while holding the write lock.
   */
  private void processDeferredOperations() {
    // Process entities to remove first
    for (BaseGameEntity entity : entitiesToRemove) {
      // Call lifecycle method
      entity.onRemoved();
      
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
