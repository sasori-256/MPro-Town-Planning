package io.github.sasori_256.town_planning.common.core;

import io.github.sasori_256.town_planning.gameobject.model.GameContext;

/**
 * フレームの更新ごとに呼び出されるメソッドを持つインターフェース。
 * ゲームの状態を更新するために使用される。
 */
public interface Updatable {
  void update(GameContext context);

  default void onAddedToGame(GameContext context) {
    // 何もしない
  }
}
