package io.github.sasori_256.town_planning;

import java.util.concurrent.locks.ReadWriteLock;
import javax.swing.SwingUtilities;

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

    EventBus eventBus = new EventBus();
    GameModel gameModel = new GameModel(eventBus);
    GameMap gameMap = gameModel.getGameMap();
    ReadWriteLock stateLock = gameModel.getStateLock();
    Camera camera = new Camera(1, WIDTH, HEIGHT, eventBus);
    GameMapController gameMapController = new GameMapController(camera, stateLock);
    SwingUtilities.invokeLater(() -> {
      GameWindow gameWindow = new GameWindow(
          gameMapController, gameMap, camera, WIDTH, HEIGHT, eventBus, gameMapController, stateLock);
      gameModel.startGameLoop(gameWindow::repaint);
    });
  }
}
