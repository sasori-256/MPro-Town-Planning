package io.github.sasori_256.town_planning.common.ui.gameObjectSelect.controller;

import java.util.ArrayList;
import java.util.function.Function;
import java.awt.geom.Point2D;

import io.github.sasori_256.town_planning.entity.building.Building;
import io.github.sasori_256.town_planning.entity.building.BuildingType;
import io.github.sasori_256.town_planning.map.controller.GameMapController;
import io.github.sasori_256.town_planning.map.controller.handler.PlaceBuildingHandler;
import io.github.sasori_256.town_planning.map.model.MapContext;

public class BuildingNode implements MenuNode {
    private final BuildingType type;
    private final Function<Point2D.Double, Building> generator;
    private final GameMapController gameMapController;
    private final MapContext mapContext;

    public BuildingNode(BuildingType buildingType, Function<Point2D.Double, Building> generator, GameMapController gameMapController, MapContext mapContext) {
        this.type = buildingType;
        this.generator = generator;
        this.gameMapController = gameMapController;
        this.mapContext = mapContext;
    }

    public BuildingType getType() { return type; }
    
    @Override
    public String getName() { return type.getDisplayName(); }

    @Override
    public boolean isLeaf() { return true; }

    @Override
    public ArrayList<MenuNode> getChildren() { 
        throw new UnsupportedOperationException("BuildingNode is a leaf node and does not have children");
    }

    @Override
    public void actionPerformed(java.awt.event.ActionEvent e) {
        //TODO: Viewと連携する.
        gameMapController.setSelectedEntityGenerator(generator);
        gameMapController.setActionOnClick(new PlaceBuildingHandler(mapContext, gameMapController));
    }
}