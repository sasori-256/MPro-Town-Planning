package io.github.sasori_256.town_planning.common.core.strategy;

import io.github.sasori_256.town_planning.entity.model.GameContext;
import io.github.sasori_256.town_planning.entity.model.BaseGameEntity;

/**
 * エンティティの状態を更新するためのStrategyを定義するインターフェース。
 */
@FunctionalInterface
public interface UpdateStrategy {
  /**
   * エンティティの更新処理を行う。
   *
   * @param context ゲームコンテキスト
   * @param self    対象エンティティ
   */
  void update(GameContext context, BaseGameEntity self);
}
