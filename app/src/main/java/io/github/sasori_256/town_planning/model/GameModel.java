package io.github.sasori_256.town_planning.model;

import io.github.sasori_256.town_planning.core.*;
import io.github.sasori_256.town_planning.event.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

/**
 * ゲームの全体モデル。
 * GameContextの実装であり、GameLoopのホストでもある。
 */
public class GameModel implements GameContext, Updatable {
  private final EventBus eventBus;
  private final GameMap gameMap;
  private final GameLoop gameLoop;

  // スレッドセーフなリストを使用（更新スレッドと描画スレッド/UIスレッドからのアクセスがあるため）
  private final List<GameObject> entities = new CopyOnWriteArrayList<>();

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

    this.gameLoop = new GameLoop(this::tick, () -> {
      // Render Trigger (View側で購読するか、専用のリスナーを呼ぶ)
      // 今回はEventBusだと高頻度すぎるかもしれないが、一旦保留。
      // 通常、Viewは repaint() を呼び出す Runnable を渡す。
    });
  }

  public void startGameLoop(Runnable renderCallback) {
    // 既存のループを作り直す必要がある（RenderCallbackを注入するため）
    // またはGameLoopを少し改造してsetterをつける。
    // ここでは新しいGameLoopインスタンスを作る簡易実装。
    GameLoop loop = new GameLoop(this::tick, renderCallback);
    loop.start();
  }

  @Override
  public void tick() {
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
    for (GameObject entity : entities) {
      entity.update(this);
    }
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

  @Override
  public Stream<GameObject> getEntities() {
    return entities.stream();
  }

  @Override
  public double getDeltaTime() {
    return lastDeltaTime;
  }

  @Override
  public void spawnEntity(GameObject entity) {
    addEntity(entity);
  }

  @Override
  public void destroyEntity(GameObject entity) {
    removeEntity(entity);
  }

  // --- Game Logic API ---

  public void addEntity(GameObject entity) {
    entities.add(entity);
    eventBus.publish(EventType.MAP_UPDATED, entity.getPosition());
  }

  public void removeEntity(GameObject entity) {
    entities.remove(entity);
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
    java.util.Optional<GameObject> target = entities.stream()
        .filter(e -> {
          ResidentAttributes.State state = e.getAttribute(ResidentAttributes.State.STATE); // STATEキーが"state"文字列と重複注意。ResidentAttributes.STATE定数を使う。

          // AttributeキーはStringなので、ResidentAttributes.STATE (="state") を使う。
          // getAttributeの戻り値はEnum。
          Object stateObj = e.getAttribute(ResidentAttributes.STATE);
          return stateObj == ResidentAttributes.State.DEAD;
        })
        .filter(e -> e.getPosition().distance(pos) <= harvestRadius)
        .findFirst();

    if (target.isPresent()) {
      GameObject soul = target.get();

      // 魂回収
      int soulAmount = 10; // 仮: 住民の種類や信仰心によって変動させるとなお良い

      // 信仰心ボーナス計算 (例)
      Integer faith = soul.getAttribute(ResidentAttributes.FAITH);
      if (faith != null) {
        soulAmount += faith / 5;
      }
      eventBus.publish(EventType.SOUL_HARVESTED, soulAmount);
      destroyEntity(soul);
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
  public boolean constructBuilding(BuildingType type, java.awt.geom.Point2D pos) {

    // 1. コストチェック

    if (souls < type.getCost()) {
      return false;
    }

    // 2. マップ上の建設可否チェック

    // GameMap.placeBuilding内でチェックされるが、ここでは事前にチェックしてコスト消費を制御する
    if (!gameMap.isValid(pos) || !gameMap.getCell(pos).canBuild()) {
      return false;
    }

    // 3. 建設処理

    // 魂消費
    addSouls(-type.getCost());

    // GameObject生成
    GameObject building = new GameObject(pos);

    // Strategy設定
    building.setRenderStrategy(
        io.github.sasori_256.town_planning.model.strategy.SimpleRenderStrategy.fromBuildingType(type));
    // 建物ごとの固有ロジック
    if (type == BuildingType.HOUSE) {
      building.setUpdateStrategy(
          new io.github.sasori_256.town_planning.model.strategy.PopulationGrowthStrategy(type.getCapacity()));
    }

    // マップとエンティティリストへの登録

    // Note: placeBuildingはMapCellへの登録のみを行う。Entityリストへの登録は別途必要。
    // また、GameObjectとGameEntityの整合性を保つため、GameMapはGameObjectを受け取るように修正が必要かもしれないが、
    // 現状はGameMapはGameEntityを受け取る。GameObjectはGameEntityを実装しているのでOK。
    if (gameMap.placeBuilding(pos, building)) {
      addEntity(building);
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

}
