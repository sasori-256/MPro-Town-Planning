package io.github.sasori_256.town_planning.common.core;

/**
 * アニメーション更新が必要なエンティティ向けのインターフェース。
 */
public interface Animatable {
  /**
   * アニメーションの更新を行う。
   *
   * @param dt 経過時間(秒)
   */
  void advanceAnimation(double dt);
}

