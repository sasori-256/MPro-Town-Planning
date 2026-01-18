package io.github.sasori_256.town_planning.common.ui.main;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.event.ComponentAdapter;
import java.util.concurrent.locks.ReadWriteLock;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

import io.github.sasori_256.town_planning.common.event.EventBus;
import io.github.sasori_256.town_planning.common.event.Subscription;
import io.github.sasori_256.town_planning.common.event.events.MapUpdatedEvent;
import io.github.sasori_256.town_planning.common.ui.ImageManager;
import io.github.sasori_256.town_planning.common.ui.gameObjectSelect.controller.CategoryNode;
import io.github.sasori_256.town_planning.common.ui.gameObjectSelect.controller.NodeMenuInitializer;
import io.github.sasori_256.town_planning.common.ui.main.seen.GameMapPanel;
import io.github.sasori_256.town_planning.common.ui.main.seen.TitlePanel;
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
  JPanel mainPanel;
  CardLayout cardLayout;
  GameModel gameModel;
  GameMap gameMap;
  Camera camera;
  EventBus eventBus;
  GameMapController gameMapController;
  ReadWriteLock stateLock;
  ImageManager imageManager;

  /**
   * ゲーム描画ウィンドウを初期化する。
   *
   * @param listener          入力イベントのリスナー
   * @param gameModel         ゲーム状態を管理するモデル
   * @param gameMap           ゲームマップ
   * @param camera            カメラ
   * @param width             ウィンドウ幅
   * @param height            ウィンドウ高さ
   * @param eventBus          イベントバス
   * @param gameMapController マップ操作コントローラ
   * @param stateLock         状態ロック
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
  }

  /**
   * カードレイアウトの初期化を行う。
   * 各シーンのパネルを作成し、メインパネルに追加する。
   * シーン切り替えのためのSceneNavigatorも設定する。
   */
  private void setupCardPanel() {
    CardLayout cardLayout = new CardLayout();
    this.mainPanel = new JPanel(cardLayout);

    SceneNavigator nav = (sceneName) -> {
      cardLayout.show(this.mainPanel, sceneName);
      repaintCurrentSceneUI();
    };

    JPanel titlePanel = createTitlePanel(nav);
    JPanel gameMapPanel = createGameMapPanel();
    // 新しいシーンを追加する場合はここに記載。そのシーン内でシーンを切り替える場合はnavを渡す。

    this.mainPanel.add(titlePanel, "TITLE");
    this.mainPanel.add(gameMapPanel, "GAME_MAP");

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
}
