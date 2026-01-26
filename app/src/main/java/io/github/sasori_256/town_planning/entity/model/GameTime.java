package io.github.sasori_256.town_planning.entity.model;

/**
 * ゲーム内時間と日付の進行を管理するクラス。
 */
public class GameTime {
  private int dayCount = 1;
  private double timeOfDaySeconds = 0.0;
  private double dayLengthSeconds = 10.0;
  private double timeScale = 1.0;

  /**
   * 経過時間を進め、進んだ日数を返す。
   *
   * @param realDeltaSeconds 実時間の経過秒
   * @return 進んだ日数
   */
  public int advance(double realDeltaSeconds) {
    if (realDeltaSeconds <= 0) {
      return 0;
    }
    double scaled = realDeltaSeconds * timeScale;
    timeOfDaySeconds += scaled;
    int days = 0;
    while (timeOfDaySeconds >= dayLengthSeconds) {
      timeOfDaySeconds -= dayLengthSeconds;
      dayCount++;
      days++;
    }
    return days;
  }

  /**
   * 現在の日数を返す。
   */
  public int getDayCount() {
    return dayCount;
  }

  /**
   * 1日の経過秒を返す。
   */
  public double getTimeOfDaySeconds() {
    return timeOfDaySeconds;
  }

  /**
   * 1日の経過率(0.0-1.0)を返す。
   */
  public double getTimeOfDayNormalized() {
    if (dayLengthSeconds <= 0.0) {
      return 0.0;
    }
    return timeOfDaySeconds / dayLengthSeconds;
  }

  /**
   * 1日の長さ(秒)を返す。
   */
  public double getDayLengthSeconds() {
    return dayLengthSeconds;
  }

  /**
   * 1日の長さ(秒)を設定する。
   *
   * @param seconds 1日の長さ
   */
  public void setDayLengthSeconds(double seconds) {
    if (seconds <= 0.0) {
      throw new IllegalArgumentException("dayLengthSeconds must be positive.");
    }
    this.dayLengthSeconds = seconds;
    if (timeOfDaySeconds >= dayLengthSeconds) {
      timeOfDaySeconds = 0.0;
    }
  }

  /**
   * 時間スケールを設定する。
   *
   * @param scale 実時間に対する倍率 (0.0は一時停止を表す)
   */
  public void setTimeScale(double scale) {
    if (scale < 0.0) {
      throw new IllegalArgumentException("timeScale must be non-negative.");
    }
    this.timeScale = scale;
  }

  /**
   * 現在の日数を設定する。
   *
   * @param day 日数
   */
  public void setDayCount(int day) {
    if (day < 1) {
      throw new IllegalArgumentException("dayCount must be >= 1.");
    }
    this.dayCount = day;
  }

  /**
   * 1日の経過秒を設定する。
   *
   * @param seconds 1日の経過秒
   */
  public void setTimeOfDaySeconds(double seconds) {
    if (seconds < 0.0 || seconds >= dayLengthSeconds) {
      throw new IllegalArgumentException(
          "timeOfDaySeconds must be within [0, dayLengthSeconds).");
    }
    this.timeOfDaySeconds = seconds;
  }
}
