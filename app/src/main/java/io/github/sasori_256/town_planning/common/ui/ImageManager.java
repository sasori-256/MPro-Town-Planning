package io.github.sasori_256.town_planning.common.ui;

import java.awt.Component;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.geom.Point2D;
import java.awt.image.ImageObserver;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ImageManager extends Component {
  private final Map<String, ImageStorage> imageStorages = new HashMap<>();

  public void loadImages() {
    String PATH = this.getClass().getClassLoader().getResource("images").getPath();
    File dir = new File(PATH);
    File[] files = dir.listFiles((d, name) -> {
      return name.endsWith(".png");
    });
    MediaTracker tracker = new MediaTracker(this);
    if (files != null) {
      for (int i = 0; i < files.length; i++) {
        File file = files[i];
        String imageName = file.getName().replaceFirst("[.][^.]+$", "").toLowerCase();
        System.out.println("Loading image: " + imageName);
        Image img = Toolkit.getDefaultToolkit().getImage(file.getPath());
        tracker.addImage(img, 0);

        try {
          tracker.waitForAll();
        } catch (InterruptedException e) {
          System.err.println("Error loading image: " + imageName);
          e.printStackTrace();
        }

        ImageStorage storage = new ImageStorage(imageName, img);
        this.imageStorages.put(imageName, storage);
      }
    }

  }

  ImageManager() {
    this.loadImages();
  }

  public ImageStorage getImageStorage(String name) {
    ImageStorage storage = (ImageStorage) this.imageStorages.get(name.toLowerCase());
    if (storage != null) {
      return storage;
    } else {
      storage = (ImageStorage) this.imageStorages.get("error");
      if (storage != null) {
        System.out.println("Warning: Image not found: " + name + ".png, returning error image.");
        return storage;
      } else {
        System.err.println("Fatal: Error image not found: error.png");
        return null;
      }
    }
  }

  public static final class ImageStorage {
    final String name;
    final Image image;
    Point2D.Double size;

    public void loadSize() {
      this.size.x = (double) this.image.getWidth((ImageObserver) null);
      this.size.y = (double) this.image.getHeight((ImageObserver) null);
    }

    ImageStorage(String name, Image image) {
      this.name = name;
      this.image = image;
      this.size = new Point2D.Double(32.0, 32.0);
      this.loadSize();
    }
  }
}
