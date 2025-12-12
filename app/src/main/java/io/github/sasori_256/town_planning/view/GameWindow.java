package io.github.sasori_256.town_planning.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.geom.Point2D;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JPanel;

class Camera {
    public static double scale = 32;
    public static double posX = 320;
    public static double posY = 160;
}

enum TerrainType {
  GRASS("grass", true, true),
  SEA("sea", false, false),
  MOUNTAIN("mountain", false, false),
  COAST_YP("coast_Yp", false, false);

  final String displayName;
  final boolean isWalkable;
  final boolean isBuildable;
  TerrainType(String displayName, boolean isWalkable, boolean isBuildable){
    this.displayName = displayName;
    this.isWalkable = isWalkable;
    this.isBuildable = isBuildable;
  }
  public String getDisplayName() {
    return displayName;
  }
  public boolean isWalkable() {
    return isWalkable;
  }
  public boolean isBuildable() {
    return isBuildable;
  }

}
enum BuildingGameObject {
  NONE("none", 0),
  HOUSE("house", 100),
  GRAVEL_ROAD_XPXM("gravel_road_XpXm", 10);

  final String displayName;
  final int cost;
  BuildingGameObject(String displayName, int cost){
    this.displayName = displayName;
    this.cost = cost;
  }
  public String getDisplayName() {
    return displayName;
  }
  public int getCost() {
    return cost;
  }
}

class MapCell {
  public TerrainType terrain;
  public BuildingGameObject building;
  public MapCell(TerrainType initTerrain, BuildingGameObject initBuilding){
    this.terrain = initTerrain;
    this.building = initBuilding;
  }
}

class GameMap {
  public final int width;
  public final int height;
  public final MapCell[][] cells;

  public GameMap(int width, int height){
    this.width = width;
    this.height = height;
    this.cells = new MapCell[height][width];
    for(int y=0; y<height; y++){
      for(int x=0; x<width; x++){
        cells[y][x] = new MapCell(TerrainType.GRASS, BuildingGameObject.NONE);
      }
    }
  }
}
/**
 * gameMapの内容を描画するクラス
 */
class GameMapPanel extends JPanel{
  private final GameMap gameMap;
  public GameMapPanel(GameMap gameMap) {
    this.gameMap = gameMap;
    setBackground(Color.BLACK);
  }
  /**
   * isometric座標をスクリーン座標に変換する
   * @param isoPos map上の座標
   * @return スクリーン座標
   */
  Point2D.Double isometricPosToScreenPos(Point2D.Double isoPos) {
    double screenX = (isoPos.x-isoPos.y-1)*Camera.scale + Camera.posX;
    double screenY = (isoPos.x+isoPos.y)*Camera.scale/2 + Camera.posY;
    return new Point2D.Double(screenX, screenY);
  }
  /**
   * 垂直方向の高さを持つ画像のシフト量を計算する
   * @param imgPos 画像の元の中心位置
   * @param imgSize 画像の元のサイズ
   * @return シフト量
   */
  Point2D.Double calculateShiftImage(Point2D.Double imgPos, Point2D.Double imgSize) {
    double shiftX = imgPos.x;
    double aspectRatio = imgSize.y / imgSize.x;
    double shiftY = imgPos.y - (aspectRatio*2 - 1) * Camera.scale / 2;
    return new Point2D.Double(shiftX, shiftY);
  }
  /**
   * 画像のスケールを計算する
   * @return 画像のスケール
   */
  Point2D.Double calculateImageScale() {
    double imageWidth = Camera.scale * 2;
    double imageHeight = Camera.scale;
    return new Point2D.Double(imageWidth, imageHeight);
  }
  /**
   * gameMapの内容を描画する
   * @param g 描画に使用するGraphicsオブジェクト
   * @implNote 画像ファイルが見つからない場合、"Warning Image not found: imageName.png at (x,y)" という警告が出力されます。
   * @see GameMap
   */
  @Override
  public void paintComponent(Graphics g) {
    super.paintComponent(g);
    for (int y = 0; y < gameMap.height; y++) {
      for (int x = 0; x < gameMap.width; x++) {
        MapCell cell = gameMap.cells[y][x];
        Point2D.Double pos = new Point2D.Double(x, y);
        Point2D.Double screenPos;
        Point2D.Double shiftedScreenPos;
        Point2D.Double imageScale;
        Point2D.Double imageSize;
        Image img;
        String terrainName = cell.terrain.getDisplayName();
        if(new File(terrainName + ".png").exists()){
          img = Toolkit.getDefaultToolkit().getImage(terrainName + ".png");
          screenPos = isometricPosToScreenPos(pos);
          g.drawImage(img, (int)screenPos.x, (int)screenPos.y, (int)(Camera.scale*2), (int)(Camera.scale), this);
        }else{
            System.err.println("Warning: Image not found: " + terrainName + ".png at (" + x + ", " + y + ")");
            img = Toolkit.getDefaultToolkit().getImage("error_terrain.png");
            screenPos = isometricPosToScreenPos(pos);
            g.drawImage(img, (int)screenPos.x, (int)screenPos.y, (int)(Camera.scale*2), (int)(Camera.scale), this);
        }
        String buildingName = cell.building.getDisplayName();
        if("none".equals(buildingName)) continue;
        if(new File(buildingName + ".png").exists()){
          img = Toolkit.getDefaultToolkit().getImage(buildingName + ".png");
          screenPos = isometricPosToScreenPos(pos);
          imageSize = new Point2D.Double(img.getWidth(this), img.getHeight(this));
          shiftedScreenPos = calculateShiftImage(screenPos, imageSize);
          imageScale = calculateImageScale();
          g.drawImage(img, (int)shiftedScreenPos.x, (int)shiftedScreenPos.y, (int)(imageScale.x), (int)(imageScale.y), this);
        }else{
            System.err.println("Warning: Image not found: " + buildingName + ".png at (" + x + ", " + y + ")");
            img = Toolkit.getDefaultToolkit().getImage("error_building.png");
            screenPos = isometricPosToScreenPos(pos);
            imageScale = calculateImageScale();
            g.drawImage(img, (int)screenPos.x, (int)screenPos.y, (int)(imageScale.x), (int)(imageScale.y), this);
        }
      }
    }
  }
}
/**
 * ゲームウィンドウを表すクラス
 * ウィンドウサイズ: 640*640
 * タイトル: "Town Planning Game"
 * @see GameMapPanel
 */
public class GameWindow extends JFrame {
  private GameMap generateTestMap() {
    GameMap testMap = new GameMap(10, 10);
    for(int x=0; x<10; x++){
      testMap.cells[0][x].terrain = TerrainType.SEA;
      testMap.cells[1][x].terrain = TerrainType.SEA;
      testMap.cells[8][x].terrain = TerrainType.COAST_YP;
      testMap.cells[9][x].terrain = TerrainType.SEA;
      testMap.cells[6][x].building = BuildingGameObject.GRAVEL_ROAD_XPXM;
    }

    return testMap;
  }
  public GameWindow() {
    setTitle("Town Planning Game");
    setSize(640, 640);
    GameMap gameMap = generateTestMap();
    GameMapPanel gameMapPanel = new GameMapPanel(gameMap);
    this.add(gameMapPanel, BorderLayout.CENTER);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setVisible(true);
  }
  public static void main(String[] args) {
    new GameWindow();
  }
}