package io.github.sasori_256.town_planning;

import java.util.concurrent.locks.ReadWriteLock;

import javax.swing.SwingUtilities;

import io.github.sasori_256.town_planning.common.core.FontManager;
import io.github.sasori_256.town_planning.common.event.EventBus;
import io.github.sasori_256.town_planning.common.ui.ImageManager;
import io.github.sasori_256.town_planning.common.ui.main.GameWindow;
import io.github.sasori_256.town_planning.entity.Camera;
import io.github.sasori_256.town_planning.entity.model.GameModel;
import io.github.sasori_256.town_planning.map.controller.GameMapController;
import io.github.sasori_256.town_planning.map.model.GameMap;

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
    final int MAP_WIDTH = 50;
    final int MAP_HEIGHT = 50;
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
      GameWindow gameWindow = new GameWindow(
          // TODO: gameMapを引数に渡すのが冗長なのでgameModelからgetするように変更する
          gameModel, gameMap, camera, WIDTH, HEIGHT, eventBus, gameMapController, stateLock, imageManager);
      gameModel.startGameLoop(gameWindow::repaint);
    });
  }
}
