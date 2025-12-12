package io.github.sasori_256.town_planning.core.strategy;

import io.github.sasori_256.town_planning.core.GameObject;
import java.awt.Graphics2D;

@FunctionalInterface
public interface RenderStrategy {
  void render(Graphics2D g, GameObject self);
}
