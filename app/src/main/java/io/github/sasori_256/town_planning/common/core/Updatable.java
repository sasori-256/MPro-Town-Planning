package io.github.sasori_256.town_planning.common.core;

/**
 * フレームの更新ごとに呼び出されるメソッドを持つインターフェース。
 * ゲームの状態を更新するために使用される。
 */
public interface Updatable {
  void tick();
}
