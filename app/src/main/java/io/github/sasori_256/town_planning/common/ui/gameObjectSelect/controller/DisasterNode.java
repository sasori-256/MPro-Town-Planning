package io.github.sasori_256.town_planning.common.ui.gameObjectSelect.controller;

import java.util.ArrayList;
import java.util.function.Supplier;

import io.github.sasori_256.town_planning.gameobject.model.BaseGameEntity;
import io.github.sasori_256.town_planning.map.controller.GameMapController;
import io.github.sasori_256.town_planning.map.controller.handler.ActionDisasterHandler;

public class DisasterNode implements MenuNode {
    private final String name;
    private final Supplier<? extends BaseGameEntity> generator;
    private final GameMapController gameMapController;

    public DisasterNode(String name, Supplier<? extends BaseGameEntity> generator, GameMapController gameMapController) {
        this.name = name;
        this.generator = generator;
        this.gameMapController = gameMapController;
    }

    @Override
    public String getName() { return name;}

    @Override
    public Boolean isLeaf() { return true; }

    @Override
    public ArrayList<MenuNode> getChildren() { 
        System.err.println("Error: Attempted to get children from DisasterNode");
        return null;
    }

    @Override
    public void actionPerformed(java.awt.event.ActionEvent e) {
        //TODO: Viewと連携する.
        gameMapController.setSelectedEntity(generator);
        gameMapController.setActionOnClick(new ActionDisasterHandler());
    }
}