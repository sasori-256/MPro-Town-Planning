package io.github.sasori_256.town_planning.common.core.strategy;

import io.github.sasori_256.town_planning.entity.model.BaseGameEntity;
import io.github.sasori_256.town_planning.entity.model.GameAction;
import io.github.sasori_256.town_planning.entity.model.GameContext;
import io.github.sasori_256.town_planning.entity.model.GameEffect;

/**
 * 1つのGameAction（排他）と複数のGameEffect（並行）を管理し実行するStrategy
 */
public class CompositeUpdateStrategy implements UpdateStrategy {
  private GameAction action;
  private final CompositeGameEffect compositeEffect = new CompositeGameEffect();

  /**
   * 複合更新ストラテジーを生成する。
   */
  public CompositeUpdateStrategy() {
  }

  /**
   * 排他実行するアクションを設定する。
   *
   * @param action アクション
   */
  public void setAction(GameAction action) {
    this.action = action;
  }

  /**
   * 併用するエフェクトを追加する。
   *
   * @param effect エフェクト
   */
  public void addEffect(GameEffect effect) {
    this.compositeEffect.add(effect);
  }

  /**
   * 併用するエフェクトを削除する。
   *
   * @param effect エフェクト
   */
  public void removeEffect(GameEffect effect) {
    this.compositeEffect.remove(effect);
  }

  /** {@inheritDoc} */
  @Override
  public void update(GameContext context, BaseGameEntity self) {
    // 1. 並行エフェクトの実行（常に実行）
    compositeEffect.execute(context, self);

    // 2. 排他アクションの実行（設定されていれば実行）
    if (action != null) {
      action.execute(context, self);
    }
  }
}
