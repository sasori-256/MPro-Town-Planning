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

  public GameModel(EventBus eventBus) {
    this.eventBus = eventBus;

    // マップサイズ 100x100
    this.gameMap = new GameMap(100, 100, eventBus);

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

  @Override
  public <T extends BaseGameEntity> void spawnEntity(T entity) {
    if (entity instanceof Resident) {
      addResidentEntity((Resident) entity);
    } else if (entity instanceof Building) {
      addBuildingEntity((Building) entity);
    } else if (entity instanceof Disaster) {
      addDisasterEntity((Disaster) entity);
    }
  }

  @Override
  public <T extends BaseGameEntity> void removeEntity(T entity) {
    if (entity == null) {
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
      residentEntities.add(entity);
      eventBus.publish(new ResidentBornEvent(entity.getPosition()));
    });
  }

  public void addBuildingEntity(Building entity) {
    withWriteLock(() -> {
      buildingEntities.add(entity);
      eventBus.publish(new MapUpdatedEvent(entity.getPosition()));
    });
  }

  public void addDisasterEntity(Disaster entity) {
    withWriteLock(() -> {
      disasterEntities.add(entity);
      eventBus.publish(new DisasterOccurredEvent(entity.getType()));
    });
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
      this.souls += amount;
      eventBus.publish(new SoulChangedEvent(souls));
    });
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

      addSouls(-type.getCost());

      Building building = new Building(pos, type);

      if (gameMap.placeBuilding(pos, building)) {
        spawnEntity(building);
        return true;
      } else {
        addSouls(type.getCost());
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

  public void update(GameContext context, double dt) {
    withWriteLock(() -> {
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

      animationAccumulator += dt;
      while (animationAccumulator >= ANIMATION_STEP) {
        animationAccumulator -= ANIMATION_STEP;
        advanceAnimations();
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
}
