package io.github.sasori_256.town_planning.common.ui;

import java.awt.Component;
import java.awt.MediaTracker;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
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
   * 所定の場所にある全ての画像を読み込む
   */
  public void loadImages() {
    java.net.URL imagesUrl = this.getClass().getClassLoader().getResource("images");
    String PATH = imagesUrl != null ? imagesUrl.getPath() : null;
    File dir = PATH != null ? new File(PATH) : null;

    java.util.List<File> fileList = new java.util.ArrayList<>();
    if (dir != null && dir.exists()) {
      // スタックを使って再帰的にサブディレクトリを探索する
      java.util.Deque<File> stack = new java.util.ArrayDeque<>();
      stack.push(dir);
      while (!stack.isEmpty()) {
        File current = stack.pop();
        File[] children = current.listFiles();
        if (children == null) continue;
        for (File child : children) {
          if (child.isDirectory()) {
            stack.push(child);
          } else if (child.getName().toLowerCase().endsWith(".png")) {
            fileList.add(child);
          }
        }
      }
    }
    File[] files = fileList.toArray(new File[0]);
    MediaTracker tracker = new MediaTracker(this);
    // 一時的に画像名とImageオブジェクトを保持する
    Map<String, BufferedImage> loadedImages = new HashMap<>();
    if (files != null) {
      for (int i = 0; i < files.length; i++) {
        File file = files[i];
        String imageName = file.getName().replaceFirst("[.][^.]+$", "").toLowerCase();
        // System.out.println("Loading image: " + imageName);
        try {
          BufferedImage img = ImageIO.read(file);
          tracker.addImage(img, 0);
          loadedImages.put(imageName, img);
        } catch (Exception e) {
          System.err.println("Error loading image: " + file.getName());
          e.printStackTrace();
          continue;
        }
      }
      try {
        tracker.waitForAll();
      } catch (InterruptedException e) {
        System.err.println("Error loading images");
        e.printStackTrace();
      }

      for (Map.Entry<String, BufferedImage> entry : loadedImages.entrySet()) {
        String imageName = entry.getKey();
        BufferedImage img = entry.getValue();
        ImageStorage storage = new ImageStorage(imageName, img);
        this.imageStorages.put(imageName, storage);
      }
    }

  }

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
        // System.out.println("Warning: Image not found: " + name + ".png, returning error image.");
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

    public ImageStorage(String name, BufferedImage image) {
      this.name = name;
      this.image = image;
      this.loadSize();
    }
  }
}
