package io.github.sasori_256.town_planning;

import io.github.sasori_256.town_planning.common.event.EventBus;
import io.github.sasori_256.town_planning.common.ui.GameWindow;
import io.github.sasori_256.town_planning.entity.Camera;
import io.github.sasori_256.town_planning.entity.model.GameModel;
import io.github.sasori_256.town_planning.map.controller.GameMapController;
import io.github.sasori_256.town_planning.map.model.GameMap;

public class App {
  public static void main(String[] args) {
    final int WIDTH = 640;
    final int HEIGHT = 640;
    final int MAP_WIDTH = 100;
    final int MAP_HEIGHT = 100;

    EventBus eventBus = new EventBus();
    GameModel gameModel = new GameModel(MAP_WIDTH, MAP_HEIGHT, eventBus);
    GameMap gameMap = gameModel.getGameMap();
    Camera camera = new Camera(1, WIDTH, HEIGHT, MAP_WIDTH, MAP_HEIGHT, eventBus);
    GameMapController gameMapController = new GameMapController(camera);
    GameWindow gameWindow = new GameWindow(gameMapController, gameMap, camera, WIDTH, HEIGHT, eventBus, gameMapController);
  }
}
