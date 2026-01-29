package io.github.sasori_256.town_planning.common.ui;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
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
   * 所定の場所にある全ての画像を読み込む (ClassLoader#getResourceAsStream を使用)
   */
  public void loadImages() {
    ClassLoader cl = this.getClass().getClassLoader();
    java.util.List<String> resourcePaths = new java.util.ArrayList<>();

    try {
      java.net.URL dirURL = cl.getResource("images");
      if (dirURL != null) {
        String protocol = dirURL.getProtocol();
        if ("file".equals(protocol)) {
          // ディレクトリとして読み込める場合 (IDE実行など)
          java.io.File root = new java.io.File(dirURL.toURI());
          java.util.Deque<java.io.File> stack = new java.util.ArrayDeque<>();
          stack.push(root);
          while (!stack.isEmpty()) {
            java.io.File cur = stack.pop();
            java.io.File[] children = cur.listFiles();
            if (children == null)
              continue;
            for (java.io.File child : children) {
              if (child.isDirectory()) {
                stack.push(child);
              } else if (child.getName().toLowerCase().endsWith(".png")) {
                // resources 内の相対パスを作る
                String rel = root.toURI().relativize(child.toURI()).getPath();
                // resource path は "images/..." の形式
                resourcePaths.add("images/" + rel);
              }
            }
          }
        } else if ("jar".equals(protocol)) {
          // JAR 内の場合は JarFile を開いて entries を走査
          String path = dirURL.getPath(); // like file:/path/to/jar.jar!/images
          int bang = path.indexOf("!");
          String jarPath = (bang >= 0) ? path.substring(0, bang) : path;
          if (jarPath.startsWith("file:")) {
            jarPath = jarPath.substring("file:".length());
          }
          jarPath = java.net.URLDecoder.decode(jarPath, "UTF-8");
          java.util.jar.JarFile jar = null;
          try {
            jar = new java.util.jar.JarFile(jarPath);
            java.util.Enumeration<java.util.jar.JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
              java.util.jar.JarEntry entry = entries.nextElement();
              String name = entry.getName();
              if (name.startsWith("images/") && !entry.isDirectory() && name.toLowerCase().endsWith(".png")) {
                resourcePaths.add(name);
              }
            }
          } finally {
            if (jar != null) {
              try {
                jar.close();
              } catch (Exception ignored) {
              }
            }
          }
        } else {
          // その他のプロトコル (例: vfs, etc.) - 試しに URL からストリームを直接探し、失敗したら何もしない
        }
      } else {
        // images ディレクトリが見つからない場合は試しにクラスパス全体を検索 (簡易)
        java.util.Enumeration<java.net.URL> roots = cl.getResources("");
        while (roots.hasMoreElements()) {
          java.net.URL u = roots.nextElement();
          // ここでは通常のケースは上の file/jar でカバーされるため特別な処理は行わない
        }
      }
    } catch (Exception e) {
      System.err.println("Error locating image resources");
      e.printStackTrace();
    }

    // 読み込み
    Map<String, BufferedImage> loadedImages = new HashMap<>();
    for (String resPath : resourcePaths) {
      try (java.io.InputStream is = cl.getResourceAsStream(resPath)) {
        if (is == null) {
          System.err.println("Resource not found via stream: " + resPath);
          continue;
        }
        BufferedImage img = ImageIO.read(is);
        if (img == null) {
          System.err.println("Failed to read image (not PNG?): " + resPath);
          continue;
        }
        String imageName = new java.io.File(resPath).getName().replaceFirst("[.][^.]+$", "").toLowerCase();
        loadedImages.put(imageName, img);
      } catch (Exception e) {
        System.err.println("Error loading resource image: " + resPath);
        e.printStackTrace();
      }
    }

    // ImageStorage に保持
    for (Map.Entry<String, BufferedImage> entry : loadedImages.entrySet()) {
      String imageName = entry.getKey();
      BufferedImage img = entry.getValue();
      ImageStorage storage = new ImageStorage(imageName, img);
      this.imageStorages.put(imageName, storage);
    }

    // プレビュー用画像の生成と保存
    // TODO: PATHから画像を取得できるようになったらbuildingだけを対象にする。
    Map<String, ImageStorage> previewStorages = new HashMap<>();
    for (ImageStorage baseStorage : new HashMap<>(this.imageStorages).values()) {
      createAndCachePreview(baseStorage, true, previewStorages);
      createAndCachePreview(baseStorage, false, previewStorages);
    }
    this.imageStorages.putAll(previewStorages);

    // 回転建物用画像の生成と保存
    // TODO: PATHから画像を取得できるようになったらbuildingだけを対象にする。
    Map<String, ImageStorage> rotateStorages = new HashMap<>();
    for (ImageStorage baseStorage : new HashMap<>(this.imageStorages).values()) {
      createAndCacheRotateBuilding(baseStorage, rotateStorages);
    }
    this.imageStorages.putAll(rotateStorages);
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

  private void createAndCacheRotateBuilding(ImageStorage baseStorage, Map<String, ImageStorage> targetCache) {
    String previewImageName = (baseStorage.getName() + "_rotate")
        .toLowerCase();

    BufferedImage source = baseStorage.getImage();
    if (source == null) {
      return;
    }

    AffineTransform tf = AffineTransform.getScaleInstance(-1, 1);
    tf.translate(-source.getWidth(), 0);
    AffineTransformOp flipOp = new AffineTransformOp(tf, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
    BufferedImage filteredImage = flipOp.filter(source, null);
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
      // System.out.println("Warning: Image not found: " + name + ".png");
      storage = this.imageStorages.get("error");
      if (storage != null) {
        // System.out.println("Warning: Image not found: " + name + ".png, returning
        // error image.");
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
  }
}
