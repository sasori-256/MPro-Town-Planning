package io.github.sasori_256.town_planning.gameObject.model;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

import io.github.sasori_256.town_planning.common.core.CompositeUpdateStrategy;
import io.github.sasori_256.town_planning.common.core.GameLoop;
import io.github.sasori_256.town_planning.common.core.Updatable;
import io.github.sasori_256.town_planning.common.event.EventBus;
import io.github.sasori_256.town_planning.common.event.EventType;
import io.github.sasori_256.town_planning.gameObject.building.BuildingObject;
import io.github.sasori_256.town_planning.gameObject.building.BuildingType;
import io.github.sasori_256.town_planning.gameObject.building.strategy.PopulationGrowthStrategy;
import io.github.sasori_256.town_planning.gameObject.resident.ResidentObject;
import io.github.sasori_256.town_planning.gameObject.resident.ResidentState;
import io.github.sasori_256.town_planning.map.model.GameMap;

/**
 * ゲームの環境情報を管理するモデルクラス。
 * GameContextの実装であり、GameLoopのホストでもある。
 */
public class GameModel implements GameContext, Updatable {
  private final EventBus eventBus;
  private final GameMap gameMap;
  private final GameLoop gameLoop;

  // スレッドセーフなリストを使用（更新スレッドと描画スレッド/UIスレッドからのアクセスがあるため）
  private final List<ResidentObject> residentEntities = new CopyOnWriteArrayList<>();
  private final List<BuildingObject> buildingEntities = new CopyOnWriteArrayList<>();

  private int souls = 100;
  private int day = 1;
  private double dayTimer = 0;
  private static final double DAY_LENGTH = 10.0; // 10秒で1日

  private double lastDeltaTime = 0;

  public GameModel(EventBus eventBus) {
    this.eventBus = eventBus;

    // マップサイズ 100x100 (仮)
    this.gameMap = new GameMap(100, 100, eventBus);

    // Event Subscriptions
    this.eventBus.subscribe(EventType.SOUL_HARVESTED, data -> {
      if (data instanceof Integer) {
        addSouls((Integer) data);
      }
    });

    // ゲームループのセットアップ

    // Updateはthis.tick()、RenderはView側で行うが、
    // GameLoopはRunnableを受け取るので、Viewへの通知はEventBus経由かCallbackで行う必要がある。
    // 今回は単純化のため、GameModelはUpdateのみをループで回し、Render更新通知をEventBusで投げる形にするか、
    // あるいはViewがGameLoopのRenderCallbackを登録できるようにする。
    // ここでは、GameModelがループを管理し、View更新用Callbackを受け取れるように設計する。

    // this.gameLoop = new GameLoop(this::update, () -> {
    // Render Trigger (View側で購読するか、専用のリスナーを呼ぶ)
    // 今回はEventBusだと高頻度すぎるかもしれないが、一旦保留。
    // 通常、Viewは repaint() を呼び出す Runnable を渡す。
    // });
    this.gameLoop = null;
  }

