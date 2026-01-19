package io.github.sasori_256.town_planning.entity.model;

import java.awt.geom.Point2D;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.DoubleConsumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import io.github.sasori_256.town_planning.common.core.GameConfig;
import io.github.sasori_256.town_planning.common.core.GameLoop;
import io.github.sasori_256.town_planning.common.core.SimulationStep;
import io.github.sasori_256.town_planning.common.event.EventBus;
import io.github.sasori_256.town_planning.entity.building.Building;
import io.github.sasori_256.town_planning.entity.building.BuildingType;
import io.github.sasori_256.town_planning.entity.disaster.Disaster;
import io.github.sasori_256.town_planning.entity.model.manager.BuildingManager;
import io.github.sasori_256.town_planning.entity.model.manager.EntityManager;
import io.github.sasori_256.town_planning.entity.model.manager.PopulationManager;
import io.github.sasori_256.town_planning.entity.model.manager.RelocationManager;
import io.github.sasori_256.town_planning.entity.model.manager.SoulManager;
import io.github.sasori_256.town_planning.entity.model.manager.TimeManager;
import io.github.sasori_256.town_planning.entity.resident.Resident;
import io.github.sasori_256.town_planning.map.model.GameMap;
import io.github.sasori_256.town_planning.map.model.TerrainType;

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
  public GameModel(int mapWidth, int mapHeight, long seed, EventBus eventBus) {
    this.eventBus = eventBus;
    this.gameMap = new GameMap(mapWidth, mapHeight, seed, eventBus);
    GenerateTownHall(seed); // マップ中央付近に町の中心を生成

    this.entityManager = new EntityManager(eventBus, stateLock);
    this.soulManager = new SoulManager(eventBus, stateLock, entityManager, INITIAL_SOUL);
    this.timeManager = new TimeManager(eventBus, stateLock);
    this.relocationManager = new RelocationManager(stateLock, entityManager);
    this.populationManager = new PopulationManager(stateLock, entityManager);
    this.buildingManager = new BuildingManager(eventBus, gameMap, stateLock, soulManager,
        entityManager);

    this.gameLoop = null;

    GameConfig.preload();
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
  
  /**
   * マップ中央付近に町の中心を生成する。
   * 
   * @param seed シード値
   */
  private void GenerateTownHall(long seed) {
    int width = gameMap.getWidth();
    int height = gameMap.getHeight();
    int centerX = width / 2;
    int centerY = height / 2;
    Random rand = new Random(seed);
    final int MAX_LOOP_COUNT = 10;
    // マップの中央を基準に正規分布に従ってオフセットを決定
    // 配置しようとした位置が海だった場合はseedを変えて再試行する
    int offsetX, offsetY, townHallX = width/2, townHallY = height/2, loopCount = 0;
    boolean foundProperPos = false; // 陸地が見つかったかどうか
    while (loopCount < MAX_LOOP_COUNT) {
      offsetX = (int) Math.round(rand.nextGaussian() * width / 10);
      offsetY = (int) Math.round(rand.nextGaussian() * height / 10);
      townHallX = centerX + offsetX;
      townHallY = centerY + offsetY;
      if (gameMap.getCell(new Point2D.Double(townHallX, townHallY)).getTerrain() != TerrainType.WATER) {
        foundProperPos = true;
        break;
      }
      loopCount++;
      rand.setSeed(seed + loopCount); // townHallPosが海だった場合はシードを変えて再試行
    }
    // 10回試行しても陸地が見つからない場合はマップを全探索し、最初に見つけた陸地に配置する
    for (int y = 0; y < height && !foundProperPos; y++) {
      for (int x = 0; x < width && !foundProperPos; x++) {
        if (gameMap.getCell(new Point2D.Double(x, y)).getTerrain() != TerrainType.WATER) {
          townHallX = x;
          townHallY = y;
          foundProperPos = true;
          System.out.println("Warning: マップ中央付近に適切な陸地が見つかりませんでした。最初に見つかった陸地に町の中心を配置します。");
        }
      }
    }
    if (!foundProperPos) {
      // 陸地が一つもないマップの場合は強制的に中央に配置
      System.out.println("Warning: マップに陸地が存在しません。町の中心をマップ中央に強制配置します。");
      townHallX = centerX;
      townHallY = centerY;
    }

    // TODO: 町の中心として一旦青い屋根の家を使用　正しい建物タイプに変更する必要あり
    // TODO: 住民の初期スポーンもここで行うはずなのだが、(続)
    // Exception in thread "main" java.lang.NullPointerException: Cannot invoke "io.github.sasori_256.town_planning.entity.model.manager.EntityManager.spawnEntity(io.github.sasori_256.town_planning.entity.model.BaseGameEntity, io.github.sasori_256.town_planning.entity.model.GameContext)" because "this.entityManager" is null
    // エラーが発生するためコメントアウト中
    Point2D.Double townHallPos = new Point2D.Double(townHallX, townHallY);
    Building townHall = new Building(townHallPos, BuildingType.BLUE_ROOFED_HOUSE);
    if (gameMap.placeBuilding(townHallPos, townHall)) {
      townHall.setCurrentPopulation(2);
      // spawnEntity(townHall);
      // spawnEntity(new Resident(new Point2D.Double(townHallPos.y, townHallPos.x),
          // ResidentType.CITIZEN, ResidentState.AT_HOME, townHallPos));
      // spawnEntity(new Resident(new Point2D.Double(townHallPos.y, townHallPos.x),
          // ResidentType.CITIZEN, ResidentState.AT_HOME, townHallPos));
    }
  }
}

