package io.github.sasori_256.town_planning.common.ui.main;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.event.ComponentAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import io.github.sasori_256.town_planning.common.core.GameConfig;
import io.github.sasori_256.town_planning.common.event.EventBus;
import io.github.sasori_256.town_planning.common.event.Subscription;
import io.github.sasori_256.town_planning.common.event.events.ConfigLoadFailedEvent;
import io.github.sasori_256.town_planning.common.event.events.EntitySpawnFailedEvent;
import io.github.sasori_256.town_planning.common.event.events.EntitySpawnFailureReason;
import io.github.sasori_256.town_planning.common.event.events.EntitySpawnKind;
import io.github.sasori_256.town_planning.common.event.events.GameOverEvent;
import io.github.sasori_256.town_planning.common.event.events.MapUpdatedEvent;
import io.github.sasori_256.town_planning.common.ui.ImageManager;
import io.github.sasori_256.town_planning.common.ui.ToastManager;
import io.github.sasori_256.town_planning.common.ui.gameObjectSelect.controller.CategoryNode;
import io.github.sasori_256.town_planning.common.ui.gameObjectSelect.controller.NodeMenuInitializer;
import io.github.sasori_256.town_planning.common.ui.main.scene.EndPanel;
import io.github.sasori_256.town_planning.common.ui.main.scene.GameMapPanel;
import io.github.sasori_256.town_planning.common.ui.main.scene.TitlePanel;
import io.github.sasori_256.town_planning.entity.Camera;
import io.github.sasori_256.town_planning.entity.model.GameModel;
import io.github.sasori_256.town_planning.map.controller.GameMapController;
import io.github.sasori_256.town_planning.map.model.GameMap;

/**
 * ゲームウィンドウを表すクラス。
 * ウィンドウサイズは生成時の設定により決定される。
 * タイトル: "Town Planning Game"
 *
 * @see GameMapPanel
 */
public class GameWindow extends JFrame {
  private JPanel mainPanel;
  private CardLayout cardLayout;
  private GameModel gameModel;
  private GameMap gameMap;
  private Camera camera;
  private EventBus eventBus;
  private GameMapController gameMapController;
  private ReadWriteLock stateLock;
  private ImageManager imageManager;
  private SceneNavigator sceneNavigator;
  private EndPanel endPanel;
  private Timer gameOverTimer;
  private final AtomicBoolean gameOverHandled = new AtomicBoolean(false);
  private ToastManager toastManager;
  private Subscription configSub;
  private Subscription spawnSub;
  private Subscription gameOverSub;
  private Subscription mapSub;

