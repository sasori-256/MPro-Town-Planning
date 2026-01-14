package io.github.sasori_256.town_planning.common.ui.gameObjectSelect.controller;

import java.util.ArrayList;
import java.util.function.Function;
import java.awt.geom.Point2D;
import io.github.sasori_256.town_planning.entity.building.Building;
import io.github.sasori_256.town_planning.entity.building.BuildingType;
import io.github.sasori_256.town_planning.entity.model.GameModel;
import io.github.sasori_256.town_planning.map.controller.GameMapController;
import io.github.sasori_256.town_planning.map.controller.handler.PlaceBuildingHandler;

/**
 * 建物選択用のメニューノード。
 */
public class BuildingNode implements MenuNode {
    private final BuildingType type;
    private final Function<Point2D.Double, Building> generator;
    private final GameMapController gameMapController;
    private final GameModel gameModel;

    /**
     * 建物ノードを生成する。
     *
     * @param buildingType      建物種別
     * @param generator         建物生成関数
     * @param gameMapController マップコントローラ
     * @param gameModel         ゲームモデル
     */
    public BuildingNode(BuildingType buildingType, Function<Point2D.Double, Building> generator,
        GameMapController gameMapController, GameModel gameModel) {
        this.type = buildingType;
        this.generator = generator;
        this.gameMapController = gameMapController;
        this.gameModel = gameModel;
    }

    /**
     * 建物種別を返す。
     *
     * @return 建物種別
     */
    public BuildingType getType() { return type; }

    /** {@inheritDoc} */
    @Override
    public String getName() { return type.getDisplayName(); }

    /** {@inheritDoc} */
    @Override
    public boolean isLeaf() { return true; }

    /** {@inheritDoc} */
    @Override
    public ArrayList<MenuNode> getChildren() {
        throw new UnsupportedOperationException("BuildingNode is a leaf node and does not have children");
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(java.awt.event.ActionEvent e) {
        //TODO: Viewと連携する.
        gameMapController.setSelectedEntityGenerator(generator);
        gameMapController.setActionOnClick(new PlaceBuildingHandler(gameModel, gameMapController));
    }
}
