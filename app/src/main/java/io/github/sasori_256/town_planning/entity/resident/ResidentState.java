package io.github.sasori_256.town_planning.entity.resident;

/**
 * 住民の状態を表す列挙型。
 */
public enum ResidentState {
  /** 家にいて非表示の状態。 */
  AT_HOME,
  IDLE,
  WORKING,
  RELAXING,
  TRAVELING,
  /** 引っ越し中。 */
  RELOCATING,
  SLEEPING,
  EATING,
  RETURNING_HOME,
  PANICKING, // 天災などの緊急事態
  DEAD;

  ResidentState() {
  }
}
