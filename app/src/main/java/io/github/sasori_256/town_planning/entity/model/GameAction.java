package io.github.sasori_256.town_planning.entity.model;

/**
 * エンティティがとる排他的な行動（移動、攻撃、作業など）
 * CompositeUpdateStrategyによって毎フレーム execute が呼ばれる
 */
public interface GameAction {
  /**
   * エンティティの行動を実行する。
   *
   * @param context ゲーム内の環境情報
   * @param self    行動を実行するエンティティ自身
   */
  void execute(GameContext context, BaseGameEntity self);
}
