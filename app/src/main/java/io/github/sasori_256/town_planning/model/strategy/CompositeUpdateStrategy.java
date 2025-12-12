package io.github.sasori_256.town_planning.model.strategy;

import io.github.sasori_256.town_planning.model.GameContext;
import io.github.sasori_256.town_planning.model.GameObject;
import io.github.sasori_256.town_planning.core.strategy.UpdateStrategy;
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
  public void update(GameContext context, GameObject self) {
    for (UpdateStrategy strategy : strategies) {
      strategy.update(context, self);
    }
  }
}
