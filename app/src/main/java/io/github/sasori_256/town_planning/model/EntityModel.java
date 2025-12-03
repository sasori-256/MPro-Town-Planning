package io.github.sasori_256.town_planning.model;

import java.awt.geom.Point2D;

abstract class EntityModel {
  public Point2D worldPos;

  public boolean draw() {
    return false;
  }

  public boolean redraw() {
    return false;
  }
}
