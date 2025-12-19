package io.github.sasori_256.town_planning.gameobject.resident.strategy;

import io.github.sasori_256.town_planning.gameobject.model.BaseGameEntity;
import io.github.sasori_256.town_planning.gameobject.model.GameAction;
import io.github.sasori_256.town_planning.gameobject.model.GameContext;

import java.awt.geom.Point2D;
import java.util.concurrent.ThreadLocalRandom;

/**
 * ランダムに移動するStrategy。
 * GameActionとして実装（排他動作）。
 */
public class RandomMoveAction implements GameAction {
  private double moveTimer = 0;
  private final double moveInterval = 1.0; // 1秒ごとに移動方向を変える
  private double dx = 0;
  private double dy = 0;
  private final double speed = 20.0; // ピクセル/秒 (わかりやすく少し速くしました)

  @Override
  public void execute(GameContext context, BaseGameEntity self) {
    double dt = context.getDeltaTime();
    moveTimer += dt;

    if (moveTimer >= moveInterval) {
      moveTimer = 0;
      // 新しいランダムな方向 (-1.0 ~ 1.0)
      dx = ThreadLocalRandom.current().nextDouble(-1, 1);
      dy = ThreadLocalRandom.current().nextDouble(-1, 1);
    }

    // 移動処理
    Point2D.Double current = self.getPosition();
    double nextX = current.getX();
    double nextY = current.getY();

    // マップ境界チェック
    if (context.getMap() != null) {
      // X軸方向の移動
      double proposedX = current.getX() + dx * speed * dt;
      if (context.getMap().isValidPosition(new Point2D.Double(proposedX, current.getY()))) {
        nextX = proposedX;
      }

      // Y軸方向の移動
      double proposedY = current.getY() + dy * speed * dt;
      if (context.getMap().isValidPosition(new Point2D.Double(nextX, proposedY))) {
        nextY = proposedY;
      }
    } else {
      // マップがない場合はチェックせず移動
      nextX += dx * speed * dt;
      nextY += dy * speed * dt;
    }

    self.setPosition(new Point2D.Double(nextX, nextY));
  }
}
