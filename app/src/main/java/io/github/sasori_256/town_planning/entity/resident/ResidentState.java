package io.github.sasori_256.town_planning.entity.resident;

/**
 * 住民の状態を表す列挙型。
 */
public enum ResidentState {
  IDLE,
  WORKING,
  RELAXING,
  TRAVELING,
  SLEEPING,
  EATING,
  RETURNING_HOME,
  PANICKING, // 天災などの緊急事態
  DEAD;

  ResidentState() {
  }
}
