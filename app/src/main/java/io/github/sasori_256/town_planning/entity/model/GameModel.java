package io.github.sasori_256.town_planning.entity.model;

import java.awt.geom.Point2D;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.DoubleConsumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import io.github.sasori_256.town_planning.common.core.GameLoop;
import io.github.sasori_256.town_planning.common.core.SimulationStep;
import io.github.sasori_256.town_planning.common.event.EventBus;
import io.github.sasori_256.town_planning.entity.building.Building;
import io.github.sasori_256.town_planning.entity.building.BuildingType;
import io.github.sasori_256.town_planning.entity.disaster.Disaster;
import io.github.sasori_256.town_planning.entity.model.BaseGameEntity;
import io.github.sasori_256.town_planning.entity.model.manager.BuildingManager;
import io.github.sasori_256.town_planning.entity.model.manager.EntityManager;
import io.github.sasori_256.town_planning.entity.model.manager.PopulationManager;
import io.github.sasori_256.town_planning.entity.model.manager.RelocationManager;
import io.github.sasori_256.town_planning.entity.model.manager.SoulManager;
import io.github.sasori_256.town_planning.entity.model.manager.TimeManager;
import io.github.sasori_256.town_planning.entity.resident.Resident;
import io.github.sasori_256.town_planning.map.model.GameMap;

/**
 * ゲームの環境情報を管理するモデルクラス。
 * GameContextの実装であり、GameLoopのホストでもある。
 *
 * <h2>スレッド安全性とロック</h2>
 * 本クラスはReadWriteLockで状態アクセスを保護する。
 * ネストしたロック取得やデッドロックを避けるため、更新サイクル中に呼ばれた
 * エンティティの生成・削除はEntityManagerでキューに積み、更新後にまとめて処理する。
 */
public class GameModel implements GameContext, SimulationStep {
  /** 初期魂所持量。 */
  private static final int INITIAL_SOUL = 100;
  /** アニメーション進行の固定ステップ(秒)。 */
  private static final double ANIMATION_STEP = 1.0 / 6.0;

  /** イベント通知に使用するイベントバス。 */
  private final EventBus eventBus;
  /** マップ状態の本体。 */
  private final GameMap gameMap;
  /** 共有状態を保護する読み書きロック。 */
  private final ReadWriteLock stateLock = new ReentrantReadWriteLock();
  /** ゲームループの参照。startGameLoop() で初期化される。 */
  private GameLoop gameLoop;

  /** エンティティ管理。 */
  private final EntityManager entityManager;
  /** 魂管理。 */
  private final SoulManager soulManager;
  /** 時間管理。 */
  private final TimeManager timeManager;
  /** 引っ越し管理。 */
  private final RelocationManager relocationManager;
  /** 住民数管理。 */
  private final PopulationManager populationManager;
  /** 建物管理。 */
  private final BuildingManager buildingManager;

  /** アニメーション進行用の経過時間バッファ。 */
  private double animationAccumulator = 0.0;
  /** 前フレームからの経過秒。 */
  private double lastDeltaTime = 0;

  /**
   * ゲームモデルを生成する。
   *
   * @param mapWidth  マップの横幅
   * @param mapHeight マップの縦幅
   * @param eventBus  イベントバス
   */
  public GameModel(int mapWidth, int mapHeight, EventBus eventBus) {
    this.eventBus = eventBus;
    this.gameMap = new GameMap(mapWidth, mapHeight, eventBus);

    this.entityManager = new EntityManager(eventBus, stateLock);
    this.soulManager = new SoulManager(eventBus, stateLock, entityManager, INITIAL_SOUL);
    this.timeManager = new TimeManager(eventBus, stateLock);
    this.relocationManager = new RelocationManager(stateLock, entityManager);
    this.populationManager = new PopulationManager(stateLock, entityManager);
    this.buildingManager = new BuildingManager(eventBus, gameMap, stateLock, soulManager,
        entityManager);

    this.gameLoop = null;
  }

  /**
   * ゲームループを起動する。
   *
   * @param renderCallback 描画コールバック
   */
  public void startGameLoop(Runnable renderCallback) {
    DoubleConsumer updateCallback = this::step;
    this.gameLoop = new GameLoop(updateCallback, renderCallback);
    this.gameLoop.start();
  }

  // --- GameContext Implementation ---

  /** {@inheritDoc} */
  @Override
  public EventBus getEventBus() {
    return eventBus;
  }

  /** {@inheritDoc} */
  @Override
  public GameMap getMap() {
    return gameMap;
  }

  /** {@inheritDoc} */
  @Override
  public Stream<Building> getBuildingEntities() {
    return entityManager.getBuildingEntities();
  }

  /** {@inheritDoc} */
  @Override
  public Stream<Resident> getResidentEntities() {
    return entityManager.getResidentEntities();
  }

  /** {@inheritDoc} */
  @Override
  public Stream<Disaster> getDisasterEntities() {
    return entityManager.getDisasterEntities();
  }

  /** {@inheritDoc} */
  @Override
  public double getDeltaTime() {
    return withReadLock(() -> lastDeltaTime);
  }

  /** {@inheritDoc} */
  @Override
  public int getDay() {
    return timeManager.getDay();
  }

  /** {@inheritDoc} */
  @Override
  public double getTimeOfDaySeconds() {
    return timeManager.getTimeOfDaySeconds();
  }

  /** {@inheritDoc} */
  @Override
  public double getTimeOfDayNormalized() {
    return timeManager.getTimeOfDayNormalized();
  }

  /** {@inheritDoc} */
  @Override
  public double getDayLengthSeconds() {
    return timeManager.getDayLengthSeconds();
  }

