package io.github.sasori_256.town_planning.common.ui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JPanel;
import io.github.sasori_256.town_planning.common.ui.ImageManager.ImageStorage;
import io.github.sasori_256.town_planning.entity.Camera;
import io.github.sasori_256.town_planning.entity.building.Building;
import io.github.sasori_256.town_planning.entity.disaster.Disaster;
import io.github.sasori_256.town_planning.entity.resident.Resident;
import io.github.sasori_256.town_planning.entity.resident.ResidentState;
import io.github.sasori_256.town_planning.map.model.GameMap;
import io.github.sasori_256.town_planning.map.model.MapCell;

/**
 * 建物を描画するためのクラス
 */
public class PaintGameObject {
  private static final double DEAD_ROTATION_DEGREES = 90.0;
  private static final float DEAD_HUE_SHIFT = 0.5f;
  private final Map<String, BufferedImage> deadTintCache = new HashMap<>();
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
    // (imageSize.y / imageSize.x - 0.5) * cameraScale * imgSize.x / 2
    // は垂直方向の高さを考慮したシフト量 これにより、画像の(視覚的な)底面の重心
    // が基準点に一致するようになる
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
  public void paintTerrain(Graphics g, Point2D.Double pos, GameMap gameMap, Camera camera,
      ImageManager imageManager, JPanel panel) {
    MapCell cell = gameMap.getCell(pos);
    String terrainName = cell.getTerrain().getDisplayName();
    paint(g, pos, terrainName, camera, imageManager, panel, true);
  }

