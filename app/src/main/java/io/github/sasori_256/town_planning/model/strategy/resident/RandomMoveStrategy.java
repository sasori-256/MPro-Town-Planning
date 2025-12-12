package io.github.sasori_256.town_planning.model.strategy.resident;

import io.github.sasori_256.town_planning.model.GameContext;
import io.github.sasori_256.town_planning.model.GameObject;
import io.github.sasori_256.town_planning.core.strategy.UpdateStrategy;
import java.awt.geom.Point2D;
import java.util.concurrent.ThreadLocalRandom;

/**
 * ランダムに移動するStrategy。
 */
public class RandomMoveStrategy implements UpdateStrategy {
  private double moveTimer = 0;
  private final double moveInterval = 1.0; // 1秒ごとに移動方向を変える
  private double dx = 0;
  private double dy = 0;
  private final double speed = 2.0; // ピクセル/秒 (グリッドベースなら調整が必要)

  @Override
  public void update(GameContext context, GameObject self) {
    double dt = context.getDeltaTime();
    moveTimer += dt;

    if (moveTimer >= moveInterval) {
      moveTimer = 0;
      // 新しいランダムな方向 (-1.0 ~ 1.0)
      dx = ThreadLocalRandom.current().nextDouble(-1, 1);
      dy = ThreadLocalRandom.current().nextDouble(-1, 1);
    }

    // 移動処理
    Point2D current = self.getPosition();
    double nextX = current.getX() + dx * speed * dt;
    double nextY = current.getY() + dy * speed * dt;

    // マップ境界チェック (簡易)
    if (context.getMap().isValid(new Point2D.Double(nextX, nextY))) {
      self.setPosition(new Point2D.Double(nextX, nextY));
    }
  }
}
