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
 * gameMapの内容を描画するクラス
 */
class GameMapPanel extends JPanel {
  /**
   * 画像を格納するための内部クラス
   */
  private static class ImageStorage {
    final String name;
    final Image image;
    final Point2D.Double size;
    ImageStorage(String name, Image image) {
      this.name = name;
      this.image = image;
      this.size = new Point2D.Double(image.getWidth(null), image.getHeight(null));
    }
  }
  private static final int MAX_IMAGES = 500;
  private final ImageStorage[] imageStorages = new ImageStorage[MAX_IMAGES];
  private int imageCount = 0;

  /**
   * 配列にすべての画像を読み込み、imageStoragesに格納する
   * @implNote 画像は "src/main/resources/images/" フォルダから読み込まれる
   * @see ImageStorage
   */
  private void loadImages() {
    final String PATH = "src/main/resources/images/"; // 画像のパス
    File dir = new File(PATH);
    File[] files = dir.listFiles((d, name) -> name.endsWith(".png"));
    if (files != null) {
      for (File file : files) {
        if (imageCount >= MAX_IMAGES) {
          System.err.println("Warning: Maximum image storage reached. Some images may not be loaded.");
          break;
        }
        String imageName = file.getName().replaceFirst("[.][^.]+$", ""); // 拡張子を除去
        Image img = Toolkit.getDefaultToolkit().getImage(file.getPath());
        imageStorages[imageCount] = new ImageStorage(imageName, img);
        imageCount++;
      }
    }
  }

  /**
   * 名前から画像を取得する。見つからなければerror_terrainを、それもなければnullを返す
   * @param name 画像の名前
   * @return 画像のImageStorageオブジェクト、見つからなければエラー画像のImageStorageオブジェクト
   * @see ImageStorage
   */
  private ImageStorage getImageByName(String name) {
    for (int i = 0; i < imageCount; i++) {
      if (imageStorages[i].name.equals(name)) {
        return imageStorages[i];
      }
    }
    if(name.equals("error_building") || name.equals("error_terrain")){
      System.err.println("Error: Error image not found: " + name + ".png");
      return imageStorages[0]; // エラー画像が見つからない場合は最初の画像を返す
    } else { 
      return getImageByName("error_terrain"); // 画像が見つからない場合はエラー画像を返す
    }
  }

  private final GameMap gameMap;
  private final Camera camera;
  public GameMapPanel(GameMap gameMap, Camera camera) {
    this.gameMap = gameMap;
    this.camera = camera;
    setBackground(Color.BLACK);
    loadImages(); // 画像の事前読み込み
  }
  /**
   * 垂直方向の高さを持つ画像のシフト量を計算する
   * @param imgPos 画像の元の中心位置
   * @param imgSize 画像の元のサイズ
   * @return シフト量
   */
  Point2D.Double calculateShiftImage(Point2D.Double imgPos, Point2D.Double imgSize, double cameraScale) {
    double shiftX = imgPos.x;
    double aspectRatio = imgSize.y / imgSize.x;
    double shiftY = imgPos.y - (aspectRatio*2 - 1) * cameraScale / 2;
    return new Point2D.Double(shiftX, shiftY);
  }
  /**
   * 画像のスケールを計算する
   * @return 画像のスケール
   */
  Point2D.Double calculateImageScale(double cameraScale) {
    double imageWidth = cameraScale * 2;
    double imageHeight = cameraScale;
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
    for (int y = 0; y < gameMap.getHeight(); y++) {
      for (int x = 0; x < gameMap.getWidth(); x++) {
        Point2D.Double pos = new Point2D.Double(x, y);
        MapCell cell = gameMap.getCell(new Point2D.Double(x, y));
        Point2D.Double screenPos = camera.isoToScreen(pos);
        Point2D.Double imageScale = calculateImageScale(camera.getScale());
        // 地形の描画
        String terrainName = cell.getTerrain().getDisplayName();
        ImageStorage terrainImage = getImageByName(terrainName);
        if("error_terrain".equals(terrainImage.name)){
          System.err.println("Warning: Image not found: " + terrainName + ".png at (" + x + ", " + y + ")");
        }
        g.drawImage(terrainImage.image, (int)screenPos.x, (int)screenPos.y, (int)(imageScale.x), (int)(imageScale.y), this);
        // 建物の描画
        String buildingName = cell.getBuilding().getType().getImageName();
        if(buildingName.equals("none")){ // 建物がない場合はスキップ
          continue;
        }
        ImageStorage buildingImage = getImageByName(buildingName);
        Point2D.Double imageSize = buildingImage.size;
        Point2D.Double shiftedScreenPos = calculateShiftImage(screenPos, imageSize, camera.getScale());
        if("error_building".equals(buildingImage.name)){
          System.err.println("Warning: Image not found: " + buildingName + ".png at (" + x + ", " + y + ")");
        }
        g.drawImage(buildingImage.image, (int)shiftedScreenPos.x, (int)shiftedScreenPos.y, (int)(imageScale.x), (int)(imageScale.y), this);
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
  public GameWindow(MouseListener listener, GameMap gameMap, Camera camera, int width, int height) {
    addMouseListener(listener);
    setTitle("Town Planning Game");
    setSize(width, height);
    //GameMap gameMap = generateTestMap();
    GameMapPanel gameMapPanel = new GameMapPanel(gameMap, camera);
    this.add(gameMapPanel, BorderLayout.CENTER);
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setVisible(true);
  }
}