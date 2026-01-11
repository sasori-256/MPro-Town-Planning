package io.github.sasori_256.town_planning.common.ui;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import javax.swing.JPanel;
import io.github.sasori_256.town_planning.common.ui.ImageManager.ImageStorage;
import io.github.sasori_256.town_planning.entity.Camera;
import io.github.sasori_256.town_planning.entity.resident.Resident;
import io.github.sasori_256.town_planning.map.model.GameMap;
import io.github.sasori_256.town_planning.map.model.MapCell;

/**
 * 建物を描画するためのクラス
 */
public class PaintGameObject {

  /**
   * 垂直方向の高さを持つ画像のシフト量を計算する
   *
   * @param imgSize     画像の元のサイズ
   * @param cameraScale カメラのスケール
   * @return シフト量
   */
  public static Point2D.Double calculateShiftImage(Point2D.Double imgSize, double cameraScale) {
    if (imgSize == null || imgSize.x == 0 || imgSize.y == 0) { // 0除算防止
      System.err.println("Warning: Invalid image size for shift calculation.");
      return new Point2D.Double(0, 0);
    }
    double shiftX = -imgSize.x / 2 * cameraScale;
    double shiftY = -imgSize.y / 2 * cameraScale - (imgSize.y / imgSize.x - 0.5)
        * cameraScale * imgSize.x / 2;
    // imageSize / 2 * cameraScale は画像の中心を基準にするためのシフト量
    // (imageSize.y / imageSize.x - 0.5) * cameraScale * imageSize.x / 2
    // は垂直方向の高さを考慮したシフト量 これにより、画像の(視覚的な)底面の重心が基準点に一致するようになる
    return new Point2D.Double(shiftX, shiftY);
  }

  /**
   * 指定された座標の地形を描画する
   *
   * @param g            グラフィックスコンテキスト
   * @param pos          座標
   * @param gameMap      ゲームマップ
   * @param camera       カメラ
   * @param imageManager 画像取得用マネージャー
   * @param panel        描画対象のパネル
   */
  public void paintTerrain(Graphics g, Point2D.Double pos, GameMap gameMap, Camera camera, ImageManager imageManager,
      JPanel panel) {
    MapCell cell = gameMap.getCell(pos);
    String terrainName = cell.getTerrain().getDisplayName();
    paint(g, pos, terrainName, camera, imageManager, panel, true);
  }

  /**
   * 指定された座標の建物を描画する
   *
   * @param g            グラフィックスコンテキスト
   * @param pos          座標
   * @param gameMap      ゲームマップ
   * @param camera       カメラ
   * @param imageManager 画像取得用マネージャー
   * @param panel        描画対象のパネル
   */
  public void paintBuilding(Graphics g, Point2D.Double pos, GameMap gameMap, Camera camera, ImageManager imageManager,
      JPanel panel) {
    MapCell cell = gameMap.getCell(pos);
    if (cell.getBuilding() == null) {
      return;
    }
    String buildingName = cell.getBuilding().getType().getImageName(cell.getLocalX(), cell.getLocalY());
    if (buildingName == null) {
      return;
    }
    paint(g, pos, buildingName, camera, imageManager, panel, true);
  }

  /**
   * 指定された住民を描画する
   *
   * @param g            グラフィックスコンテキスト
   * @param resident     描画対象の住民
   * @param camera       カメラ
   * @param imageManager 画像取得用マネージャー
   * @param panel        描画対象のパネル
   */
  public void paintResident(Graphics g, Resident resident, Camera camera, ImageManager imageManager, JPanel panel) {
    if (resident == null || resident.getType() == null) {
      return;
    }
    String imageName = resident.getType().getImageName();
    if (imageName == null) {
      return;
    }
    Point2D.Double pos = resident.getPosition();
    paint(g, pos, imageName, camera, imageManager, panel, false);
  }

  /**
   * 指定された座標に指定された画像を描画する
   *
   * @param g            グラフィックスコンテキスト
   * @param pos          座標
   * @param name         建物の名前
   * @param camera       カメラ
   * @param imageManager 画像取得用マネージャー
   * @param panel        描画対象のパネル
   * @param snapToGrid   座標をグリッド中央に丸めるかの真偽値
   */
  private void paint(Graphics g, Point2D.Double pos, String name, Camera camera,
      ImageManager imageManager, JPanel panel, boolean snapToGrid) {
    Graphics2D g2d = (Graphics2D) g;
    // 建物または地形の描画
    ImageStorage imageStorage = imageManager.getImageStorage(name);
    if (imageStorage != null) {
      Point2D.Double renderPos = pos;
      if (snapToGrid) {
        renderPos = new Point2D.Double(Math.round(pos.x), Math.round(pos.y));
      }
      Point2D.Double screenPos = camera.isoToScreen(renderPos);
      double cameraScale = camera.getScale();
      Point2D.Double posShift = calculateShiftImage(imageStorage.size, cameraScale);
      Point2D.Double imageScale = imageStorage.size;
      int xPos = (int) Math.round(screenPos.x + posShift.x);
      int yPos = (int) Math.round(screenPos.y + posShift.y);
      int width = (int) (imageScale.x * cameraScale);
      int height = (int) (imageScale.y * cameraScale);
      g2d.drawImage(imageStorage.image, xPos, yPos, width, height, panel);
    }
  }
}