  public void startGameLoop(Runnable renderCallback) {
    // 既存のループを作り直す必要がある（RenderCallbackを注入するため）
    // またはGameLoopを少し改造してsetterをつける。
    // ここでは新しいGameLoopインスタンスを作る簡易実装。
    Runnable updateCallback = () -> update(this);
    GameLoop loop = new GameLoop(updateCallback, renderCallback);
    loop.start();
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

  /**
   * 住民・建物の2種類の全エンティティをストリームで返す。
   * 
   * @return 指定した型の全エンティティのストリーム
   */
  @Override
  public <T extends BaseGameEntity> Stream<T> getEntities() {
    return Stream.concat(residentEntities.stream(), buildingEntities.stream())
        .map(e -> (T) e);
  }

  @Override
  public double getDeltaTime() {
    return lastDeltaTime;
  }

  @Override
  public <T extends BaseGameEntity> void spawnEntity(T entity) {
    if (entity instanceof ResidentObject) {
      addResidentEntity((ResidentObject) entity);
    } else if (entity instanceof BuildingObject) {
      addBuildingEntity((BuildingObject) entity);
    }
  }

  @Override
  public <T extends BaseGameEntity> void removeEntity(T entity) {
    if (entity instanceof ResidentObject) {
      residentEntities.remove(entity);
    } else if (entity instanceof BuildingObject) {
      buildingEntities.remove(entity);
    }
  }

  // --- Game Logic API ---

  public void addResidentEntity(ResidentObject entity) {
    residentEntities.add(entity);
    eventBus.publish(EventType.RESIDENT_BORN, entity.getPosition());
  }

  public void addBuildingEntity(BuildingObject entity) {
    buildingEntities.add(entity);
    eventBus.publish(EventType.MAP_UPDATED, entity.getPosition());
  }

  public void removeBuildingEntity(BuildingObject entity) {
    gameMap.removeBuilding(entity.getPosition());
    // マップ上の占有情報などもクリアする必要があるならMap経由で行う
    eventBus.publish(EventType.MAP_UPDATED, entity.getPosition());
  }

  public int getSouls() {
    return souls;
  }

  public void addSouls(int amount) {
    this.souls += amount;
    eventBus.publish(EventType.SOUL_CHANGED, souls);
  }

  /**
   * 指定座標付近の死体から魂を刈り取る。
   * 
   * @param pos クリック座標
   * 
   * @return 刈り取りに成功したらtrue
   */
  public boolean harvestSoulAt(java.awt.geom.Point2D pos) {
    double harvestRadius = 1.0; // 半径1グリッド

    // 範囲内の死体を探す
    // Note: 複数の死体が重なっている場合、1つだけ回収するか全部回収するかは仕様次第。
    // ここでは最初に見つかった1つを回収する。
    java.util.Optional<ResidentObject> target = residentEntities.stream()
        .filter(e -> {
          ResidentState state = e.getState();
          return state == ResidentState.DEAD;
        })
        .filter(e -> e.getPosition().distance(pos) <= harvestRadius)
        .findFirst();

    if (target.isPresent()) {
      ResidentObject deadResident = target.get();

      // 魂回収
      int soulAmount = 10; // 仮: 住民の種類や信仰心によって変動させるとなお良い

      // 信仰心ボーナス計算 (例)
      Integer faith = deadResident.getFaith();
      if (faith != null) {
        soulAmount += faith / 5;
      }
      eventBus.publish(EventType.SOUL_HARVESTED, soulAmount);
      removeEntity(deadResident);
      return true;
    }
    return false;
  }

  /**
   * 
   * 建物を建設する。
   * 
   * @param type 建物の種類
   * @param pos  建設位置（グリッド座標）
   * 
   * @return 建設に成功したらtrue
   * 
   */
  public boolean constructBuilding(Point2D.Double pos, BuildingType type) {

    // 1. コストチェック
    if (souls < type.getCost()) {
      return false;
    }

    // 2. マップ上の建設可否チェック

    // GameMap.placeBuilding内でチェックされるが、ここでは事前にチェックしてコスト消費を制御する
    if (!gameMap.isValidPos(pos) || !gameMap.getCell(pos).canBuild()) {
      return false;
    }

    // 3. 建設処理

    // 魂消費
    addSouls(-type.getCost());

    // BaseGameEntity生成
    BuildingObject building = new BuildingObject(pos, type);

    // Strategy設定
    // building.setRenderStrategy( type.getRenderStrategy() );
    // 建物ごとの固有ロジック
    if (type == BuildingType.HOUSE) {
      CompositeUpdateStrategy compositeUpdateStrategy = new CompositeUpdateStrategy(
          new PopulationGrowthStrategy(type.getMaxPopulation()));
      building.setUpdateStrategy(compositeUpdateStrategy);
    }
    // マップとエンティティリストへの登録

    // NOTE:
    // placeBuildingはMapCellへの登録のみを行う。Entityリストへの登録は別途必要。
    // また、BaseGameEntityとGameEntityの整合性を保つため、GameMapはBaseGameEntityを受け取るように修正が必要かもしれないが、
    // 現状はGameMapはGameEntityを受け取る。GameObjectはGameEntityを実装しているのでOK。
    if (gameMap.placeBuilding(pos, building)) {
      spawnEntity(building);
      return true;
    } else {
      // 万が一Mapへの配置に失敗した場合は払い戻し（通常ここには来ないはず）
      addSouls(type.getCost());
      return false;
    }
  }

  public int getDay() {
    return day;
  }

  public GameMap getGameMap() {
    return gameMap;
  }

  public GameLoop getGameLoop() {
    return gameLoop;
  }

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
    // 時間計測 (簡易的)
    double dt = 1.0 / 60.0; // Fixed time step
    this.lastDeltaTime = dt;

    // 時間経過処理
    dayTimer += dt;
    if (dayTimer >= DAY_LENGTH) {
      dayTimer = 0;
      day++;
      eventBus.publish(EventType.DAY_PASSED, day);
    }

    // 全エンティティの更新
    for (ResidentObject resident : residentEntities) {
      resident.update(context);
    }

    for (BuildingObject building : buildingEntities) {
      building.update(context);
    }
  }

}
