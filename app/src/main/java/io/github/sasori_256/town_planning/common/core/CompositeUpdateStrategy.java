package io.github.sasori_256.town_planning.common.core;

import io.github.sasori_256.town_planning.common.core.strategy.UpdateStrategy;
import io.github.sasori_256.town_planning.gameObject.model.GameContext;
import io.github.sasori_256.town_planning.gameObject.model.BaseGameEntity;

import java.util.Arrays;
import java.util.List;

/**
 * 複数のUpdateStrategyを順番に実行するコンポジット戦略。
 */
public class CompositeUpdateStrategy implements UpdateStrategy {
  private final List<UpdateStrategy> strategies;

  public CompositeUpdateStrategy(UpdateStrategy... strategies) {
    this.strategies = Arrays.asList(strategies);
  }

  @Override
  public void update(GameContext context, BaseGameEntity self) {
    for (UpdateStrategy strategy : strategies) {
      strategy.update(context, self);
    }
  }
}
