package io.github.sasori_256.town_planning;

// import com.google.common.eventbus.EventBus;

import io.github.sasori_256.town_planning.common.event.EventBus;
import io.github.sasori_256.town_planning.common.ui.GameWindow;
import io.github.sasori_256.town_planning.gameObject.Camera;
import io.github.sasori_256.town_planning.gameObject.model.GameModel;
import io.github.sasori_256.town_planning.map.controller.GameMapController;
import io.github.sasori_256.town_planning.map.model.GameMap;

public class Main {
  public static void main(String[] args) {
    EventBus eventBus = new EventBus();
    GameModel gameModel = new GameModel(eventBus);
    GameMap gameMap = gameModel.getGameMap();
    Camera camera = new Camera(64, 32);
    GameMapController gameMapController = new GameMapController(camera);
    GameWindow gameWindow = new GameWindow();

  }
}
