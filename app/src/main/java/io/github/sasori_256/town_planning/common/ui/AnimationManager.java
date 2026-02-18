package io.github.sasori_256.town_planning.common.ui;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

/**
 * アニメーション（連番 PNG）を管理し、フレームを取得するクラス。
 * JARファイル内でも動作するように修正済み。
 */
public class AnimationManager {
  private final Map<String, AnimationStorage> animationStorages = new HashMap<>();

  public AnimationManager() {
    this.loadAnimations();
  }

  /** resources/animations にある画像を読み込む。 */
  public void loadAnimations() {
    ClassLoader cl = this.getClass().getClassLoader();
    List<String> resourcePaths = new ArrayList<>();

    // 1. まず、画像ファイルの「パス（文字列）」だけをかき集める
    try {
      Enumeration<URL> urls = cl.getResources("animations");
      while (urls.hasMoreElements()) {
        URL dirURL = urls.nextElement();
        if (dirURL == null)
          continue;

        String protocol = dirURL.getProtocol();
        if ("file".equals(protocol)) {
          // IDE実行時: ファイルシステムを探索
          File root = new File(dirURL.toURI());
          scanDirectory(root, "animations", resourcePaths);
        } else if ("jar".equals(protocol)) {
          // JAR実行時: JAR内部を探索
          scanJar(dirURL, resourcePaths);
        }
      }
    } catch (Exception e) {
      System.err.println("Error locating animation resources: " + e.getMessage());
      e.printStackTrace();
    }

    // 2. 集めたパスを使って画像を読み込む (Fileクラスは使わない)
    Pattern pattern = Pattern.compile("^(.*?)[_\\-]?(\\d+)$");
    Map<String, List<FrameInfo>> grouped = new HashMap<>();

    for (String resPath : resourcePaths) {
      // パスからファイル名を取得 (例: "animations/walk_01.png" -> "walk_01")
      String fileName = new File(resPath).getName(); // パス解析のためだけにFileを使用(読み込みはしない)
      String nameBase = fileName.replaceFirst("[.][^.]+$", "").toLowerCase(); // 拡張子削除

      // 正規表現で名前とインデックスを分離
      Matcher matcher = pattern.matcher(nameBase);
      String animName;
      int idx = 0;
      if (matcher.matches()) {
        animName = matcher.group(1).isEmpty() ? nameBase : matcher.group(1);
        try {
          idx = Integer.parseInt(matcher.group(2));
        } catch (NumberFormatException e) {
          idx = 0;
        }
      } else {
        animName = nameBase;
      }

      // ストリームから画像を読み込む
      try (InputStream is = cl.getResourceAsStream(resPath)) {
        if (is == null) {
          System.err.println("Could not open stream for: " + resPath);
          continue;
        }
        BufferedImage img = ImageIO.read(is);
        if (img != null) {
          grouped.computeIfAbsent(animName, k -> new ArrayList<>())
              .add(new FrameInfo(img, idx));
        }
      } catch (IOException e) {
        System.err.println("Error loading image: " + resPath);
        e.printStackTrace();
      }
    }

    // 3. 読み込んだ画像をソートして保存
    for (Map.Entry<String, List<FrameInfo>> entry : grouped.entrySet()) {
      String base = entry.getKey();
      List<FrameInfo> list = entry.getValue();
      // インデックス順に並べ替え
      Collections.sort(list, Comparator.comparingInt(o -> o.index));

      List<BufferedImage> frames = new ArrayList<>();
      for (FrameInfo fi : list) {
        frames.add(fi.image);
      }

      if (!frames.isEmpty()) {
        this.animationStorages.put(base, new AnimationStorage(base, frames));
        System.out.println("Loaded animation: " + base + " (" + frames.size() + " frames)");
      }
    }
  }

  // IDE用: 再帰的にディレクトリを探索
  private void scanDirectory(File dir, String resourcePrefix, List<String> paths) {
    File[] files = dir.listFiles();
    if (files == null)
      return;

    for (File f : files) {
      if (f.isDirectory()) {
        scanDirectory(f, resourcePrefix + "/" + f.getName(), paths);
      } else if (f.getName().toLowerCase().endsWith(".png")) {
        paths.add(resourcePrefix + "/" + f.getName());
      }
    }
  }

  // JAR用: JarFileエントリを探索
  private void scanJar(URL url, List<String> paths) throws IOException {
    String path = url.getPath();
    int bang = path.indexOf("!");
    String jarPath = (bang >= 0) ? path.substring(0, bang) : path;
    if (jarPath.startsWith("file:")) {
      jarPath = jarPath.substring(5);
    }
    jarPath = URLDecoder.decode(jarPath, StandardCharsets.UTF_8.name());

    try (JarFile jar = new JarFile(jarPath)) {
      Enumeration<JarEntry> entries = jar.entries();
      while (entries.hasMoreElements()) {
        JarEntry entry = entries.nextElement();
        String name = entry.getName();
        // "animations/" で始まり、.png で終わるものを収集
        if (name.startsWith("animations/") && !entry.isDirectory() && name.toLowerCase().endsWith(".png")) {
          paths.add(name);
        }
      }
    }
  }

  public BufferedImage getFrame(String name, int frameIndex, boolean loop) {
    if (name == null)
      return null;
    AnimationStorage storage = this.animationStorages.get(name.toLowerCase());
    if (storage == null || storage.frames.isEmpty())
      return null;

    int count = storage.frames.size();
    int idx;
    if (loop) {
      idx = Math.floorMod(frameIndex, count);
    } else {
      idx = Math.max(0, Math.min(frameIndex, count - 1));
    }
    return storage.frames.get(idx);
  }

  public int getFrameCount(String name) {
    if (name == null)
      return 0;
    AnimationStorage storage = this.animationStorages.get(name.toLowerCase());
    return (storage == null) ? 0 : storage.frames.size();
  }

  // 画像とインデックスを一時保持するクラス
  private static final class FrameInfo {
    final BufferedImage image;
    final int index;

    FrameInfo(BufferedImage image, int idx) {
      this.image = image;
      this.index = idx;
    }
  }

  private static final class AnimationStorage {
    final String name; // Warning抑制のため削除しても良いが残置
    final List<BufferedImage> frames;

    AnimationStorage(String name, List<BufferedImage> frames) {
      this.name = name;
      this.frames = frames;
    }
  }
}