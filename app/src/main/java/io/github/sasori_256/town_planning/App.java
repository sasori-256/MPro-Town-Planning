package io.github.sasori_256.town_planning;

import javax.swing.SwingUtilities;

import io.github.sasori_256.town_planning.common.core.FontManager;
import io.github.sasori_256.town_planning.common.event.EventBus;
import io.github.sasori_256.town_planning.common.ui.ImageManager;
import io.github.sasori_256.town_planning.common.ui.main.GameFlowController;
import io.github.sasori_256.town_planning.common.ui.main.GameWindow;

/**
 * アプリケーションのエントリーポイント。
 */
public class App {
  /**
   * ゲームを初期化して起動する。
   *
   * @param args 起動引数
   */
  public static void main(String[] args) {
    final int WIDTH = 960;
    final int HEIGHT = 640;
    final int MAP_WIDTH = 100;
    final int MAP_HEIGHT = 100;
    final long SEED = 0L;

    new FontManager();
    ImageManager imageManager = new ImageManager();

    EventBus eventBus = new EventBus();
    GameModel gameModel = new GameModel(MAP_WIDTH, MAP_HEIGHT, SEED, eventBus);
    GameMap gameMap = gameModel.getGameMap();

    Camera camera = new Camera(1, WIDTH, HEIGHT, MAP_WIDTH, MAP_HEIGHT, eventBus);
    ReadWriteLock stateLock = gameModel.getStateLock();
    GameMapController gameMapController = new GameMapController(camera, stateLock);
    SwingUtilities.invokeLater(() -> {
      ImageManager imageManager = new ImageManager();
      GameWindow gameWindow = new GameWindow(WIDTH, HEIGHT);
      GameFlowController controller = new GameFlowController(
          gameWindow, imageManager, WIDTH, HEIGHT, MAP_WIDTH, MAP_HEIGHT, SEED);
      controller.initialize();
    });
  }
}
