package io.github.sasori_256.town_planning.common.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JPanel;

import io.github.sasori_256.town_planning.gameobject.Camera;
import io.github.sasori_256.town_planning.map.model.GameMap;
import io.github.sasori_256.town_planning.map.model.MapCell;

/**
 * GameMapの内容を描画するクラス
 */
class GameMapPanel extends JPanel {
  private final GameMap gameMap;
  private final Camera camera;

  public GameMapPanel(GameMap gameMap, Camera camera) {
    this.gameMap = gameMap;
    this.camera = camera;
    setBackground(Color.BLACK);
  }

  /**
   * 垂直方向の高さを持つ画像のシフト量を計算する
   * 
   * @param imgPos  画像の元の中心位置
   * @param imgSize 画像の元のサイズ
   * @return シフト量
   */
  Point2D.Double calculateShiftImage(Point2D.Double imgPos, Point2D.Double imgSize) {
    double shiftX = imgPos.x;
    double aspectRatio = imgSize.y / imgSize.x;
    double shiftY = imgPos.y - (aspectRatio * 2 - 1) * camera.getScale() / 2;
    return new Point2D.Double(shiftX, shiftY);
  }

  /**
   * 画像のスケールを計算する
   * 
   * @return 画像のスケール
   */
  Point2D.Double calculateImageScale() {
    double imageWidth = camera.getScale() * 2;
    double imageHeight = camera.getScale();
    return new Point2D.Double(imageWidth, imageHeight);
  }

  /**
   * gameMapの内容を描画する
   * 
   * @param g 描画に使用するGraphicsオブジェクト
   * @implNote 画像ファイルが見つからない場合、"Warning Image not found: imageName.png at (x,y)" という警告が出力されます。
   * @see GameMap
   */
  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    final String PATH = "src/main/resources/images/";
    final String ERROR_TERRAIN_IMAGE = "error_terrain.png";
    final String ERROR_BUILDING_IMAGE = "error_building.png";

    for (int y = 0; y < gameMap.getHeight(); y++) {
      for (int x = 0; x < gameMap.getWidth(); x++) {
        MapCell cell = gameMap.getCell(new Point2D.Double(x, y));
        Point2D.Double pos = new Point2D.Double(x, y);
        Point2D.Double screenPos;
        Point2D.Double shiftedScreenPos;
        Point2D.Double imageScale;
        Point2D.Double imageSize;
        Image img;

        // 地形の描画
        // 画像ファイルが存在する場合はそれを使用し、存在しない場合はエラーメッセージを表示して代替画像を使用する
        // TODO: 画像のプリロードを検討
        // TODO: isoToScreenの引数型をPoint2D.Doubleかx, yの形式のどちらかに統一する
        // TODO: camera.scaleの内容をどうにかする
        // TODO: layerIndexに従った描画順序の実装
        String terrainName = cell.getTerrain().getDisplayName();
        String terrainImageName = terrainName + ".png";
        if (new File(PATH + terrainImageName).exists()) {
          img = Toolkit.getDefaultToolkit().getImage(PATH + terrainImageName);
          screenPos = camera.isoToScreen(pos);
          g.drawImage(img, (int) screenPos.x, (int) screenPos.y, (int) (camera.getScale() * 2),
              (int) (camera.getScale()), this);
        } else {
          System.err.println("Warning: Image not found: " + terrainImageName + " at (" + x + ", " + y + ")");
          img = Toolkit.getDefaultToolkit().getImage(PATH + ERROR_TERRAIN_IMAGE);
          screenPos = camera.isoToScreen(pos);
          g.drawImage(img, (int) screenPos.x, (int) screenPos.y, (int) (camera.getScale() * 2),
              (int) (camera.getScale()), this);
        }

        // 建物の描画
        // 建物が存在しない場合はスキップ
        // 画像ファイルが存在する場合はそれを使用し、存在しない場合はエラーメッセージを表示して代替画像を使用する
        // TODO: 画像のプリロードを検討
        // TODO: isoToScreenの引数型をPoint2D.Doubleかx, yの形式のどちらかに統一する
        // TODO: camera.scaleの内容をどうにかする
        // TODO: layerIndexに従った描画順序の実装
        String buildingName = cell.getBuilding().getType().getDisplayName();
        String buildingImageName = cell.getBuilding().getType().getImageName() + ".png";
        if (buildingName.equals("none")) {
          continue;
        }
        if (new File(PATH + buildingImageName).exists()) {
          img = Toolkit.getDefaultToolkit().getImage(PATH + buildingImageName);
          screenPos = camera.isoToScreen(pos);
          imageSize = new Point2D.Double(img.getWidth(this), img.getHeight(this));
          shiftedScreenPos = calculateShiftImage(screenPos, imageSize);
          imageScale = calculateImageScale();
          g.drawImage(img, (int) shiftedScreenPos.x, (int) shiftedScreenPos.y, (int) (imageScale.x),
              (int) (imageScale.y), this);
        } else {
          System.err.println("Warning: Image not found: " + buildingImageName + " at (" + x + ", " + y + ")");
          img = Toolkit.getDefaultToolkit().getImage(ERROR_BUILDING_IMAGE);
          screenPos = camera.isoToScreen(pos);
          imageScale = calculateImageScale();
          g.drawImage(img, (int) screenPos.x, (int) screenPos.y, (int) (imageScale.x), (int) (imageScale.y), this);
        }
      }
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
  public GameWindow(MouseListener mouseListener, GameMap gameMap, Camera camera, int width, int height) {
    setTitle("Town Planning Game");
    setSize(width, height);
    // マウス判定はGameWindowで受け取り、必要に応じてGameMapPanelに伝える
    // Listenerの登録はGameWindowで行うが、Listener自体は外部から渡す形にする
    this.addMouseListener(mouseListener);

    GameMapPanel gameMapPanel = new GameMapPanel(gameMap, camera);

    // BorderLayoutの中央にGameMapPanelを配置
    // TODO: Layout管理の改善検討
    this.add(gameMapPanel, BorderLayout.CENTER);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setVisible(true);
  }
}
