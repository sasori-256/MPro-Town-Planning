package io.github.sasori_256.town_planning.common.ui.main;

import java.util.concurrent.locks.ReadWriteLock;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

import io.github.sasori_256.town_planning.common.event.EventBus;
import io.github.sasori_256.town_planning.common.event.Subscription;
import io.github.sasori_256.town_planning.common.event.events.MapUpdatedEvent;
import io.github.sasori_256.town_planning.common.ui.ImageManager;
import io.github.sasori_256.town_planning.common.ui.gameObjectSelect.controller.CategoryNode;
import io.github.sasori_256.town_planning.common.ui.gameObjectSelect.controller.NodeMenuInitializer;
import io.github.sasori_256.town_planning.common.ui.main.scene.GameMapPanel;
import io.github.sasori_256.town_planning.entity.Camera;
import io.github.sasori_256.town_planning.entity.model.GameModel;
import io.github.sasori_256.town_planning.map.controller.GameMapController;
import io.github.sasori_256.town_planning.map.model.GameMap;

public class GameSession {
  private final EventBus eventBus = EventBus.getInstance();
  private final GameModel gameModel;
  private final GameMap gameMap;
  private final Camera camera;
  private final ReadWriteLock stateLock;
  private final GameMapController gameMapController;
  private final GameMapPanel gameMapPanel;
  private Subscription mapSub;

  /**
   * ゲームセッションを生成する。
   *
   * @param windowWidth  ウィンドウ幅
   * @param windowHeight ウィンドウ高さ
   * @param mapWidth     マップ幅
   * @param mapHeight    マップ高さ
   * @param seed         マップ生成シード
   * @param imageManager 画像管理
   * @param navigator    画面遷移ナビゲータ
   */
  public GameSession(
      int windowWidth,
      int windowHeight,
      int mapWidth,
      int mapHeight,
      long seed,
      ImageManager imageManager,
      GameFlowNavigator navigator) {
    this.gameModel = new GameModel(mapWidth, mapHeight, seed);
    this.gameMap = gameModel.getGameMap();
    this.stateLock = gameModel.getStateLock();
    this.camera = new Camera(1, windowWidth, windowHeight, mapWidth, mapHeight);
    this.gameMapController = new GameMapController(camera, stateLock);

    CategoryNode root = NodeMenuInitializer.setup(this.gameMapController, this.gameModel);
    this.gameMapPanel = new GameMapPanel(this.gameMap, this.gameModel, this.camera, root, this.stateLock,
        imageManager, navigator);
    this.gameMapPanel.addMouseListener(this.gameMapController);
    this.gameMapPanel.addMouseMotionListener(this.gameMapController);
    this.gameMapPanel.addMouseWheelListener(this.gameMapController);
    this.gameMapPanel.addKeyListener(this.gameMapController);
    this.gameMapPanel.setFocusable(true);

    this.mapSub = eventBus.subscribe(MapUpdatedEvent.class, event -> {
      if (SwingUtilities.isEventDispatchThread()) {
        gameMapPanel.repaint();
      } else {
        SwingUtilities.invokeLater(gameMapPanel::repaint);
      }
    });
  }

  /**
   * ゲーム画面のコンポーネントを返す。
   */
  public JComponent getView() {
    return gameMapPanel;
  }

  /**
   * セッションで使用しているイベントバスを返す。
   */
  public EventBus getEventBus() {
    return eventBus;
  }

  /**
   * ゲームループを開始する。
   *
   * @param renderCallback 描画更新コールバック
   */
  public void start(Runnable renderCallback) {
    gameModel.startGameLoop(renderCallback);
  }

  /**
   * ゲームループを一時停止する。
   */
  public void pause() {
    if (gameModel.getGameLoop() != null) {
      gameModel.getGameLoop().pause();
    }
  }

  /**
   * ゲームループを停止する。
   */
  public void stop() {
    if (gameModel.getGameLoop() != null) {
      gameModel.getGameLoop().stop();
    }
  }

  /**
   * セッションの後始末を行う。
   */
  public void dispose() {
    stop();
    if (mapSub != null) {
      mapSub.unsubscribe();
      mapSub = null;
    }
  }

  /**
   * 結果画面向けのスナップショットを取得する。
   */
  public GameResult snapshot() {
    return new GameResult(
        gameModel.getDay(),
        gameModel.getSoul(),
        gameModel.getPopulationMax(),
        gameModel.getPopulationTotalDeaths());
  }

  /**
   * 画面サイズに合わせてカメラサイズを更新する。
   */
  public void updateCameraScreenSize(int width, int height) {
    camera.setScreenSize(width, height);
  }
}
