package io.github.sasori_256.town_planning.common.ui;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;

/**
 * アニメーション（連番 PNG）を管理し、フレームを取得するクラス。
 *
 * <p>ファイル名は末尾に番号がついていることを期待します（例: walk_001.png）。
 */
public class AnimationManager {
  private final Map<String, AnimationStorage> animations = new HashMap<>();

  /**
   * アニメーションマネージャを生成し、画像を読み込む。
   */
  public AnimationManager() {
    this.loadAnimations();
  }

  /** resources/animations にある画像を読み込む。 */
  public void loadAnimations() {
    URL animationsUrl = this.getClass().getClassLoader().getResource("animations");
    String path = animationsUrl != null ? animationsUrl.getPath() : null;
    File dir = path != null ? new File(path) : null;

    List<File> fileList = new ArrayList<>();
    if (dir != null && dir.exists()) {
      // 再帰的に検索
      java.util.Deque<File> stack = new java.util.ArrayDeque<>();
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
            fileList.add(child);
          }
        }
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
        this.animations.put(base.toLowerCase(), storage);
        System.out.println("Loaded animation: " + base + " (" + frames.size() + " frames)");
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
    if (name == null) {
      return null;
    }
    AnimationStorage storage = this.animations.get(name.toLowerCase());
    if (storage == null || storage.frames.isEmpty()) {
      return null;
    }
    int count = storage.frames.size();
    if (count <= 0) {
      return null;
    }
    int idx = frameIndex;
    if (loop) {
      idx = Math.floorMod(frameIndex, count);
    } else {
      idx = Math.max(0, Math.min(frameIndex, count - 1));
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
    AnimationStorage storage = this.animations.get(name.toLowerCase());
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
