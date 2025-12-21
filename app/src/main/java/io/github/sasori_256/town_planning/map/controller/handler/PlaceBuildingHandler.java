package io.github.sasori_256.town_planning.map.controller.handler;

import java.awt.geom.Point2D;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import io.github.sasori_256.town_planning.gameobject.building.Building;
import io.github.sasori_256.town_planning.gameobject.model.BaseGameEntity;
import io.github.sasori_256.town_planning.map.controller.GameMapController;
import io.github.sasori_256.town_planning.map.model.MapContext;

public class PlaceBuildingHandler implements BiConsumer<Point2D.Double, Supplier<? extends BaseGameEntity>> {
  private MapContext mapContext;
  private GameMapController gameMapController;

  public PlaceBuildingHandler(MapContext mapContext, GameMapController gameMapController) {
    this.mapContext = mapContext;
    this.gameMapController = gameMapController;
  }

  @Override
  public void accept(Point2D.Double isoPoint, Supplier<? extends BaseGameEntity> selectedBuilding) {
    Point2D.Double flooredPoint = new Point2D.Double(Math.floor(isoPoint.x), Math.floor(isoPoint.y));
    BaseGameEntity entity = selectedBuilding.get();
    if (entity instanceof Building building) { mapContext.placeBuilding(flooredPoint, building); }
    else{ System.err.println("Error: Trying to place an building that is not a Building."); }
    gameMapController.setSelectedEntity(() -> null);
    gameMapController.setActionOnClick(new ClickGameMapHandler());
  }
}
