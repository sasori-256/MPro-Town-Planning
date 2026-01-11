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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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
import io.github.sasori_256.town_planning.entity.building.BuildingType;
import io.github.sasori_256.town_planning.entity.model.GameModel;
import io.github.sasori_256.town_planning.entity.resident.Resident;
import io.github.sasori_256.town_planning.map.controller.GameMapController;
import io.github.sasori_256.town_planning.map.model.GameMap;
import io.github.sasori_256.town_planning.map.model.MapCell;

/**
 * gameMapの内容を描画するクラス
 */
class GameMapPanel extends JPanel {

  private final GameMap gameMap;
  private final GameModel gameModel;
  private final Camera camera;
  private final CategoryNode root;
  private final ImageManager imageManager;
  private final AnimationManager animationManager;
  private final PaintGameObject paintGameObject;
  private final PaintUI paintUI;
  private final ReadWriteLock stateLock;

  public GameMapPanel(GameMap gameMap, GameModel gameModel, Camera camera, CategoryNode root,
      ReadWriteLock stateLock) {
    this.gameMap = gameMap;
    this.gameModel = gameModel;
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
        animationManager.setBounds(0, 0, getWidth(), getHeight());
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
      int maxZ = gameMap.getWidth() + gameMap.getHeight();

      // 地形と床系タイルを描画し、アクタ系タイルを収集
      List<DrawEntry> actors = new ArrayList<>();
      for (int z = 0; z < maxZ; z++) {
        for (int x = 0; x <= z; x++) {
          int y = z - x;
          if (x < gameMap.getWidth() && y < gameMap.getHeight() && isInsideCameraView(x, y)) {
            Point2D.Double pos = new Point2D.Double(x, y);
            paintGameObject.paintTerrain(g, pos, gameMap, camera, imageManager, this);

            MapCell cell = gameMap.getCell(pos);
            if (cell.getBuilding() == null) {
              continue;
            }
            BuildingType.DrawGroup group = cell.getBuilding().getType()
                .getDrawGroup(cell.getLocalX(), cell.getLocalY());
            if (group == BuildingType.DrawGroup.FLOOR) {
              paintGameObject.paintBuilding(g, pos, gameMap, camera, imageManager, this);
            } else if (group == BuildingType.DrawGroup.ACTOR) {
              actors.add(DrawEntry.forBuilding(pos));
            }
          }
        }
      }

      gameModel.getResidentEntities().forEach(resident -> {
        Point2D.Double pos = resident.getPosition();
        int x = (int) Math.floor(pos.getX());
        int y = (int) Math.floor(pos.getY());
        if (x < 0 || y < 0 || x >= gameMap.getWidth() || y >= gameMap.getHeight()) {
          return;
        }
        if (!isInsideCameraView(x, y)) {
          return;
        }
        actors.add(DrawEntry.forResident(resident));
      });

      actors.sort(DrawEntry.DEPTH_ORDER);
      for (DrawEntry entry : actors) {
        if (entry.kind == DrawKind.BUILDING_TILE) {
          paintGameObject.paintBuilding(g, entry.pos, gameMap, camera, imageManager, this);
        } else if (entry.kind == DrawKind.RESIDENT) {
          paintGameObject.paintResident(g, entry.resident, camera, imageManager, this);
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

  private enum DrawKind {
    BUILDING_TILE,
    RESIDENT
  }

  private static final class DrawEntry {
    private static final Comparator<DrawEntry> DEPTH_ORDER = Comparator
        .comparingDouble((DrawEntry entry) -> entry.depth)
        .thenComparingDouble(entry -> entry.y)
        .thenComparingDouble(entry -> entry.x)
        .thenComparingInt(entry -> entry.kind == DrawKind.BUILDING_TILE ? 0 : 1);

    private final double x;
    private final double y;
    private final double depth;
    private final DrawKind kind;
    private final Point2D.Double pos;
    private final Resident resident;

    private static DrawEntry forBuilding(Point2D.Double pos) {
      return new DrawEntry(DrawKind.BUILDING_TILE, pos, null);
    }

    private static DrawEntry forResident(Resident resident) {
      return new DrawEntry(DrawKind.RESIDENT, resident.getPosition(), resident);
    }

    private DrawEntry(DrawKind kind, Point2D.Double pos, Resident resident) {
      this.kind = kind;
      this.pos = new Point2D.Double(pos.getX(), pos.getY());
      this.x = this.pos.getX();
      this.y = this.pos.getY();
      this.depth = this.x + this.y;
      this.resident = resident;
    }
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
    CategoryNode root = NodeMenuInitializer.setup(gameMapController, gameMap);

    GameMapPanel gameMapPanel = new GameMapPanel(gameMap, gameModel, camera, root, stateLock);
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
