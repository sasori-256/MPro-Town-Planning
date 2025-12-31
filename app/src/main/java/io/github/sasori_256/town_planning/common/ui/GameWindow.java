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

import javax.swing.JFrame;
import javax.swing.JPanel;

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

  public static final boolean ENABLE_UI = true; // UI描画を有効にするかどうかのフラグ 推奨: true
  public static final boolean INFINITE_RELOAD = false; // 無限リロードモードを有効にするかどうかのフラグ 推奨: false
  public static final boolean SHOW_REPAINT_COUNT_AND_FPS = true; // 再描画回数と平均fpsをコンソールに表示するかどうかのフラグ 推奨: false

  private final GameMap gameMap;
  private final Camera camera;
  private final CategoryNode root;
  private final ImageManager imageManager;
  private final PaintGameObject paintGameObject;
  private final PaintUI paintUI;

  public GameMapPanel(GameMap gameMap, Camera camera, CategoryNode root) {
    this.gameMap = gameMap;
    this.camera = camera;
    this.root = root;
    this.imageManager = new ImageManager();
    this.paintGameObject = new PaintGameObject();
    this.setLayout(null);
    setBackground(Color.BLACK);
    if (ENABLE_UI) {
      this.paintUI = new PaintUI(imageManager, this, root);
      paintUI.paint(this.getGraphics());
    } else { // 以下デバッグ用
      this.paintUI = null;
    }
    
    if (INFINITE_RELOAD) {
      Thread reloadThread = new Thread(() -> {
        while (true) {
          try {
            Thread.sleep(1); // 1msごとにリロード
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
          repaint();
        }
      });
      reloadThread.setDaemon(true);
      reloadThread.start();
    }
  }

  /**
   * gameMapの内容を描画する
   * 
   * @param g 描画に使用するGraphicsオブジェクト
   * @implNote 画像ファイルが見つからない場合、"Warning Image not found: imageName.png at (x,y)"
   *           という警告が出力されます。
   * @see GameMap
   */
  int repaintCount = 0;
  int last1SecRepaintCount = 0;
  double lastFps = 0;
  long lastSecondTime = System.currentTimeMillis();

  @Override
  public void paintComponent(Graphics g) {
    if (SHOW_REPAINT_COUNT_AND_FPS) {
      repaintCount++;
      last1SecRepaintCount++;
      long currentTime = System.currentTimeMillis();
      if (currentTime - lastSecondTime >= 1000) {
        lastFps = last1SecRepaintCount;
        lastSecondTime = currentTime;
        last1SecRepaintCount = 0;
        System.out.printf("Repaint Count: %d, Approx FPS: %.2f\n", repaintCount, lastFps);
      }
    }
    super.paintComponent(g);
    // マップの奥(上)から手前(下)に向かって描画する
    for (int z = 0; z < gameMap.getWidth() + gameMap.getHeight(); z++) {
      for (int x = 0; x <= z; x++) {
        int y = z - x;
        if (x >= 0 && x < gameMap.getWidth() && y >= 0 && y < gameMap.getHeight() && isInsideCameraView(x, y)) {
          Point2D.Double pos = new Point2D.Double(x, y);
          paintGameObject.paintTerrain(g, pos, gameMap, camera, imageManager, this);
          paintGameObject.paintBuilding(g, pos, gameMap, camera, imageManager, this);
        }
      }
    }
  }

  boolean isInsideCameraView(int x, int y) {
    Point2D.Double screenPos = camera.isoToScreen(new Point2D.Double(x + 1, y));
    int panelWidth = this.getWidth();
    int panelHeight = this.getHeight();
    double cameraScale = camera.getScale();
    int margin = (int) (100 / (cameraScale * 1.5)) + 50;
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
}

/**
 * ゲームウィンドウを表すクラス
 * ウィンドウサイズ: 640*640
 * タイトル: "Town Planning Game"
 * 
 * @see GameMapPanel
 */
public class GameWindow extends JFrame {
  public <T extends MouseListener & MouseMotionListener & KeyListener> GameWindow(T listener,
      GameMap gameMap, Camera camera, int width, int height, EventBus eventBus) {
    setTitle("Town Planning Game");
    setSize(width, height);
    // GameMap gameMap = generateTestMap();
    GameMapController gameMapController = new GameMapController(camera);
    CategoryNode root = NodeMenuInitializer.setup(gameMapController, gameMap);

    GameMapPanel gameMapPanel = new GameMapPanel(gameMap, camera, root);
    gameMapPanel.addMouseListener(listener);
    gameMapPanel.addMouseMotionListener(listener);
    gameMapPanel.addKeyListener(listener);
    gameMapPanel.setFocusable(true);
    eventBus.subscribe(MapUpdatedEvent.class, event -> {
      gameMapPanel.repaint();
    });
    this.add(gameMapPanel, BorderLayout.CENTER);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(java.awt.event.ComponentEvent e) {
        gameMapPanel.repaintUI();
        // MEMO:ウィンドウリサイズ時の処理を追加する場合はここに記載 Cameraの位置修正とか
      }
    });
    setVisible(true);
  }
}