  /** {@inheritDoc} */
  @Override
  public int getSoul() {
    return soulManager.getSoul();
  }

  /** {@inheritDoc} */
  @Override
  public int getPopulationTotal() {
    return populationManager.getTotalPopulation();
  }

  /** {@inheritDoc} */
  @Override
  public int getPopulationAlive() {
    return populationManager.getAlivePopulation();
  }

  /** {@inheritDoc} */
  @Override
  public int getPopulationDead() {
    return populationManager.getDeadPopulation();
  }

  /** {@inheritDoc} */
  @Override
  public <T extends BaseGameEntity> void spawnEntity(T entity) {
    entityManager.spawnEntity(entity, this);
  }

  /** {@inheritDoc} */
  @Override
  public <T extends BaseGameEntity> void removeEntity(T entity) {
    entityManager.removeEntity(entity, this);
  }

  // --- Game Logic API ---

  /**
   * 住民エンティティを追加する。
   *
   * @param entity 住民
   */
  public void addResidentEntity(Resident entity) {
    spawnEntity(entity);
  }

  /**
   * 建物エンティティを追加する。
   *
   * @param entity 建物
   */
  public void addBuildingEntity(Building entity) {
    spawnEntity(entity);
  }

  /**
   * 災害エンティティを追加する。
   *
   * @param entity 災害
   */
  public void addDisasterEntity(Disaster entity) {
    spawnEntity(entity);
  }

  /**
   * 建物エンティティを削除する。
   *
   * @param entity 建物
   */
  public void removeBuildingEntity(Building entity) {
    buildingManager.removeBuildingEntity(entity);
  }

  /**
   * 魂を加算する。
   *
   * @param amount 追加量
   */
  public void addSoul(int amount) {
    soulManager.addSoul(amount);
  }

  /**
   * 魂所持量を設定する。
   *
   * @param soul 魂所持量
   */
  public void setSoul(int soul) {
    soulManager.setSoul(soul);
  }

  /**
   * 指定座標付近の死体から魂を刈り取る。
   *
   * @param pos 対象位置
   * @return 収穫できた場合はtrue
   */
  public boolean harvestSoulAt(Point2D pos) {
    return soulManager.harvestSoulAt(this, pos);
  }

  /**
   * 建物を建設する。
   *
   * @param pos  設置位置
   * @param type 建物種別
   * @return 建設できた場合はtrue
   */
  public boolean constructBuilding(Point2D.Double pos, BuildingType type) {
    return buildingManager.constructBuilding(this, pos, type);
  }

  // getters / setters
  /**
   * マップを返す。
   *
   * @return マップ
   */
  public GameMap getGameMap() {
    return gameMap;
  }

  /**
   * 状態ロックを返す。
   *
   * @return 状態ロック
   */
  public ReadWriteLock getStateLock() {
    return stateLock;
  }

  /**
   * ゲームループを返す。
   *
   * @return ゲームループ
   */
  public GameLoop getGameLoop() {
    return gameLoop;
  } // 現状、startGameLoopで新しいループが作られるので、このgetterの用途は不明

  /**
   * 日数を設定する。
   *
   * @param day 日数
   */
  public void setDay(int day) {
    timeManager.setDay(day);
  }

  /**
   * 直近のdelta timeを返す。
   *
   * @return delta time
   */
  public double getLastDeltaTime() {
    return withReadLock(() -> lastDeltaTime);
  }

  /**
   * 直近のdelta timeを設定する。
   *
   * @param lastDeltaTime delta time
   */
  public void setLastDeltaTime(double lastDeltaTime) {
    withWriteLock(() -> {
      this.lastDeltaTime = lastDeltaTime;
    });
  }

  /**
   * 読み込みロック内で引数なし返り値ありの関数を実行する。
   *
   * @param supplier 取得処理
   */
  private <T> T withReadLock(Supplier<T> supplier) {
    Lock readLock = stateLock.readLock();
    readLock.lock();
    try {
      return supplier.get();
    } finally {
      readLock.unlock();
    }
  }

  /**
   * 書き込みロック内でrun関数を実行する。
   *
   * @param action 実行処理
   */
  private void withWriteLock(Runnable action) {
    withWriteLock(() -> {
      action.run();
      return null;
    });
  }

  /**
   * 書き込みロック内で引数なし返り値ありの関数を実行する。
   *
   * @param supplier 状態更新処理
   */
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
   * 現在ステップのゲーム状態を更新する。
   *
   * @param dt 前回からの経過秒
   */
  @Override
  public void step(double dt) {
    stepInternal(this, dt);
  }

  /**
   * 実際の更新処理をまとめて実行する内部メソッド。
   *
   * @param context ゲームコンテキスト
   * @param dt      経過秒
   */
  private void stepInternal(GameContext context, double dt) {
    withWriteLock(() -> {
      // updateサイクル中の生成/削除を遅延する
      entityManager.beginUpdateCycle();
      try {
        this.lastDeltaTime = dt;

        // 日付進行と日次処理をまとめて実行
        timeManager.advance(dt, day -> relocationManager.rebalanceResidents());

        // 住民・建物・災害の更新
        entityManager.updateEntities(context);

        // 6fps相当のアニメーション進行
        animationAccumulator += dt;
        while (animationAccumulator >= ANIMATION_STEP) {
          animationAccumulator -= ANIMATION_STEP;
          entityManager.advanceAnimations(ANIMATION_STEP);
        }

        // update中に溜まった生成/削除を反映
        entityManager.processDeferredOperations(context);
      } finally {
        entityManager.endUpdateCycle();
      }
    });
  }
}
