package io.github.sasori_256.town_planning;

import java.util.concurrent.locks.ReadWriteLock;

import javax.swing.SwingUtilities;

import io.github.sasori_256.town_planning.common.core.FontManager;
import io.github.sasori_256.town_planning.common.ui.ImageManager;
import io.github.sasori_256.town_planning.common.ui.main.GameWindow;
import io.github.sasori_256.town_planning.entity.Camera;
import io.github.sasori_256.town_planning.entity.model.GameModel;
import io.github.sasori_256.town_planning.map.controller.GameMapController;

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

    GameModel gameModel = new GameModel(MAP_WIDTH, MAP_HEIGHT, SEED);

    Camera camera = new Camera(1, WIDTH, HEIGHT, MAP_WIDTH, MAP_HEIGHT);
    ReadWriteLock stateLock = gameModel.getStateLock();
    GameMapController gameMapController = new GameMapController(camera, stateLock);
    SwingUtilities.invokeLater(() -> {
      GameWindow gameWindow = new GameWindow(
          // TODO: gameMapを引数に渡すのが冗長なのでgameModelからgetするように変更する
          gameModel, camera, WIDTH, HEIGHT, gameMapController, stateLock, imageManager);
      gameModel.startGameLoop(gameWindow::repaint);
    });
  }
}
