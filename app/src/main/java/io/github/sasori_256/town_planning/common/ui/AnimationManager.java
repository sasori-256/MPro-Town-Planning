package io.github.sasori_256.town_planning.common.ui;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;

/**
 * アニメーション（連番 PNG）を管理し、フレームを取得するクラス。
 *
 * <p>
 * ファイル名は末尾に番号がついていることを期待します（例: walk_001.png）。
 */
public class AnimationManager {
  private final Map<String, AnimationStorage> animationStorages = new HashMap<>();

  /**
   * アニメーションマネージャを生成し、画像を読み込む。
   */
  public AnimationManager() {
    this.loadAnimations();
  }

  /** resources/animations にある画像を読み込む。 */
  public void loadAnimations() {
    ClassLoader cl = this.getClass().getClassLoader();
    List<String> resourcePaths = new ArrayList<>();

    try {
      java.net.URL dirURL = cl.getResource("animations");
      if (dirURL != null) {
        String protocol = dirURL.getProtocol();
        if ("file".equals(protocol)) {
          // ディレクトリとして読み込める場合 (IDE実行など)
          java.io.File root = new java.io.File(dirURL.toURI());
          Deque<java.io.File> stack = new ArrayDeque<>();
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
                // resource path は "animations/..." の形式
                resourcePaths.add("animations/" + rel);
              }
            }
          }
        } else if ("jar".equals(protocol)) {
          // JAR 内の場合は JarFile を開いて entries を走査
          String path = dirURL.getPath(); // like file:/path/to/jar.jar!/animations
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
              if (name.startsWith("animations/") && name.toLowerCase().endsWith(".png")) {
                resourcePaths.add(name);
              }
            }
          } finally {
            if (jar != null) {
              jar.close();
            }
          }
        }
      }
    } catch (Exception e) {
      System.err.println("Error locating animation resources: " + e.getMessage());
      e.printStackTrace();
    }

    // 読み込んだリソースパスからファイルを作成
    List<File> fileList = new ArrayList<>();
    for (String resPath : resourcePaths) {
      URL url = cl.getResource(resPath);
      if (url != null) {
        try {
          File f = new File(url.toURI());
          fileList.add(f);
        } catch (Exception e) {
          System.err.println("Error accessing animation resource: " + resPath + " - " + e.getMessage());
          e.printStackTrace();
        }
      } else {
        System.err.println("Resource not found for animation: " + resPath);
      }
    }

    Pattern pattern = Pattern.compile("^(.*?)[_\\-]?(\\d+)$");
    Map<String, List<FileWithIndex>> grouped = new HashMap<>();
    for (File file : fileList) {
      String name = file.getName().replaceFirst("[.][^.]+$", "").toLowerCase();
      Matcher matcher = pattern.matcher(name);
      String base;
      int idx = 0;
      if (matcher.matches()) {
        base = matcher.group(1).isEmpty() ? name : matcher.group(1);
        try {
          idx = Integer.parseInt(matcher.group(2));
        } catch (NumberFormatException e) {
          idx = 0;
        }
      } else {
        base = name;
        idx = 0;
      }
      grouped.computeIfAbsent(base, k -> new ArrayList<>()).add(new FileWithIndex(file, idx));
    }

    for (Map.Entry<String, List<FileWithIndex>> entry : grouped.entrySet()) {
      String base = entry.getKey();
      List<FileWithIndex> list = entry.getValue();
      Collections.sort(list, Comparator.comparingInt(o -> o.index));
      List<BufferedImage> frames = new ArrayList<>();
      for (FileWithIndex fi : list) {
        try {
          BufferedImage img = ImageIO.read(fi.file);
          if (img != null) {
            frames.add(img);
          }
        } catch (Exception ex) {
          System.err.println("Error loading animation frame: " + fi.file.getName());
          ex.printStackTrace();
        }
      }
      if (!frames.isEmpty()) {
        AnimationStorage storage = new AnimationStorage(base, frames);
        this.animationStorages.put(base.toLowerCase(), storage);
        // System.out.println("Loaded animation: " + base + " (" + frames.size() + "
        // frames)");
      }
    }
  }

  /**
   * 指定したフレームを取得する。
   *
   * @param name       アニメーション名（拡張子・番号なし、小文字大文字不問）
   * @param frameIndex フレーム番号
   * @param loop       ループ再生する場合は true
   * @return フレーム画像（存在しない場合は null）
   */
  public BufferedImage getFrame(String name, int frameIndex, boolean loop) {
    if (name == null) { // 指定されたアニメーション名が存在しない場合は何もしない
      return null;
    }
    AnimationStorage storage = this.animationStorages.get(name.toLowerCase());
    if (storage == null || storage.frames.isEmpty()) { // アニメーションが見つからない場合も何もしない
      return null;
    }
    int count = storage.frames.size(); // アニメーションの総フレーム数
    if (count <= 0) { // アニメーションが存在するがフレームがない場合(そんなことはないはずだが)
      return null;
    }
    int idx = frameIndex; // 再生するべきフレームのインデックス
    if (loop) {
      idx = Math.floorMod(frameIndex, count); // ループ再生の場合はフレーム数で割った余りをインデックスとする
    } else {
      idx = Math.max(0, Math.min(frameIndex, count - 1)); // ループ再生でない場合はフレーム数の範囲内にインデックスを収める
    }
    return storage.frames.get(idx);
  }

  /**
   * アニメーションのフレーム数を返す。
   *
   * @param name アニメーション名
   * @return フレーム数（見つからない場合は0）
   */
  public int getFrameCount(String name) {
    if (name == null) {
      return 0;
    }
    AnimationStorage storage = this.animationStorages.get(name.toLowerCase());
    if (storage == null) {
      return 0;
    }
    return storage.frames.size();
  }

  private static final class FileWithIndex {
    final File file;
    final int index;

    FileWithIndex(File file, int idx) {
      this.file = file;
      this.index = idx;
    }
  }

  private static final class AnimationStorage {
    private final String name;
    private final List<BufferedImage> frames;

    AnimationStorage(String name, List<BufferedImage> frames) {
      this.name = name;
      this.frames = frames;
    }
  }
}
