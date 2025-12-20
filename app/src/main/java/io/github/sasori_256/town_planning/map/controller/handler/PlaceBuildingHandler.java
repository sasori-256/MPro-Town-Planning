package io.github.sasori_256.town_planning.map.controller.handler;

import java.awt.geom.Point2D;
import java.util.function.Consumer;

import io.github.sasori_256.town_planning.entity.building.Building;
import io.github.sasori_256.town_planning.map.controller.GameMapController;
import io.github.sasori_256.town_planning.map.model.MapContext;

public class PlaceBuildingHandler implements Consumer<Point2D.Double> {
  private MapContext mapContext;
  private Building selectedBuilding;
  private GameMapController gameMapController;

  public PlaceBuildingHandler(MapContext mapContext, Building selectedBuilding, GameMapController gameMapController) {
    this.mapContext = mapContext;
    this.selectedBuilding = selectedBuilding;
    this.gameMapController = gameMapController;
  }

  @Override
  public void accept(Point2D.Double isoPoint) {
    Point2D.Double flooredPoint = new Point2D.Double(Math.floor(isoPoint.x), Math.floor(isoPoint.y));
    mapContext.placeBuilding(flooredPoint, selectedBuilding);
    gameMapController.setActionOnClick(new ClickGameMapHandler());
  }
}
