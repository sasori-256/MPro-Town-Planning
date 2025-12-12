package io.github.sasori_256.town_planning.core.strategy;

import io.github.sasori_256.town_planning.model.GameObject;
import io.github.sasori_256.town_planning.model.GameContext;

@FunctionalInterface
public interface UpdateStrategy {
  void update(GameContext context, GameObject self);
}
