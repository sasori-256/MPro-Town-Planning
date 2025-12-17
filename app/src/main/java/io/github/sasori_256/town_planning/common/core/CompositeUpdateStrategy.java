package io.github.sasori_256.town_planning.common.core;

import io.github.sasori_256.town_planning.common.core.strategy.UpdateStrategy;
import io.github.sasori_256.town_planning.gameObject.model.BaseGameEntity;
import io.github.sasori_256.town_planning.gameObject.model.GameAction;
import io.github.sasori_256.town_planning.gameObject.model.GameContext;
import io.github.sasori_256.town_planning.gameObject.model.GameEffect;

/**
 * 1つのGameAction（排他）と複数のGameEffect（並行）を管理し実行するStrategy
 */
public class CompositeUpdateStrategy implements UpdateStrategy {
  private GameAction action;
  private final CompositeGameEffect compositeEffect = new CompositeGameEffect();

  public CompositeUpdateStrategy() {
  }

  public void setAction(GameAction action) {
    this.action = action;
  }

  public void addEffect(GameEffect effect) {
    this.compositeEffect.add(effect);
  }

  public void removeEffect(GameEffect effect) {
    this.compositeEffect.remove(effect);
  }

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
