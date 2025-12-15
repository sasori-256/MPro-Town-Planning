package io.github.sasori_256.town_planning.common.core;

// AtomicBooleanについて
// AtomicBooleanは、Javaのjava.util.concurrent.atomicパッケージに属するクラスであり、
// スレッドセーフなブール値の操作を提供します。
// 複数のスレッドが同時にアクセスする可能性のあるブール値を扱う際に使用され、
// 競合状態を防ぐために設計されています。
// AtomicBooleanは、基本的なブール値の操作（true/falseの設定、取得、反転など）を
// 原子操作として提供します。
// これにより、複数のスレッドが同時に値を変更しようとした場合でも、一貫性が保たれます。
// 例えば、複数のスレッドが同時にフラグを設定またはクリアしようとする場合に、
// AtomicBooleanを使用することで、競合状態を防ぎ、正しい結果を得ることができます。
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 固定タイムステップのゲームループを提供するクラス。
 * 更新処理(Update)と描画処理(Render)を制御する。
 */
public class GameLoop implements Runnable {
  private final AtomicBoolean running = new AtomicBoolean(false);
  private final Runnable updateCallback;
  private final Runnable renderCallback;
  private Thread thread = null;

  // 60 FPS target
  // TODO: マジックナンバーを定数化して外部から変更できるようにする
  private static final double TIME_STEP = 1.0 / 60.0;
  private static final long TIME_STEP_NANO = (long) (TIME_STEP * 1_000_000_000);

  public GameLoop(Runnable updateCallback, Runnable renderCallback) {
    this.updateCallback = updateCallback;
    this.renderCallback = renderCallback;
  }

  public void start() {
    if (thread == null) {
      try {
        if (running.compareAndSet(false, true)) {
          thread = new Thread(this, "GameLoop-Thread");
          thread.setDaemon(true); // アプリ終了時に自動で落ちるように
          thread.start();
        }
      } catch (Exception e) {
        running.set(false);
        e.printStackTrace();
      }
    }
  }

  public void stop() {
    running.set(false);
    try {
      if (thread != null) {
        thread.join(); // ゲームループスレッドの終了を待機
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      e.printStackTrace();
    }
  }

  @Override
  public void run() {
    long lastTime = System.nanoTime(); // 前フレームの時刻
    double accumulator = 0.0; // 経過時間

    while (running.get()) {
      long now = System.nanoTime();
      long frameTime = now - lastTime;
      lastTime = now;

      // あまりに大きな遅延が発生した場合の補正
      // フレーム時間が0.25秒を超える場合、0.25秒に制限する
      if (frameTime > 250_000_000) { // Max frame time to avoid spiral of death (0.25s)
        frameTime = 250_000_000;
      }

      accumulator += frameTime;

      // 修正ループ: 固定タイムステップで更新を行う
      while (accumulator >= TIME_STEP_NANO) {
        updateCallback.run();
        accumulator -= TIME_STEP_NANO;
      }

      // 描画処理
      // renderCallback.run();

      // フレームレート制御: 次のフレームまで待機
      long sleepTime = (TIME_STEP_NANO - (System.nanoTime() - now)) / 1_000_000;
      if (sleepTime > 1) {
        try {
          Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      }
    }
  }
}
