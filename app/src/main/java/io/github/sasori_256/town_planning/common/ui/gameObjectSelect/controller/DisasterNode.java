package io.github.sasori_256.town_planning.common.ui.gameObjectSelect.controller;

import java.util.ArrayList;
import java.util.function.Function;
import java.awt.geom.Point2D;

import io.github.sasori_256.town_planning.entity.disaster.Disaster;
import io.github.sasori_256.town_planning.entity.disaster.DisasterType;
import io.github.sasori_256.town_planning.entity.model.GameModel;
import io.github.sasori_256.town_planning.map.controller.GameMapController;
import io.github.sasori_256.town_planning.map.controller.handler.ActionDisasterHandler;
import io.github.sasori_256.town_planning.map.model.MapContext;

/**
 * 災害選択用のメニューノード。
 */
public class DisasterNode implements MenuNode {
    private final DisasterType type;
    private final Function<Point2D.Double, Disaster> generator;
    private final GameMapController gameMapController;
    private final GameModel gameModel;
    private final MapContext mapContext;

    /**
     * 災害ノードを生成する。
     *
     * @param disasterType      災害種別
     * @param generator         災害生成関数
     * @param gameMapController マップコントローラ
     * @param gameModel         ゲームモデル
     * @param mapContext        マップ参照
     */
    public DisasterNode(DisasterType disasterType, Function<Point2D.Double, Disaster> generator, GameMapController gameMapController,
        GameModel gameModel, MapContext mapContext) {
        this.type = disasterType;
        this.generator = generator;
        this.gameMapController = gameMapController;
        this.gameModel = gameModel;
        this.mapContext = mapContext;
    }

    /**
     * 災害種別を返す。
     *
     * @return 災害種別
     */
    public DisasterType getType() { return type; }

    /** {@inheritDoc} */
    @Override
    public String getName() { return type.getDisplayName(); }

    /** {@inheritDoc} */
    @Override
    public boolean isLeaf() { return true; }

    /** {@inheritDoc} */
    @Override
    public ArrayList<MenuNode> getChildren() {
        throw new UnsupportedOperationException("DisasterNode is a leaf node and has no children.");
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(java.awt.event.ActionEvent e) {
        //TODO: Viewと連携する.
        gameMapController.setSelectedEntityGenerator(generator);
        gameMapController.setActionOnClick(new ActionDisasterHandler(gameModel, gameMapController, mapContext));
    }
}
