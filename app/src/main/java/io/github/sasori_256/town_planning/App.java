package io.github.sasori_256.town_planning;

import java.awt.geom.Point2D;
import java.util.concurrent.locks.ReadWriteLock;
import javax.swing.SwingUtilities;
import io.github.sasori_256.town_planning.common.event.EventBus;
import io.github.sasori_256.town_planning.common.ui.GameWindow;
import io.github.sasori_256.town_planning.entity.Camera;
import io.github.sasori_256.town_planning.entity.building.Building;
import io.github.sasori_256.town_planning.entity.building.BuildingType;
import io.github.sasori_256.town_planning.entity.model.GameModel;
import io.github.sasori_256.town_planning.entity.resident.Resident;
import io.github.sasori_256.town_planning.entity.resident.ResidentState;
import io.github.sasori_256.town_planning.entity.resident.ResidentType;
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
    Point2D.Double homePos = new Point2D.Double(0, 0);
    Building home = new Building(homePos, BuildingType.RED_ROOFED_HOUSE);
    if (gameMap.placeBuilding(homePos, home)) {
      home.setCurrentPopulation(2);
      gameModel.spawnEntity(home);
      gameModel.spawnEntity(new Resident(new Point2D.Double(homePos.getX(), homePos.getY()),
          ResidentType.CITIZEN, ResidentState.AT_HOME, homePos));
      gameModel.spawnEntity(new Resident(new Point2D.Double(homePos.getX(), homePos.getY()),
          ResidentType.CITIZEN, ResidentState.AT_HOME, homePos));
    }
    Camera camera = new Camera(1, WIDTH, HEIGHT, MAP_WIDTH, MAP_HEIGHT, eventBus);
    ReadWriteLock stateLock = gameModel.getStateLock();
    GameMapController gameMapController = new GameMapController(camera, stateLock);
    SwingUtilities.invokeLater(() -> {
      GameWindow gameWindow = new GameWindow(
          gameMapController, gameModel, gameMap, camera, WIDTH, HEIGHT, eventBus, gameMapController,
          stateLock);
      gameModel.startGameLoop(gameWindow::repaint);
    });
  }
}
