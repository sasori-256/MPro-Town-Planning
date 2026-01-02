package io.github.sasori_256.town_planning;

import java.awt.geom.Point2D;

import io.github.sasori_256.town_planning.common.event.EventBus;
import io.github.sasori_256.town_planning.common.ui.GameWindow;
import io.github.sasori_256.town_planning.entity.Camera;
import io.github.sasori_256.town_planning.entity.building.Building;
import io.github.sasori_256.town_planning.entity.building.BuildingType;
import io.github.sasori_256.town_planning.entity.model.GameModel;
import io.github.sasori_256.town_planning.map.controller.GameMapController;
import io.github.sasori_256.town_planning.map.controller.handler.PlaceBuildingHandler;
import io.github.sasori_256.town_planning.map.model.GameMap; //いったん動かすために記載。Entity選択メニューが実装されたら消す。

public class App {
  public static void main(String[] args) {
    final int WIDTH = 640;
    final int HEIGHT = 640;

    EventBus eventBus = new EventBus();
    GameModel gameModel = new GameModel(eventBus);
    GameMap gameMap = gameModel.getGameMap();
    Camera camera = new Camera(1, WIDTH, HEIGHT, eventBus);
    GameMapController gameMapController = new GameMapController(camera);
    gameMapController.setActionOnClick(new PlaceBuildingHandler(gameMap, gameMapController)); // いったん動かすために記載。Entity選択メニューが実装されたら消す。
    gameMapController.setSelectedEntityGenerator((point) -> new Building(point, BuildingType.HOUSE)); // いったん動かすために記載。Entity選択メニューが実装されたら消す。
    GameWindow gameWindow = new GameWindow(gameMapController, gameMap, camera, WIDTH, HEIGHT, eventBus,
        gameMapController);
  }
}