  /**
   * 指定された座標の建物を描画する
   *
   * @param g                グラフィックスコンテキスト
   * @param pos              座標
   * @param gameMap          ゲームマップ
   * @param camera           カメラ
   * @param imageManager     画像取得用マネージャー
   * @param animationManager アニメーション取得用マネージャー
   * @param panel            描画対象のパネル
   */
  public void paintBuilding(Graphics g, Point2D.Double pos, GameMap gameMap, Camera camera,
      ImageManager imageManager, AnimationManager animationManager, JPanel panel) {
    MapCell cell = gameMap.getCell(pos);
    if (cell.getBuilding() == null) {
      return;
    }
    Building building = cell.getBuilding();
    String animationName = building.getType().getAnimationName(cell.getLocalX(), cell.getLocalY());
    if (animationManager != null && animationName != null) {
      int frameIndex = building.getAnimationFrameIndex(cell.getLocalX(), cell.getLocalY());
      boolean loop = building.getType().isAnimationLoop(cell.getLocalX(), cell.getLocalY());
      BufferedImage frame = animationManager.getFrame(animationName, frameIndex, loop);
      if (frame != null) {
        paintImage(g, pos, frame, camera, panel, true);
        return;
      }
    }

    String buildingName = building.getType().getImageName(cell.getLocalX(), cell.getLocalY());
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
  public void paintResident(Graphics g, Resident resident, Camera camera, ImageManager imageManager,
      JPanel panel) {
    if (resident == null || resident.getType() == null) {
      return;
    }
    String imageName = resident.getType().getImageName();
    if (imageName == null) {
      return;
    }
    ImageStorage imageStorage = imageManager.getImageStorage(imageName);
    if (imageStorage == null || imageStorage.image == null) {
      return;
    }
    Point2D.Double pos = resident.getPosition();
    if (resident.getState() == ResidentState.DEAD) {
      paintDeadResident((Graphics2D) g, pos, imageStorage, imageName, camera, panel,
          resident.getDeathAnimationProgress());
      return;
    }
    // Residents move with sub-tile positions, so don't snap to grid.
    paintImage(g, pos, imageStorage.image, camera, panel, false);
  }

  /**
   * 指定された災害を描画する
   *
   * @param g                グラフィックスコンテキスト
   * @param disaster         描画対象の災害
   * @param camera           カメラ
   * @param imageManager     画像取得用マネージャー
   * @param animationManager アニメーション取得用マネージャー
   * @param panel            描画対象のパネル
   */
  public void paintDisaster(Graphics g, Disaster disaster, Camera camera, ImageManager imageManager,
      AnimationManager animationManager, JPanel panel) {
    if (disaster == null || disaster.getType() == null) {
      return;
    }
    String animationName = disaster.getAnimationName();
    if (animationManager != null && animationName != null) {
      BufferedImage frame = animationManager.getFrame(animationName, disaster.getAnimationFrameIndex(),
          disaster.isAnimationLoop());
      if (frame != null) {
        paintImage(g, disaster.getPosition(), frame, camera, panel, false);
        return;
      }
    }
    String imageName = disaster.getType().getImageName();
    if (imageName == null) {
      return;
    }
    paint(g, disaster.getPosition(), imageName, camera, imageManager, panel, false);
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

  private void paintImage(Graphics g, Point2D.Double pos, BufferedImage image, Camera camera,
      JPanel panel, boolean snapToGrid) {
    if (image == null) {
      return;
    }
    Graphics2D g2d = (Graphics2D) g;
    Point2D.Double renderPos = pos;
    if (snapToGrid) {
      renderPos = new Point2D.Double(Math.round(pos.x), Math.round(pos.y));
    }
    Point2D.Double screenPos = camera.isoToScreen(renderPos);
    double cameraScale = camera.getScale();
    Point2D.Double imageSize = new Point2D.Double(image.getWidth(), image.getHeight());
    Point2D.Double posShift = calculateShiftImage(imageSize, cameraScale);
    int xPos = (int) Math.round(screenPos.x + posShift.x);
    int yPos = (int) Math.round(screenPos.y + posShift.y);
    int width = (int) (imageSize.x * cameraScale);
    int height = (int) (imageSize.y * cameraScale);
    g2d.drawImage(image, xPos, yPos, width, height, panel);
  }

  private void paintDeadResident(Graphics2D g2d, Point2D.Double pos, ImageStorage imageStorage,
      String imageName, Camera camera, JPanel panel, double progress) {
    if (imageStorage == null || imageStorage.image == null) {
      return;
    }
    double clamped = Math.max(0.0, Math.min(1.0, progress));
    BufferedImage baseImage = imageStorage.image;
    Point2D.Double imageSize = new Point2D.Double(baseImage.getWidth(), baseImage.getHeight());
    Point2D.Double screenPos = camera.isoToScreen(pos);
    double cameraScale = camera.getScale();
    Point2D.Double posShift = calculateShiftImage(imageSize, cameraScale);
    int xPos = (int) Math.round(screenPos.x + posShift.x);
    int yPos = (int) Math.round(screenPos.y + posShift.y);
    int width = (int) (imageSize.x * cameraScale);
    int height = (int) (imageSize.y * cameraScale);
    if (width <= 0 || height <= 0) {
      return;
    }

    double angle = Math.toRadians(DEAD_ROTATION_DEGREES * clamped);
    double pivotX = xPos + width / 2.0;
    double pivotY = yPos + height;
    AffineTransform originalTransform = g2d.getTransform();
    Composite originalComposite = g2d.getComposite();
    g2d.rotate(angle, pivotX, pivotY);

    float baseAlpha = (float) (1.0 - clamped);
    if (baseAlpha > 0.0f) {
      g2d.setComposite(AlphaComposite.SrcOver.derive(baseAlpha));
      g2d.drawImage(baseImage, xPos, yPos, width, height, panel);
    }
    BufferedImage tinted = getDeadTintedImage(imageName, baseImage);
    float tintAlpha = (float) clamped;
    if (tinted != null && tintAlpha > 0.0f) {
      g2d.setComposite(AlphaComposite.SrcOver.derive(tintAlpha));
      g2d.drawImage(tinted, xPos, yPos, width, height, panel);
    }

    g2d.setComposite(originalComposite);
    g2d.setTransform(originalTransform);
  }

  private BufferedImage getDeadTintedImage(String imageName, BufferedImage baseImage) {
    if (imageName == null || baseImage == null) {
      return null;
    }
    BufferedImage cached = deadTintCache.get(imageName);
    if (cached != null) {
      return cached;
    }
    BufferedImage tinted = createHueShiftedImage(baseImage, DEAD_HUE_SHIFT);
    if (tinted != null) {
      deadTintCache.put(imageName, tinted);
    }
    return tinted;
  }

  private BufferedImage createHueShiftedImage(BufferedImage baseImage, float hueShift) {
    int width = baseImage.getWidth();
    int height = baseImage.getHeight();
    if (width <= 0 || height <= 0) {
      return null;
    }
    BufferedImage shifted = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    float shift = hueShift % 1.0f;
    if (shift < 0.0f) {
      shift += 1.0f;
    }
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        int argb = baseImage.getRGB(x, y);
        int alpha = (argb >>> 24) & 0xFF;
        if (alpha == 0) {
          shifted.setRGB(x, y, 0);
          continue;
        }
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;
        float[] hsb = Color.RGBtoHSB(r, g, b, null);
        float newHue = hsb[0] + shift;
        if (newHue > 1.0f) {
          newHue -= 1.0f;
        }
        int rgb = Color.HSBtoRGB(newHue, hsb[1], hsb[2]);
        int tintedArgb = (alpha << 24) | (rgb & 0x00FFFFFF);
        shifted.setRGB(x, y, tintedArgb);
      }
    }
    return shifted;
  }
}
