package io.github.sasori_256.town_planning.controller;

import java.awt.geom.Point2D;
import java.util.function.Consumer;

import io.github.sasori_256.town_planning.controller.moc.BuildingGameObject;
import io.github.sasori_256.town_planning.controller.moc.MapContext;


public class PlaceBuildingHandler implements Consumer<Point2D.Double>{
    private MapContext mapContext;
    private BuildingGameObject selectedBuilding;
    private GameMapController gameMapController;

    public PlaceBuildingHandler(MapContext mapContext, BuildingGameObject selectedBuilding, GameMapController gameMapController) {
        this.mapContext = mapContext;
        this.selectedBuilding = selectedBuilding;
        this.gameMapController = gameMapController;
    }

    @Override
    public void accept(Point2D.Double isoPoint) {
        isoPoint.x = Math.floor(isoPoint.x);
        isoPoint.y = Math.floor(isoPoint.y);
        mapContext.placeBuilding(isoPoint, selectedBuilding);
        gameMapController.setActionOnClick(new ClickGameMapHandler());
    }
    
}
