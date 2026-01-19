package io.github.sasori_256.town_planning.common.ui.main;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.event.ComponentAdapter;
import java.util.concurrent.locks.ReadWriteLock;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import io.github.sasori_256.town_planning.common.core.GameConfig;
import io.github.sasori_256.town_planning.common.event.EventBus;
import io.github.sasori_256.town_planning.common.event.Subscription;
import io.github.sasori_256.town_planning.common.event.events.ConfigLoadFailedEvent;
import io.github.sasori_256.town_planning.common.event.events.EntitySpawnFailedEvent;
import io.github.sasori_256.town_planning.common.event.events.EntitySpawnFailureReason;
import io.github.sasori_256.town_planning.common.event.events.EntitySpawnKind;
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
 * ゲームウィンドウを表すクラス
 * ウィンドウサイズ: 640*640
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

  /**
   * ゲーム描画ウィンドウを初期化する。
   *
   * @param gameModel         ゲーム状態を管理するモデル
   * @param gameMap           ゲームマップ
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
      GameMap gameMap,
      Camera camera,
      int width,
      int height,
      EventBus eventBus,
      GameMapController gameMapController,
      ReadWriteLock stateLock,
      ImageManager imageManager) {
    this.gameModel = gameModel;
    this.gameMap = gameMap;
    this.camera = camera;
    this.eventBus = eventBus;
    this.gameMapController = gameMapController;
    this.stateLock = stateLock;
    this.imageManager = imageManager;
    setTitle("Town Planning Game");
    setSize(width, height);
    setupCardPanel();
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    ToastManager toastManager = new ToastManager(getLayeredPane(), getLayeredPane());
    Subscription configSub = eventBus.subscribe(ConfigLoadFailedEvent.class,
        event -> toastManager.show(buildConfigMessage(event), ToastManager.ToastType.ERROR));
    Subscription spawnSub = eventBus.subscribe(EntitySpawnFailedEvent.class,
        event -> toastManager.show(buildSpawnFailureMessage(event), ToastManager.ToastType.WARNING));
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

    SceneNavigator nav = (sceneName) -> {
      this.cardLayout.show(this.mainPanel, sceneName);
      repaintCurrentSceneUI();
    };

    JPanel titlePanel = createTitlePanel(nav);
    JPanel gameMapPanel = createGameMapPanel();
    JPanel endPanel = createEndPanel();
    // 新しいシーンを追加する場合はここに記載。そのシーン内でシーンを切り替える場合はnavを渡す。

    this.mainPanel.add(titlePanel, "TITLE");
    this.mainPanel.add(gameMapPanel, "GAME_MAP");
    this.mainPanel.add(endPanel, "END");

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
    Subscription mapSub = eventBus.subscribe(MapUpdatedEvent.class, event -> {
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
   * @param sceneNavigator
   * @return TitlePanelのインスタンス
   */
  private JPanel createTitlePanel(SceneNavigator sceneNavigator) {
    TitlePanel titlePanel = new TitlePanel(sceneNavigator, this.imageManager);
    return titlePanel;
  }

  private JPanel createEndPanel() {
    EndPanel endPanel = new EndPanel();
    return endPanel;
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