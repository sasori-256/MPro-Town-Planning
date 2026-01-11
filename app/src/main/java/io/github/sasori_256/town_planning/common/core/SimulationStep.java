package io.github.sasori_256.town_planning.common.core;

/**
 * ゲーム世界の1ステップ更新を表すインターフェース。
 */
public interface SimulationStep {
  /**
   * ゲーム世界を指定時間だけ進める。
   *
   * @param dt 経過時間(秒)
   */
  void step(double dt);
}
