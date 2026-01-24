package io.github.sasori_256.town_planning.common.ui.main.scene;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

import javax.swing.JPanel;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import java.awt.Graphics;
import java.awt.geom.Point2D;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import io.github.sasori_256.town_planning.common.ui.AnimationManager;
import io.github.sasori_256.town_planning.common.ui.ImageManager;
import io.github.sasori_256.town_planning.common.ui.PaintGameObject;
import io.github.sasori_256.town_planning.common.ui.gameObjectSelect.controller.CategoryNode;
import io.github.sasori_256.town_planning.common.ui.gameObjectSelect.view.PaintObjectSelectUI;
import io.github.sasori_256.town_planning.common.ui.main.GameFlowNavigator;
import io.github.sasori_256.town_planning.common.ui.main.UiRefreshable;
import io.github.sasori_256.town_planning.common.ui.resourceViewer.view.PaintResourceViewerUI;
import io.github.sasori_256.town_planning.entity.Camera;
import io.github.sasori_256.town_planning.entity.building.BuildingType;
import io.github.sasori_256.town_planning.entity.disaster.Disaster;
import io.github.sasori_256.town_planning.entity.model.GameModel;
import io.github.sasori_256.town_planning.entity.resident.Resident;
import io.github.sasori_256.town_planning.entity.resident.ResidentState;
import io.github.sasori_256.town_planning.map.model.GameMap;
import io.github.sasori_256.town_planning.map.model.MapCell;

/**
 * gameMapの内容を描画するクラス
 */
public class GameMapPanel extends JPanel implements UiRefreshable {
  private final GameMap gameMap;
  private final GameModel gameModel;
  private final Camera camera;
  private final CategoryNode root;
  private final ImageManager imageManager;
  private final AnimationManager animationManager;
  private final PaintGameObject paintGameObject;
  private final PaintObjectSelectUI paintObjectSelectUI;
  private final ReadWriteLock stateLock;
  private final GameFlowNavigator navigator;

  /**
   * マップ描画パネルを生成する。
   *
   * @param gameMap   マップ
   * @param gameModel ゲームモデル
   * @param camera    カメラ
   * @param root      ルートカテゴリ
   * @param stateLock 状態ロック
   */
  public GameMapPanel(
      GameMap gameMap,
      GameModel gameModel,
      Camera camera,
      CategoryNode root,
      ReadWriteLock stateLock,
      ImageManager imageManager,
      GameFlowNavigator navigator) {
    this.gameMap = gameMap;
    this.gameModel = gameModel;
    this.camera = camera;
    this.root = root;
    this.imageManager = imageManager;
    this.animationManager = new AnimationManager();
    this.paintGameObject = new PaintGameObject();
    this.stateLock = stateLock;
    this.navigator = navigator;
    this.setLayout(null);
    setBackground(new Color(19, 175, 251)); // 海の色
    this.paintObjectSelectUI = new PaintObjectSelectUI(imageManager, this, root);
    this.add(new PaintResourceViewerUI(gameModel, imageManager, 1.0));
    paintObjectSelectUI.paint();
    setupNavigationBindings();
    revalidate();
    repaint();
  }

  private void setupNavigationBindings() {
    if (navigator == null) {
      return;
    }
    getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
        .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "goTitle");
    getActionMap().put("goTitle", new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        navigator.goToTitle();
      }
    });
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
              paintGameObject.paintBuilding(g, pos, gameMap, camera, imageManager,
                  animationManager, this);
            } else if (group == BuildingType.DrawGroup.ACTOR) {
              actors.add(DrawEntry.forBuilding(pos));
            }
          }
        }
      }

      gameModel.getResidentEntities().forEach(resident -> {
        ResidentState state = resident.getState();
        if (state == ResidentState.AT_HOME) {
          return;
        }
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

      gameModel.getDisasterEntities().forEach(disaster -> {
        Point2D.Double pos = disaster.getPosition();
        int x = (int) Math.floor(pos.getX());
        int y = (int) Math.floor(pos.getY());
        if (!isInsideCameraView(x, y)) {
          return;
        }
        actors.add(DrawEntry.forDisaster(disaster));
      });

      actors.sort(DrawEntry.DEPTH_ORDER);
      for (DrawEntry entry : actors) {
        if (entry.kind == DrawKind.BUILDING_TILE) {
          paintGameObject.paintBuilding(g, entry.pos, gameMap, camera, imageManager,
              animationManager, this);
        } else if (entry.kind == DrawKind.RESIDENT) {
          paintGameObject.paintResident(g, entry.resident, camera, imageManager, animationManager, this);
        } else if (entry.kind == DrawKind.DISASTER) {
          paintGameObject.paintDisaster(g, entry.disaster, camera, imageManager,
              animationManager, this);
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

  /**
   * UIを再描画する。
   */
  @Override
  public void repaintUI() {
    this.paintObjectSelectUI.repaintUI();
  }

  private enum DrawKind {
    BUILDING_TILE,
    RESIDENT,
    DISASTER
  }

  private static final class DrawEntry {
    private static final Comparator<DrawEntry> DEPTH_ORDER = Comparator
        .comparingDouble((DrawEntry entry) -> entry.depth)
        .thenComparingDouble(entry -> entry.y)
        .thenComparingDouble(entry -> entry.x)
        .thenComparingInt(entry -> {
          if (entry.kind == DrawKind.BUILDING_TILE) {
            return 0;
          }
          if (entry.kind == DrawKind.RESIDENT) {
            return 1;
          }
          return 2;
        });

    private final double x;
    private final double y;
    private final double depth;
    private final DrawKind kind;
    private final Point2D.Double pos;
    private final Resident resident;
    private final Disaster disaster;

    private static DrawEntry forBuilding(Point2D.Double pos) {
      return new DrawEntry(DrawKind.BUILDING_TILE, pos, null, null);
    }

    private static DrawEntry forResident(Resident resident) {
      return new DrawEntry(DrawKind.RESIDENT, resident.getPosition(), resident, null);
    }

    private static DrawEntry forDisaster(Disaster disaster) {
      return new DrawEntry(DrawKind.DISASTER, disaster.getPosition(), null, disaster);
    }

    private DrawEntry(DrawKind kind, Point2D.Double pos, Resident resident,
        Disaster disaster) {
      this.kind = kind;
      this.pos = new Point2D.Double(pos.getX(), pos.getY());
      this.x = this.pos.getX();
      this.y = this.pos.getY();
      this.depth = this.x + this.y;
      this.resident = resident;
      this.disaster = disaster;
    }
  }
}
