package io.github.sasori_256.town_planning.common.core;

/**
 * アニメーション更新が必要なエンティティ向けのインターフェース。
 */
public interface Animatable {
  /**
   * アニメーションの更新を行う
   *
   * これを使うときは書き込みロックが既に保持されていないと競合が発生する可能性がある。
   *
   */
  void advanceAnimation();
}
