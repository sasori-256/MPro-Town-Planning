package io.github.sasori_256.town_planning.entity.resident;

/**
 * 住民の状態を表す列挙型。
 */
public enum ResidentState {
  /** 家にいて非表示の状態。 */
  AT_HOME,
  /** 待機中。 */
  IDLE,
  /** 作業中。 */
  WORKING,
  /** 休憩中。 */
  RELAXING,
  /** 移動中。 */
  TRAVELING,
  /** 引っ越し中。 */
  RELOCATING,
  /** 睡眠中。 */
  SLEEPING,
  /** 食事中。 */
  EATING,
  /** 帰宅中。 */
  RETURNING_HOME,
  /** 天災などの緊急事態。 */
  PANICKING,
  /** 死亡状態。 */
  DEAD;

  ResidentState() {
  }
}
