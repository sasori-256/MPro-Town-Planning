package io.github.sasori_256.town_planning.map.controller.handler;

import java.awt.geom.Point2D;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import io.github.sasori_256.town_planning.gameobject.model.BaseGameEntity;

public class ActionDisasterHandler implements BiConsumer<Point2D.Double, Supplier<? extends BaseGameEntity>> {
  @Override
  public void accept(Point2D.Double isoPoint, Supplier<? extends BaseGameEntity> entity) {
    // TODO:
    System.out.println("Action Disaster at: (" + isoPoint.x + ", " + isoPoint.y + ")");
  }

}
