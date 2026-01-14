package io.github.sasori_256.town_planning.common.ui.gameObjectSelect.controller;

import java.util.HashMap;
import java.util.Map;
import io.github.sasori_256.town_planning.entity.building.Building;
import io.github.sasori_256.town_planning.entity.building.BuildingType;
import io.github.sasori_256.town_planning.entity.disaster.Disaster;
import io.github.sasori_256.town_planning.entity.disaster.DisasterType;
import io.github.sasori_256.town_planning.entity.model.CategoryType;
import io.github.sasori_256.town_planning.entity.model.GameModel;
import io.github.sasori_256.town_planning.map.controller.GameMapController;
import io.github.sasori_256.town_planning.map.model.MapContext;

/**
 * メニューノードを組み立てるユーティリティ。
 */
public class NodeMenuInitializer {
    /**
     * ノードメニューの初期化を行う。
     * 創造モードと天災モードの親となるルートノードを返す。
     *
     * @param gameMapController ゲームマップコントローラ
     * @param gameModel         ゲームモデル
     * @return ルートのCategoryNode
     */
    public static CategoryNode setup(GameMapController gameMapController, GameModel gameModel) {
        CategoryNode root = new CategoryNode("root");
        CategoryNode creativeRoot = new CategoryNode("創造");
        CategoryNode disasterRoot = new CategoryNode("天災");
        root.add(creativeRoot);
        root.add(disasterRoot);

        MapContext mapContext = gameModel.getGameMap();

        // Buildingのノード追加
        Map<CategoryType, CategoryNode> categoryNodeMap = new HashMap<>();
        for(BuildingType type: BuildingType.values()){
            CategoryNode categoryNode = categoryNodeMap.computeIfAbsent(type.getCategory(), cat -> {
                CategoryNode newCategoryNode = new CategoryNode(cat.getDisplayName());
                creativeRoot.add(newCategoryNode);
                return newCategoryNode;
            });

            BuildingNode buildingNode = new BuildingNode(type, (point) -> new Building(point, type),
                gameMapController, gameModel);
            categoryNode.add(buildingNode);
        }

        // Disasterのノード追加
        categoryNodeMap = new HashMap<>();
        for(DisasterType type: DisasterType.values()){
            CategoryNode categoryNode = categoryNodeMap.computeIfAbsent(type.getCategory(), cat -> {
                CategoryNode newCategoryNode = new CategoryNode(cat.getDisplayName());
                disasterRoot.add(newCategoryNode);
                return newCategoryNode;
            });

            DisasterNode disasterNode = new DisasterNode(type, (point) -> new Disaster(point, type),
                gameMapController, gameModel, mapContext);
            categoryNode.add(disasterNode);
        }
        return root;
    }
}
