package io.github.sasori_256.town_planning.common.core.strategy;

import io.github.sasori_256.town_planning.gameObject.model.GameContext;
import io.github.sasori_256.town_planning.gameObject.model.GameObject;

@FunctionalInterface
public interface UpdateStrategy {
  void update(GameContext context, GameObject self);
}