  /**
   * ゲーム描画ウィンドウを初期化する。
   *
   * @param gameModel         ゲーム状態を管理するモデル
   * @param camera            カメラ
   * @param width             ウィンドウ幅
   * @param height            ウィンドウ高さ
   * @param eventBus          イベントバス
   * @param gameMapController マップ操作コントローラ
   * @param stateLock         状態ロック
   * @param imageManager      画像管理マネージャ
   */
  public GameWindow(
      GameModel gameModel,
      Camera camera,
      int width,
      int height,
      EventBus eventBus,
      GameMapController gameMapController,
      ReadWriteLock stateLock,
      ImageManager imageManager) {
    this.gameModel = gameModel;
    this.gameMap = gameModel.getMap();
    this.camera = camera;
    this.eventBus = eventBus;
    this.gameMapController = gameMapController;
    this.stateLock = stateLock;
    this.imageManager = imageManager;

    setTitle("Town Planning Game");
    setSize(width, height);
    setupCardPanel();
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    this.toastManager = new ToastManager(getLayeredPane(), getLayeredPane());
    this.configSub = eventBus.subscribe(ConfigLoadFailedEvent.class,
        event -> this.toastManager.show(buildConfigMessage(event), ToastManager.ToastType.ERROR));
    this.spawnSub = eventBus.subscribe(EntitySpawnFailedEvent.class,
        event -> this.toastManager.show(buildSpawnFailureMessage(event), ToastManager.ToastType.WARNING));
    this.gameOverSub = eventBus.subscribe(GameOverEvent.class, event -> handleGameOver());
    this.addComponentListener(new ComponentAdapter() {
      /**
       * ウィンドウサイズ変更時の処理を行う。
       *
       * @param e イベント
       */
      @Override
      public void componentResized(java.awt.event.ComponentEvent e) {
        repaintCurrentSceneUI();
        camera.setScreenSize(GameWindow.this.mainPanel.getWidth(), GameWindow.this.mainPanel.getHeight());
        // MEMO:ウィンドウリサイズ時の処理を追加する場合はここに記載 Cameraの位置修正とか
      }
    });
    this.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosed(WindowEvent e) {
        cleanupSubscriptions();
      }

      @Override
      public void windowClosing(WindowEvent e) {
        cleanupSubscriptions();
      }
    });
    setVisible(true);
    GameConfig.reportErrors(eventBus);
  }

  /**
   * カードレイアウトの初期化を行う。
   * 各シーンのパネルを作成し、メインパネルに追加する。
   * シーン切り替えのためのSceneNavigatorも設定する。
   */
  private void setupCardPanel() {
    this.cardLayout = new CardLayout();
    this.mainPanel = new JPanel(this.cardLayout);

    this.sceneNavigator = (sceneName) -> {
      this.cardLayout.show(this.mainPanel, sceneName);
      repaintCurrentSceneUI();
    };

    JPanel titlePanel = createTitlePanel(this.sceneNavigator);
    JPanel gameMapPanel = createGameMapPanel();
    this.endPanel = createEndPanel();
    // 新しいシーンを追加する場合はここに記載。そのシーン内でシーンを切り替える場合はnavを渡す。

    this.mainPanel.add(titlePanel, "TITLE");
    this.mainPanel.add(gameMapPanel, "GAME_MAP");
    this.mainPanel.add(this.endPanel, "END");

    this.add(this.mainPanel, BorderLayout.CENTER);
  }

  private void repaintCurrentSceneUI() {
    for (Component comp : this.mainPanel.getComponents()) {
      if (comp.isVisible() && comp instanceof UiRefreshable) {
        ((UiRefreshable) comp).repaintUI();
      }
    }
  }

  /**
   * GameMapPanelを作成する。
   * 
   * @return GameMapPanelのインスタンス
   */
  private JPanel createGameMapPanel() {
    CategoryNode root = NodeMenuInitializer.setup(this.gameMapController, this.gameModel);
    GameMapPanel gameMapPanel = new GameMapPanel(this.gameMap, this.gameModel, this.camera, root, this.stateLock,
        this.imageManager);
    gameMapPanel.addMouseListener(this.gameMapController);
    gameMapPanel.addMouseMotionListener(this.gameMapController);
    gameMapPanel.addMouseWheelListener(this.gameMapController);
    gameMapPanel.addKeyListener(this.gameMapController);
    gameMapPanel.setFocusable(true);
    // TODO: onCloseなる関数でWindowのフレーム破棄時にunsubscribeするようにする
    this.mapSub = eventBus.subscribe(MapUpdatedEvent.class, event -> {
      if (SwingUtilities.isEventDispatchThread()) {
        gameMapPanel.repaint();
      } else {
        SwingUtilities.invokeLater(gameMapPanel::repaint);
      }
    });
    return gameMapPanel;
  }

  /**
   * TitlePanelを作成する。
   * 
   * @param sceneNavigator シーンの切り替えを行うためのSceneNavigator
   * @return TitlePanelのインスタンス
   */
  private JPanel createTitlePanel(SceneNavigator sceneNavigator) {
    TitlePanel titlePanel = new TitlePanel(sceneNavigator, this.imageManager);
    return titlePanel;
  }

  private EndPanel createEndPanel() {
    return new EndPanel();
  }

  /**
   * GameOver時に実行する終了処理。
   * Eventのunsubscribeやトースト通知、ゲームオーバー画面に表示する要素の引き渡しなどを行う。
   */
  private void handleGameOver() {
    if (!gameOverHandled.compareAndSet(false, true)) {
      return;
    }
    if (toastManager != null) {
      toastManager.show("住民がいなくなりました。", ToastManager.ToastType.INFO);
    }
    if (gameModel.getGameLoop() != null) {
      gameModel.getGameLoop().pause();
    }
    unsubscribe(mapSub);
    mapSub = null;
    int day = gameModel.getDay();
    int soul = gameModel.getSoul();
    int maxPopulation = gameModel.getPopulationMax();
    int totalDeaths = gameModel.getPopulationTotalDeaths();
    SwingUtilities.invokeLater(() -> scheduleGameOverTransition(day, soul, maxPopulation, totalDeaths));
  }

  private void scheduleGameOverTransition(int day, int soul, int maxPopulation, int totalDeaths) {
    if (this.endPanel != null) {
      this.endPanel.setResult(day, soul, maxPopulation, totalDeaths);
    }
    if (gameOverTimer != null) {
      gameOverTimer.stop();
    }
    gameOverTimer = new Timer(3000, event -> {
      if (sceneNavigator != null) {
        sceneNavigator.changeScene("END");
      }
      if (gameModel.getGameLoop() != null) {
        gameModel.getGameLoop().stop();
      }
    });
    gameOverTimer.setRepeats(false);
    gameOverTimer.start();
  }

  private void cleanupSubscriptions() {
    unsubscribe(mapSub);
    unsubscribe(configSub);
    unsubscribe(spawnSub);
    unsubscribe(gameOverSub);
    if (gameOverTimer != null) {
      gameOverTimer.stop();
      gameOverTimer = null;
    }
    mapSub = null;
    configSub = null;
    spawnSub = null;
    gameOverSub = null;
  }

  private void unsubscribe(Subscription sub) {
    if (sub != null) {
      sub.unsubscribe();
    }
  }

  private static String buildConfigMessage(ConfigLoadFailedEvent event) {
    String code = event.code();
    if ("CONFIG_NOT_FOUND".equals(code)) {
      return "設定ファイルが見つかりません (game.properties)";
    }
    if ("CONFIG_LOAD_FAILED".equals(code)) {
      return "設定ファイルの読み込みに失敗しました";
    }
    if ("CONFIG_PARSE_FAILED".equals(code)) {
      return "設定値が不正です: " + safeDetail(event.message());
    }
    return "設定読み込みエラー: " + safeDetail(event.message());
  }

  private static String buildSpawnFailureMessage(EntitySpawnFailedEvent event) {
    String kindName = describeKind(event.kind());
    EntitySpawnFailureReason reason = event.reason();
    if (reason == EntitySpawnFailureReason.INSUFFICIENT_SOUL) {
      return kindName + "を生成できません: 魂が足りません";
    }
    if (reason == EntitySpawnFailureReason.INVALID_POSITION) {
      return kindName + "を生成できません: 設置できない場所です";
    }
    if (reason == EntitySpawnFailureReason.PLACEMENT_BLOCKED) {
      return kindName + "を生成できません: 設置場所が塞がっています";
    }
    if (reason == EntitySpawnFailureReason.INVALID_ENTITY) {
      return kindName + "の生成に失敗しました: 対象が不正です";
    }
    return kindName + "の生成に失敗しました";
  }

  private static String describeKind(EntitySpawnKind kind) {
    if (kind == EntitySpawnKind.DISASTER) {
      return "災害";
    }
    return "建物";
  }

  private static String safeDetail(String detail) {
    return detail == null || detail.isBlank() ? "詳細不明" : detail;
  }
}
