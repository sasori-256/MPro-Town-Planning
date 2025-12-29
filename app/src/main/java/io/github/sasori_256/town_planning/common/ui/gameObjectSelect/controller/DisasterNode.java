package io.github.sasori_256.town_planning.common.ui.gameObjectSelect.controller;

import java.util.ArrayList;
import java.util.function.Function;
import java.awt.geom.Point2D;

import io.github.sasori_256.town_planning.entity.disaster.DisasterType;
import io.github.sasori_256.town_planning.entity.model.BaseGameEntity;
import io.github.sasori_256.town_planning.map.controller.GameMapController;
import io.github.sasori_256.town_planning.map.controller.handler.ActionDisasterHandler;

public class DisasterNode implements MenuNode {
    private final DisasterType type;
    private final Function<Point2D.Double, ? extends BaseGameEntity> generator;
    private final GameMapController gameMapController;

    public DisasterNode(DisasterType disasterType, Function<Point2D.Double, ? extends BaseGameEntity> generator, GameMapController gameMapController) {
        this.type = disasterType;
        this.generator = generator;
        this.gameMapController = gameMapController;
    }

    public DisasterType getType() { return type; }

    @Override
    public String getName() { return type.getDisplayName(); }

    @Override
    public boolean isLeaf() { return true; }

    @Override
    public ArrayList<MenuNode> getChildren() {
        throw new UnsupportedOperationException("DisasterNode is a leaf node and has no children.");
    }

    @Override
    public void actionPerformed(java.awt.event.ActionEvent e) {
        //TODO: Viewと連携する.
        gameMapController.setSelectedEntityGenerator(generator);
        gameMapController.setActionOnClick(new ActionDisasterHandler());
    }
}