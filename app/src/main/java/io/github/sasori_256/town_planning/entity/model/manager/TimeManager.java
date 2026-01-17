package io.github.sasori_256.town_planning.entity.model.manager;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

import io.github.sasori_256.town_planning.common.event.EventBus;
import io.github.sasori_256.town_planning.common.event.events.DayPassedEvent;
import io.github.sasori_256.town_planning.entity.model.GameTime;

/**
 * ゲーム内時間の進行を管理する。
 */
public class TimeManager {
  /** イベント通知に使用するイベントバス。 */
  private final EventBus eventBus;
  /** 状態同期に使用するロック。 */
  private final ReadWriteLock stateLock;
  /** ゲーム内時間の本体。 */
  private final GameTime gameTime = new GameTime();

  /**
   * 時間管理を生成する。
   *
   * @param eventBus  イベントバス
   * @param stateLock 状態ロック
   */
  public TimeManager(EventBus eventBus, ReadWriteLock stateLock) {
    this.eventBus = eventBus;
    this.stateLock = stateLock;
  }

  /**
   * 前回からの経過時間を反映し、日付進行イベントを発行する。
   *
   * @param dt          経過秒
   * @param onDayPassed 日付進行時の追加処理。不要ならnull
   * @return 進行した日数
   */
  public int advance(double dt, IntConsumer onDayPassed) {
    return withWriteLock(() -> {
      int previousDay = gameTime.getDayCount();
      int daysAdvanced = gameTime.advance(dt);
      if (daysAdvanced > 0) {
        int currentDay = gameTime.getDayCount();
        for (int dayNumber = previousDay + 1; dayNumber <= currentDay; dayNumber++) {
          eventBus.publish(new DayPassedEvent(dayNumber));
          if (onDayPassed != null) {
            onDayPassed.accept(dayNumber);
          }
        }
      }
      return daysAdvanced;
    });
  }

  /**
   * 現在の日数を返す。
   *
   * @return 日数
   */
  public int getDay() {
    return withReadLock(() -> gameTime.getDayCount());
  }

  /**
   * 1日の経過秒を返す。
   *
   * @return 経過秒
   */
  public double getTimeOfDaySeconds() {
    return withReadLock(() -> gameTime.getTimeOfDaySeconds());
  }

  /**
   * 1日の経過率(0.0-1.0)を返す。
   *
   * @return 経過率
   */
  public double getTimeOfDayNormalized() {
    return withReadLock(() -> gameTime.getTimeOfDayNormalized());
  }

  /**
   * 1日の長さ(秒)を返す。
   *
   * @return 1日の長さ(秒)
   */
  public double getDayLengthSeconds() {
    return withReadLock(() -> gameTime.getDayLengthSeconds());
  }

  /**
   * 日数を設定する。
   *
   * @param day 日数
   */
  public void setDay(int day) {
    withWriteLock(() -> {
      gameTime.setDayCount(day);
      return null;
    });
  }

  /**
   * 読み込みロック内で処理を実行する。
   *
   * @param supplier 実行処理
   * @param <T>      返り値型
   * @return 実行結果
   */
  private <T> T withReadLock(Supplier<T> supplier) {
    Lock readLock = stateLock.readLock();
    readLock.lock();
    try {
      return supplier.get();
    } finally {
      readLock.unlock();
    }
  }

  /**
   * 書き込みロック内で処理を実行する。
   *
   * @param supplier 実行処理
   * @param <T>      返り値型
   * @return 実行結果
   */
  private <T> T withWriteLock(Supplier<T> supplier) {
    Lock writeLock = stateLock.writeLock();
    writeLock.lock();
    try {
      return supplier.get();
    } finally {
      writeLock.unlock();
    }
  }
}
