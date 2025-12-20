package io.github.sasori_256.town_planning.entity.model;

/**
 * エンティティに適用される並行可能な効果（パッシブスキル、バフ、環境効果、施設機能など）。
 * CompositeUpdateStrategyによって毎フレーム execute が呼ばれる。
 */
public interface GameEffect {
  /**
   * エンティティに効果を適用する。
   *
   * @param context ゲーム内の環境情報
   * @param self    効果を適用するエンティティ自身
   */
  void execute(GameContext context, BaseGameEntity self);
}
