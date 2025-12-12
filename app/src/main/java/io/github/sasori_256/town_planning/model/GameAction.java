package io.github.sasori_256.town_planning.model;

import io.github.sasori_256.town_planning.model.GameContext; // または GameModel
import java.awt.geom.Point2D;

public interface GameAction {
  String getName();

  int getCost();

  // 実行メソッド: GameModelを受け取り、変更を加える
  boolean execute(GameModel model, Point2D targetPos);
}
