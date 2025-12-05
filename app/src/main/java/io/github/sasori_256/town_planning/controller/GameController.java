package io.github.sasori_256.town_planning.controller;

import io.github.sasori_256.town_planning.core.*;
import io.github.sasori_256.town_planning.model.GameModel; // なんかnot visibleになってしまう
import java.util.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import javax.swing.Timer;

/**
 * 入力を処理し、ゲームモデルを制御するコントローラークラス。
 * タイマーを使用して定期的にゲームモデルの状態を更新する。
 * TODO: マウス操作やキーボード操作の処理を追加する。
 */
public class GameController implements ActionListener {
  private final GameModel gameModel;
  private final Timer timer;

  public GameController(GameModel gameModel, int tickIntervalMs) {
    this.gameModel = gameModel;
    this.timer = new Timer(tickIntervalMs, this);
    this.timer.start();
  }

  public void onMapClicked(Point2D position) {
    // マップがクリックされたときの処理をここに追加
    System.out.println("Map clicked at: " + position);
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    gameModel.tick();
  }
}
