package io.github.sasori_256.town_planning.common.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ComponentAdapter;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import io.github.sasori_256.town_planning.common.event.EventBus;
import io.github.sasori_256.town_planning.common.event.events.MapUpdatedEvent;
import io.github.sasori_256.town_planning.common.ui.gameObjectSelect.controller.CategoryNode;
import io.github.sasori_256.town_planning.common.ui.gameObjectSelect.controller.NodeMenuInitializer;
import io.github.sasori_256.town_planning.entity.Camera;
import io.github.sasori_256.town_planning.map.controller.GameMapController;
import io.github.sasori_256.town_planning.map.model.GameMap;

/**
 * gameMapの内容を描画するクラス
 */
class GameMapPanel extends JPanel {

  private final GameMap gameMap;
  private final Camera camera;
  private final CategoryNode root;
  private final ImageManager imageManager;
  private final AnimationManager animationManager;
  private final PaintGameObject paintGameObject;
  private final PaintUI paintUI;
  private final ReadWriteLock stateLock;

  public GameMapPanel(GameMap gameMap, Camera camera, CategoryNode root, ReadWriteLock stateLock) {
    this.gameMap = gameMap;
    this.camera = camera;
    this.root = root;
    this.imageManager = new ImageManager();
    this.animationManager = new AnimationManager();
    this.paintGameObject = new PaintGameObject();
    this.stateLock = stateLock;
    this.setLayout(null);
    setBackground(Color.BLACK);
    this.paintUI = new PaintUI(imageManager, this, root);
    paintUI.paint();
    add(animationManager);
    // 子コンポーネントをオーバーレイ表示するため、animationManager をパネル全体に広げる
    animationManager.setOpaque(false);
    animationManager.setBounds(0, 0, this.getWidth(), this.getHeight());
    // リサイズ時に animationManager のサイズを更新する
    this.addComponentListener(new java.awt.event.ComponentAdapter() {
      @Override
      public void componentResized(java.awt.event.ComponentEvent e) {
        
      }
    });
    revalidate();
    repaint();
  }

  /**
   * gameMapの内容を描画する
   * 
   * @param g 描画に使用するGraphicsオブジェクト
   * @implNote 画像ファイルが見つからない場合、"Warning Image not found: imageName.png at (x,y)"
   *           という警告が出力されます。
   * @see GameMap
   */
  @Override
  public void paintComponent(Graphics g) {

    super.paintComponent(g);
    Lock readLock = stateLock.readLock();
    readLock.lock();
    try {
      // マップの奥(上)から手前(下)に向かって描画する
      for (int z = 0; z < gameMap.getWidth() + gameMap.getHeight(); z++) {
        for (int x = 0; x <= z; x++) {
          int y = z - x;
          if (x < gameMap.getWidth() && y < gameMap.getHeight() && isInsideCameraView(x, y)) {
            Point2D.Double pos = new Point2D.Double(x, y);
            paintGameObject.paintTerrain(g, pos, gameMap, camera, imageManager, this);
            paintGameObject.paintBuilding(g, pos, gameMap, camera, imageManager, this);
          }
        }
      }
    } finally {
      readLock.unlock();
    }
  }

  boolean isInsideCameraView(int x, int y) {
    Point2D.Double screenPos = camera.isoToScreen(new Point2D.Double(x + 0, y));
    int panelWidth = this.getWidth();
    int panelHeight = this.getHeight();
    double cameraScale = camera.getScale();
    int margin = (int) (cameraScale * 32);
    // 画面外にある場合は描画しない
    if (screenPos.x < -margin || screenPos.x > panelWidth + margin || screenPos.y < -margin
        || screenPos.y > panelHeight + margin) {
      return false;
    }
    return true;
  }

  public void repaintUI() {
    this.paintUI.repaintUI();
  }

  public AnimationManager getAnimationManager() {
    return this.animationManager;
  }
}

/**
 * ゲームウィンドウを表すクラス
 * ウィンドウサイズ: 640*640
 * タイトル: "Town Planning Game"
 * 
 * @see GameMapPanel
 */
public class GameWindow extends JFrame {
  public <T extends MouseListener & MouseMotionListener & MouseWheelListener & KeyListener> GameWindow(T listener,
      GameMap gameMap, Camera camera, int width, int height, EventBus eventBus, GameMapController gameMapController,
      ReadWriteLock stateLock) {
    setTitle("Town Planning Game");
    setSize(width, height);
    // GameMap gameMap = generateTestMap();;
    CategoryNode root = NodeMenuInitializer.setup(gameMapController, gameMap);

    GameMapPanel gameMapPanel = new GameMapPanel(gameMap, camera, root, stateLock);
    gameMapPanel.addMouseListener(listener);
    gameMapPanel.addMouseMotionListener(listener);
    gameMapPanel.addMouseWheelListener(listener);
    gameMapPanel.addKeyListener(listener);
    gameMapPanel.setFocusable(true);
    eventBus.subscribe(MapUpdatedEvent.class, event -> {
      if (SwingUtilities.isEventDispatchThread()) {
        gameMapPanel.repaint();
      } else {
        SwingUtilities.invokeLater(gameMapPanel::repaint);
      }
    });
    this.add(gameMapPanel, BorderLayout.CENTER);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(java.awt.event.ComponentEvent e) {
        gameMapPanel.repaintUI();
        camera.setScreenSize(getWidth(), getHeight());
        gameMapPanel.getAnimationManager().setBounds(0, 0, getWidth(), getHeight());
        // MEMO:ウィンドウリサイズ時の処理を追加する場合はここに記載 Cameraの位置修正とか
      }
    });
    setVisible(true);
  }
}
