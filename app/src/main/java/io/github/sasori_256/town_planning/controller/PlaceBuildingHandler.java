package io.github.sasori_256.town_planning.controller;

import java.awt.geom.Point2D;
import java.util.function.Consumer;

// import io.github.sasori_256.town_planning.controller.moc.BuildingGameObject;
// import io.github.sasori_256.town_planning.controller.moc.MapContext;
import io.github.sasori_256.town_planning.model.MapContext;
import io.github.sasori_256.town_planning.model.entity.BuildingObject;



public class PlaceBuildingHandler implements Consumer<Point2D.Double>{
    private MapContext mapContext;
    private BuildingObject selectedBuilding;
    private GameMapController gameMapController;

    public PlaceBuildingHandler(MapContext mapContext, BuildingObject selectedBuilding, GameMapController gameMapController) {
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
