package io.github.sasori_256.town_planning.common.core.strategy;

import io.github.sasori_256.town_planning.gameobject.model.GameContext;
import io.github.sasori_256.town_planning.gameobject.model.BaseGameEntity;

@FunctionalInterface
public interface UpdateStrategy {
  void update(GameContext context, BaseGameEntity self);
}
