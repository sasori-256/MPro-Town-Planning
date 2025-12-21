package io.github.sasori_256.town_planning.common.ui.gameObjectSelect.controller;

import java.util.ArrayList;
import java.util.function.Supplier;

import io.github.sasori_256.town_planning.gameobject.building.Building;
import io.github.sasori_256.town_planning.map.controller.GameMapController;
import io.github.sasori_256.town_planning.map.controller.handler.PlaceBuildingHandler;
import io.github.sasori_256.town_planning.map.model.MapContext;

public class BuildingNode implements MenuNode {
    private final String name;
    private final Supplier<Building> generator;
    private final GameMapController gameMapController;
    private final MapContext mapContext;

    public BuildingNode(String name, Supplier<Building> generator, GameMapController gameMapController, MapContext mapContext) {
        this.name = name;
        this.generator = generator;
        this.gameMapController = gameMapController;
        this.mapContext = mapContext;
    }

    @Override
    public String getName() { return name;}

    @Override
    public Boolean isLeaf() { return true; }

    @Override
    public ArrayList<MenuNode> getChildren() { 
        System.err.println("Error: Attempted to get children from BuildingNode");
        return null;   
    }

    @Override
    public void actionPerformed(java.awt.event.ActionEvent e) {
        //TODO: Viewと連携する.
        gameMapController.setSelectedEntity(generator);
        gameMapController.setActionOnClick(new PlaceBuildingHandler(mapContext, gameMapController));
    }
}