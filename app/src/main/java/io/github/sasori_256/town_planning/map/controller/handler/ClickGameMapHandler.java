package io.github.sasori_256.town_planning.map.controller.handler;

import java.awt.geom.Point2D;
import java.util.function.BiConsumer;
import java.util.function.Function;

import io.github.sasori_256.town_planning.entity.model.BaseGameEntity;

public class ClickGameMapHandler implements BiConsumer<Point2D.Double, Function<Point2D.Double, ? extends BaseGameEntity>> {
  @Override
  public void accept(Point2D.Double isoPoint, Function<Point2D.Double, ? extends BaseGameEntity> entityGenerator) {
    // TODO:
    System.out.println("Clicked at Iso Coordinates: (" + isoPoint.x + ", " + isoPoint.y + ")");
  }

}
