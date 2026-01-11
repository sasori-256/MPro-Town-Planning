package io.github.sasori_256.town_planning.common.ui;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.Timer;

/**
 * アニメーション（連番 PNG）を管理し、再生・描画するクラス
 *
 * 使用法:
 * - コンストラクタで resources/animations 下の PNG を読み込む
 * - play("walk", 12, x, y) のようにして再生を登録する
 *
 * 実装の注意:
 * - ファイル名は末尾に番号がついていることを期待します（例: walk_001.png, walk_002.png）
 * - 番号部分のパターンは (.*?)[_-]?(\\d+) を想定し、数値でソートします
 * - 再生はループ再生します
 */
public class AnimationManager extends JComponent {
	private final Map<String, AnimationStorage> animations = new HashMap<>();
	private final List<PlayingAnimation> playing = new ArrayList<>();

	// 汎用タイマーで repaint をトリガーする（UI スレッドで動く）
	private Timer timer;

	public AnimationManager() {
		this.loadAnimations();
		// デフォルトで透過描画（必要なら true に変更してください）
		this.setOpaque(false);
	}

	/** resources/animations にある画像を読み込む */
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
				if (children == null)
					continue;
				for (File child : children) {
					if (child.isDirectory()) {
						stack.push(child);
					} else if (child.getName().toLowerCase().endsWith(".png")) {
						fileList.add(child);
					}
				}
			}
		}

		// baseName -> list of (file, index)
		Pattern p = Pattern.compile("^(.*?)[_\\-]?(\\d+)$");
		Map<String, List<FileWithIndex>> grouped = new HashMap<>();
		for (File f : fileList) {
			String name = f.getName().replaceFirst("[.][^.]+$", "").toLowerCase();
			Matcher m = p.matcher(name);
			String base;
			int idx = 0;
			if (m.matches()) {
				base = m.group(1).isEmpty() ? name : m.group(1);
				try {
					idx = Integer.parseInt(m.group(2));
				} catch (NumberFormatException e) {
					idx = 0;
				}
			} else {
				base = name;
				idx = 0;
			}
			grouped.computeIfAbsent(base, k -> new ArrayList<>()).add(new FileWithIndex(f, idx));
		}

		for (Map.Entry<String, List<FileWithIndex>> e : grouped.entrySet()) {
			String base = e.getKey();
			List<FileWithIndex> list = e.getValue();
			// インデックスでソートする
			Collections.sort(list, Comparator.comparingInt(o -> o.index));
			List<BufferedImage> frames = new ArrayList<>();
			for (FileWithIndex fi : list) {
				try {
					BufferedImage img = ImageIO.read(fi.file);
					if (img != null)
						frames.add(img);
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
	 * 指定した名前のアニメーションを指定フレームレートで x,y に描画する（ループ再生）
	 * 
	 * @param name アニメーション名（拡張子・番号なし、小文字大文字不問）
	 */
	public PlayingAnimation play(String name, int frameRate, double x, double y, boolean doLoop) {
		if (name == null)
			return null;
		AnimationStorage storage = this.animations.get(name.toLowerCase());
		if (storage == null) {
			System.err.println("Animation not found: " + name);
			return null;
		}
		System.out.println("Start animation: " + name + " at (" + x + "," + y + ") with frameRate " + frameRate);
		if (frameRate <= 0)
			frameRate = 1;
		PlayingAnimation pa = new PlayingAnimation(storage, frameRate, x, y, doLoop, System.currentTimeMillis());
		synchronized (this.playing) {
			this.playing.add(pa);
		}
		ensureTimerRunning();
		this.repaint();
		return pa;
	}

	public void stop(PlayingAnimation pa) {
		if (pa == null)
			return;
		synchronized (this.playing) {
			this.playing.remove(pa);
		}
	}

	private void ensureTimerRunning() {
		// TODO: オリジナルのタイマーを持つのではなく、GameLoopから呼び出されるように変更する
		if (this.timer == null) {
			// 40ms 毎に repaint（最終描画タイミングは各アニメーションの fps を尊重する）
			this.timer = new Timer(40, e -> this.repaint());
			this.timer.setRepeats(true);
			this.timer.start();
		} else if (!this.timer.isRunning()) {
			this.timer.start();
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		long now = System.currentTimeMillis();
		synchronized (this.playing) {
			Iterator<PlayingAnimation> it = this.playing.iterator();
			while (it.hasNext()) {
				PlayingAnimation pa = it.next();
				if (pa.isFinished(now)) {
					it.remove();
					continue;
				}
				BufferedImage img = pa.getCurrentFrame(now);
				if (img != null) {
					g.drawImage(img, (int) Math.round(pa.x), (int) Math.round(pa.y), null);
				}
			}
		}
	}

	// 補助クラス
	private static final class FileWithIndex {
		final File file;
		final int index;

		FileWithIndex(File f, int idx) {
			this.file = f;
			this.index = idx;
		}
	}

	public static final class AnimationStorage {
		public final String name;
		public final List<BufferedImage> frames;
		public final int width;
		public final int height;

		AnimationStorage(String name, List<BufferedImage> frames) {
			this.name = name;
			this.frames = frames;
			if (!frames.isEmpty()) {
				BufferedImage img = frames.get(0);
				this.width = img.getWidth();
				this.height = img.getHeight();
			} else {
				this.width = 0;
				this.height = 0;
			}
		}
	}

	private static final class PlayingAnimation {
		final AnimationStorage storage;
		final int frameRate;
		final double x;
		final double y;
		final boolean doLoop;
		final long startMs;
		final long frameDurationMs;

		PlayingAnimation(AnimationStorage storage, int frameRate, double x, double y, boolean doLoop, long startMs) {
			this.storage = storage;
			this.frameRate = frameRate;
			this.x = x;
			this.y = y;
			this.doLoop = doLoop;
			this.startMs = startMs;
			this.frameDurationMs = Math.max(1, 1000L / frameRate);
		}

		BufferedImage getCurrentFrame(long nowMs) {
			if (storage == null || storage.frames.isEmpty())
				return null;
			long elapsed = Math.max(0, nowMs - this.startMs);
			int idx = (int) ((elapsed / this.frameDurationMs) % storage.frames.size());
			return storage.frames.get(idx);
		}

		boolean isFinished(long nowMs) {
			if (doLoop)
				return false;
			long elapsed = Math.max(0, nowMs - this.startMs);
			int totalFrames = storage.frames.size();
			long totalDuration = totalFrames * frameDurationMs;
			return elapsed >= totalDuration;
		}
	}
}