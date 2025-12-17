package io.github.sasori_256.town_planning.gameObject.model;

/**
 * エンティティに適用される並行可能な効果（パッシブスキル、バフ、環境効果、施設機能など）。
 * CompositeUpdateStrategyによって毎フレーム execute が呼ばれる。
 */
public interface GameEffect {
  void execute(GameContext context, BaseGameEntity self);
}
