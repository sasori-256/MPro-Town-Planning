package io.github.sasori_256.town_planning.common.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

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
  private static final class ImageStorage {
    final String name;
    final Image image;
    Point2D.Double size;
    
    public void loadSize() {
      this.size.x = image.getWidth(null);
      this.size.y = image.getHeight(null);
    }
    
    ImageStorage(String name, Image image) {
      this.name = name;
      this.image = image;
      this.size = new Point2D.Double(32, 32); // 仮の初期値
      loadSize();
    }
  }
  private final Map<String, ImageStorage> imageStorages = new HashMap<>();

  /**
   * 配列にすべての画像を読み込み、imageStoragesに格納する
   * @implNote 画像は "src/main/resources/images/" フォルダから読み込まれる
   * @see ImageStorage
   */
  private void loadImages() {
    String PATH = getClass().getClassLoader().getResource("images").getPath();
    
    File dir = new File(PATH);
    File[] files = dir.listFiles((d, name) -> name.endsWith(".png"));
    MediaTracker tracker = new MediaTracker(this);
    if (files != null) {
      for (int i = 0; i < files.length; i++) {
        File file = files[i];
        String imageName = file.getName().replaceFirst("[.][^.]+$", "").toLowerCase(); // 拡張子を除去し、全部小文字にする
        System.out.println("Loading image: " + file.getName());
        Image img = Toolkit.getDefaultToolkit().getImage(file.getAbsolutePath());
        imageStorages.put(imageName, new ImageStorage(imageName, img));
        tracker.addImage(img, i);
        try {
          tracker.waitForID(i);
        } catch (InterruptedException e) {
          System.err.println("Error loading image: " + file.getName());
        }

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
    name = name.toLowerCase(); // 名前を小文字に変換して統一
    if (imageStorages.get(name) != null) {
      return imageStorages.get(name);
    }
    if (name.equals("error_terrain") || name.equals("error_building")) {
      return null; // エラー画像自体が見つからない場合はnullを返す
    }
    return getImageByName("error_terrain"); // 画像が見つからない場合はエラー画像を返す
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
    double imageWidth = 32 * cameraScale * 2;
    double imageHeight = 32 * cameraScale;
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