package io.github.sasori_256.town_planning.model;

/**
 * GameObjectの属性マップで使用するキー定数。
 */
public class ResidentAttributes {
  // 基本ステータス
  public static final String TYPE = "resident_type"; // ResidentType enum
  public static final String AGE = "age"; // Double (years/days)
  public static final String MAX_AGE = "max_age"; // Double
  public static final String FAITH = "faith"; // Integer (0-100)
  public static final String STATE = "state"; // ResidentState enum

  // 状態定義
  public enum State {
    ALIVE,
    DEAD,
    SOUL_HARVESTED
  }

  private ResidentAttributes() {
  } // Prevent instantiation
}
