package io.github.sasori_256.town_planning.entity.model;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

import io.github.sasori_256.town_planning.common.core.GameLoop;
import io.github.sasori_256.town_planning.common.core.Updatable;
import io.github.sasori_256.town_planning.common.event.EventBus;
import io.github.sasori_256.town_planning.common.event.events.DayPassedEvent;
import io.github.sasori_256.town_planning.common.event.events.MapUpdatedEvent;
import io.github.sasori_256.town_planning.common.event.events.ResidentBornEvent;
import io.github.sasori_256.town_planning.common.event.events.SoulChangedEvent;
import io.github.sasori_256.town_planning.common.event.events.SoulHarvestedEvent;
import io.github.sasori_256.town_planning.entity.building.Building;
import io.github.sasori_256.town_planning.entity.building.BuildingType;
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
  private GameLoop gameLoop; // 現在は未使用だが、startGameLoopで使われることを想定

  // スレッドセーフなリストを使用
  private final List<Building> buildingEntities = new CopyOnWriteArrayList<>();
  private final List<Resident> residentEntities = new CopyOnWriteArrayList<>();

  private int souls = 100;
  private int day = 1;
  private double dayTimer = 0;
  private static final double DAY_LENGTH = 10.0; // 10秒で1日

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
    Runnable updateCallback = () -> update(this);
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
  public double getDeltaTime() {
    return lastDeltaTime;
  }

  @Override
  public <T extends BaseGameEntity> void spawnEntity(T entity) {
    if (entity instanceof Resident) {
      addResidentEntity((Resident) entity);
    } else if (entity instanceof Building) {
      addBuildingEntity((Building) entity);
    }
  }

  @Override
  public <T extends BaseGameEntity> void removeEntity(T entity) {
    if (entity == null) {
      return;
    }

    // ライフサイクルメソッドの呼び出し
    entity.onRemoved();

    if (entity instanceof Resident) {
      residentEntities.remove(entity);
    } else if (entity instanceof Building) {
      buildingEntities.remove(entity);
    }
  }

  // --- Game Logic API ---

  public void addResidentEntity(Resident entity) {
    residentEntities.add(entity);
    eventBus.publish(new ResidentBornEvent(entity.getPosition()));
  }

  public void addBuildingEntity(Building entity) {
    buildingEntities.add(entity);
    eventBus.publish(new MapUpdatedEvent(entity.getPosition()));
  }

  public void removeBuildingEntity(Building entity) {
    gameMap.removeBuilding(entity.getPosition());
    eventBus.publish(new MapUpdatedEvent(entity.getPosition()));
  }

  public int getSouls() {
    return souls;
  }

  public void addSouls(int amount) {
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
  }

  // getters / setters
  public int getDay() {
    return day;
  }

  public GameMap getGameMap() {
    return gameMap;
  }

  public GameLoop getGameLoop() {
    return gameLoop;
  } // 現状、startGameLoopで新しいループが作られるので、このgetterの用途は不明

  public void setSouls(int souls) {
    this.souls = souls;
  }

  public void setDay(int day) {
    this.day = day;
  }

  public double getDayTimer() {
    return dayTimer;
  }

  public void setDayTimer(double dayTimer) {
    this.dayTimer = dayTimer;
  }

  public static double getDayLength() {
    return DAY_LENGTH;
  }

  public double getLastDeltaTime() {
    return lastDeltaTime;
  }

  public void setLastDeltaTime(double lastDeltaTime) {
    this.lastDeltaTime = lastDeltaTime;
  }

  @Override
  public void update(GameContext context) {
    double dt = 1.0 / 60.0;
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
  }
}
