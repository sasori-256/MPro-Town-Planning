package io.github.sasori_256.town_planning;

import java.awt.geom.Point2D;

import io.github.sasori_256.town_planning.common.event.EventBus;
import io.github.sasori_256.town_planning.common.ui.GameWindow;
import io.github.sasori_256.town_planning.entity.Camera;
import io.github.sasori_256.town_planning.entity.model.GameModel;
import io.github.sasori_256.town_planning.map.controller.GameMapController;
import io.github.sasori_256.town_planning.map.model.GameMap;

public class Main {
  public static void main(String[] args) {
    final int WIDTH = 640;
    final int HEIGHT = 640;

    EventBus eventBus = new EventBus();
    GameModel gameModel = new GameModel(eventBus);
    GameMap gameMap = gameModel.getGameMap();
    Camera camera = new Camera(1, new Point2D.Double(0, 0));
    GameMapController gameMapController = new GameMapController(camera, gameMap);
    GameWindow gameWindow = new GameWindow(gameMapController, gameMap, camera, WIDTH, HEIGHT);
  }
}
