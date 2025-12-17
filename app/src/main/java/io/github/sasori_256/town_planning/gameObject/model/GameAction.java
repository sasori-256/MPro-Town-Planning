package io.github.sasori_256.town_planning.gameObject.model;

/**
 * エンティティがとる排他的な行動（移動、攻撃、作業など）
 * CompositeUpdateStrategyによって毎フレーム execute が呼ばれる
 */
public interface GameAction {
  void execute(GameContext context, BaseGameEntity self);
}
