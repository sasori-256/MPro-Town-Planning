package io.github.sasori_256.town_planning.common.ui;

import java.awt.Graphics;
import java.awt.geom.Point2D;

import javax.swing.JPanel;

import io.github.sasori_256.town_planning.common.ui.ImageManager.ImageStorage;
import io.github.sasori_256.town_planning.entity.Camera;
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
    double shiftX = 0;
    double shiftY = -(imgSize.y / imgSize.x * 2 - 1) * cameraScale / 2 + imgSize.y / 2 * cameraScale;
    return new Point2D.Double(shiftX, shiftY);
  }

  /**
   * 指定された座標の地形を描画する
   * 
   * @param g              グラフィックスコンテキスト
   * @param pos            座標
   * @param gameMap        ゲームマップ
   * @param camera         カメラ
   * @param getImageByName 画像取得関数
   * @param panel          描画対象のパネル
   */
  public void paintTerrain(Graphics g, Point2D.Double pos, GameMap gameMap, Camera camera, ImageManager imageManager,
      JPanel panel) {
    MapCell cell = gameMap.getCell(pos);
    String terrainName = cell.getTerrain().getDisplayName();
    paint(g, pos, terrainName, camera, imageManager, panel);
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
    String buildingName = cell.getBuilding().getType().getImageName();
    if (buildingName.equals("none")) { // 建物がない場合はスキップ
      return;
    }
    paint(g, pos, buildingName, camera, imageManager, panel);
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
   */
  public void paint(Graphics g, Point2D.Double pos, String name, Camera camera, ImageManager imageManager,
      JPanel panel) {
    // 建物または地形の描画
    ImageStorage imageStorage = imageManager.getImageStorage(name);
    if (imageStorage != null) {
      Point2D.Double screenPos = camera.isoToScreen(pos);
      double cameraScale = camera.getScale();
      Point2D.Double posShift = calculateShiftImage(imageStorage.size, cameraScale);
      Point2D.Double imageScale = imageStorage.size;
      int xPos = (int) Math.round(screenPos.x + posShift.x);
      int yPos = (int) Math.round(screenPos.y + posShift.y);
      int width = (int) (imageScale.x * cameraScale);
      int height = (int) (imageScale.y * cameraScale);
      g.drawImage(imageStorage.image, xPos, yPos, width, height, panel);
    }
  }
}
