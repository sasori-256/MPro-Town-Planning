package io.github.sasori_256.town_planning.common.ui;

import java.awt.BorderLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.util.concurrent.locks.ReadWriteLock;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import io.github.sasori_256.town_planning.common.event.EventBus;
import io.github.sasori_256.town_planning.common.event.Subscription;
import io.github.sasori_256.town_planning.common.event.events.MapUpdatedEvent;
import io.github.sasori_256.town_planning.common.ui.gameObjectSelect.controller.CategoryNode;
import io.github.sasori_256.town_planning.common.ui.gameObjectSelect.controller.NodeMenuInitializer;
import io.github.sasori_256.town_planning.entity.Camera;
import io.github.sasori_256.town_planning.entity.model.GameModel;
import io.github.sasori_256.town_planning.map.controller.GameMapController;
import io.github.sasori_256.town_planning.map.model.GameMap;
import io.github.sasori_256.town_planning.map.view.GameMapPanel;

/**
 * ゲームウィンドウを表すクラス
 * ウィンドウサイズ: 640*640
 * タイトル: "Town Planning Game"
 *
 * @see GameMapPanel
 */
public class GameWindow extends JFrame {
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
  public <T extends MouseListener & MouseMotionListener & MouseWheelListener & KeyListener> GameWindow(
      T listener,
      GameModel gameModel,
      GameMap gameMap,
      Camera camera,
      int width,
      int height,
      EventBus eventBus,
      GameMapController gameMapController,
      ReadWriteLock stateLock) {
    setTitle("Town Planning Game");
    setSize(width, height);
    // GameMap gameMap = generateTestMap();
    CategoryNode root = NodeMenuInitializer.setup(gameMapController, gameModel);

    GameMapPanel gameMapPanel = new GameMapPanel(gameMap, gameModel, camera, root, stateLock);
    gameMapPanel.addMouseListener(listener);
    gameMapPanel.addMouseMotionListener(listener);
    gameMapPanel.addMouseWheelListener(listener);
    gameMapPanel.addKeyListener(listener);
    gameMapPanel.setFocusable(true);
    // TODO: onCloseなる関数でWindowのフレーム破棄時にunsubscribeするようにする
    Subscription mapSub = eventBus.subscribe(MapUpdatedEvent.class, event -> {
      if (SwingUtilities.isEventDispatchThread()) {
        gameMapPanel.repaint();
      } else {
        SwingUtilities.invokeLater(gameMapPanel::repaint);
      }
    });
    this.add(gameMapPanel, BorderLayout.CENTER);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.addComponentListener(new ComponentAdapter() {
      /**
       * ウィンドウサイズ変更時の処理を行う。
       *
       * @param e イベント
       */
      @Override
      public void componentResized(java.awt.event.ComponentEvent e) {
        gameMapPanel.repaintUI();
        camera.setScreenSize(gameMapPanel.getWidth(), gameMapPanel.getHeight());
        // MEMO:ウィンドウリサイズ時の処理を追加する場合はここに記載 Cameraの位置修正とか
      }
    });
    setVisible(true);
  }
}
