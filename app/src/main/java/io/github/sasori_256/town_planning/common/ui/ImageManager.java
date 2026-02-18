package io.github.sasori_256.town_planning.common.ui;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.File;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

/**
 * 画像を管理するクラス
 * 画像の読み込みと取得を担当する
 */
public class ImageManager extends Component {
  private final Map<String, ImageStorage> imageStorages = new HashMap<>();

  /**
   * 所定の場所にある全ての画像を読み込む (ClassLoader#getResource を使用)
   */
  public void loadImages() {
    URL imagesUrl = this.getClass().getClassLoader().getResource("images");
    String path = imagesUrl != null ? imagesUrl.getPath() : null;
    File dir = path != null ? new File(path) : null;

    if (dir != null && dir.exists()) {
      Deque<File> stack = new ArrayDeque<>();
      stack.push(dir);
      while (!stack.isEmpty()) {
        File current = stack.pop();
        File[] children = current.listFiles();
        if (children == null) {
          continue;
        }
        for (File child : children) {
          if (child.isDirectory()) {
            stack.push(child);
          } else if (child.getName().toLowerCase().endsWith(".png")) {
            String name = child.getName().substring(0, child.getName().length() - 4).toLowerCase();
            try {
              BufferedImage image = ImageIO.read(child);
              ImageStorage storage = new ImageStorage(name, image);
              this.imageStorages.put(name, storage);
            } catch (Exception e) {
              System.err.println("Failed to load image: " + child.getPath());
              e.printStackTrace();
            }
          }
        }
      }
    }

    // プレビュー用画像の生成と保存
    // TODO: PATHから画像を取得できるようになったらbuildingだけを対象にする。
    Map<String, ImageStorage> previewStorages = new HashMap<>();
    for (ImageStorage baseStorage : new HashMap<>(this.imageStorages).values()) {
      createAndCachePreview(baseStorage, true, previewStorages);
      createAndCachePreview(baseStorage, false, previewStorages);
    }
    this.imageStorages.putAll(previewStorages);
  }

  private void createAndCachePreview(ImageStorage baseStorage, boolean buildable,
      Map<String, ImageStorage> targetCache) {
    String previewImageName = (baseStorage.getName() + (buildable ? "_preview_buildable" : "_preview_unbuildable"))
        .toLowerCase();

    BufferedImage source = baseStorage.getImage();
    if (source == null) {
      return;
    }
    float[] offsets = new float[] { 0f, 0f, 0f, 0f };
    float[] scales;
    if (buildable) {
      scales = new float[] { 1f, 1f, 1f, 0.5f }; // 半透明
    } else {
      scales = new float[] { 1f, 0.3f, 0.3f, 0.7f }; // 赤がかった半透明
    }
    RescaleOp rescaleOp = new RescaleOp(scales, offsets, null);
    if (source.getType() != BufferedImage.TYPE_INT_ARGB) { // ARGB でない場合は変換
      BufferedImage argbImage = new BufferedImage(source.getWidth(), source.getHeight(),
          BufferedImage.TYPE_INT_ARGB);
      Graphics2D g2 = argbImage.createGraphics();
      g2.drawImage(source, 0, 0, null);
      g2.dispose();
      source = argbImage;
    }

    BufferedImage filteredImage = rescaleOp.filter(source, null);
    ImageStorage previewStorage = new ImageStorage(previewImageName, filteredImage);
    targetCache.put(previewImageName, previewStorage);
  }

  /**
   * 画像マネージャを生成し、画像を読み込む。
   */
  public ImageManager() {
    this.loadImages();
  }

  /**
   * 名前から対応する画像を取得する
   * 
   * @param name 取得したい画像の名前 (拡張子なし、小文字・大文字区別なし)
   * @return 画像情報
   */
  public ImageStorage getImageStorage(String name) {
    ImageStorage storage = this.imageStorages.get(name.toLowerCase());
    if (storage != null) {
      return storage;
    } else {
      storage = this.imageStorages.get("error");
      if (storage != null) {
        System.out.println("Warning: Image not found: " + name + ".png, returning error image.");
        return storage;
      } else {
        System.err.println("Fatal: Error image not found: error.png");
        return null;
      }
    }
  }

  /**
   * 画像の名前、画像オブジェクト、画像サイズを保持するクラス(構造体)
   */
  public static final class ImageStorage {
    final String name;
    final BufferedImage image;
    Point2D.Double size = new Point2D.Double(); // サイズは読み取り直しが必要な場合があるため、finalにしない

    /**
     * 画像サイズを読み込む。
     */
    public void loadSize() {
      if (this.image == null) {
        System.err.println("Cannot load size for null image: " + this.name);
        return;
      }
      this.size.x = this.image.getWidth(null);
      this.size.y = this.image.getHeight(null);
      if (this.size.x == -1 || this.size.y == -1) {
        System.err.println("Failed to get image size for: " + this.name);
        this.size = new Point2D.Double(64.0, 32.0);
        return;
      }
    }

    /**
     * 画像情報を生成する。
     *
     * @param name  画像名
     * @param image 画像
     */
    public ImageStorage(String name, BufferedImage image) {
      this.name = name;
      this.image = image;
      this.loadSize();
    }

    /**
     * 画像名を返す。
     *
     * @return 画像名
     */
    public String getName() {
      return name;
    }

    /**
     * 画像を返す。
     *
     * @return 画像
     */
    public BufferedImage getImage() {
      return image;
    }

    /**
     * 画像サイズを返す。
     *
     * @return 画像サイズ
     */
    public Point2D.Double getSize() {
      return size;
    }
  }
}
