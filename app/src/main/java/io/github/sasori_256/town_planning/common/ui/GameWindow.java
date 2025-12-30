package io.github.sasori_256.town_planning.common.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ComponentAdapter;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
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
    this.paintUI = new PaintUI();

    setBackground(Color.BLACK);
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
    for (int y = 0; y < gameMap.getHeight(); y++) {
      for (int x = 0; x < gameMap.getWidth(); x++) {
        Point2D.Double pos = new Point2D.Double(x, y);
        paintGameObject.paintTerrain(g, pos, gameMap, camera, imageManager, this);
      }
    }
    for (int y = 0; y < gameMap.getHeight(); y++) {
      for (int x = 0; x < gameMap.getWidth(); x++) {
        Point2D.Double pos = new Point2D.Double(x, y);
        paintGameObject.paintBuilding(g, pos, gameMap, camera, imageManager, this);
      }
    }
    paintUI.paint(g, root, 1.0, imageManager, this);

  }

  public void repaintUI() {
    this.paintUI.repaintUI(this);
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
  public <T extends MouseListener & MouseMotionListener & KeyListener> GameWindow(T listener, GameMap gameMap,
      Camera camera, int width, int height, EventBus eventBus) {
    addMouseListener(listener);
    addKeyListener(listener);
    addMouseMotionListener(listener);
    setTitle("Town Planning Game");
    setSize(width, height);
    // GameMap gameMap = generateTestMap();
    GameMapController gameMapController = new GameMapController(camera);
    CategoryNode root = NodeMenuInitializer.setup(gameMapController, gameMap);

    GameMapPanel gameMapPanel = new GameMapPanel(gameMap, camera, root);
    eventBus.subscribe(MapUpdatedEvent.class, event -> {
      gameMapPanel.repaint();
    });
    this.add(gameMapPanel, BorderLayout.CENTER);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    this.addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(java.awt.event.ComponentEvent e) {
        gameMapPanel.repaintUI();
      }
    });
    setVisible(true);
  }
}