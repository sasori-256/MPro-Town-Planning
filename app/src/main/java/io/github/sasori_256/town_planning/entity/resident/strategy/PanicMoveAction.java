package io.github.sasori_256.town_planning.entity.resident.strategy;

import io.github.sasori_256.town_planning.entity.model.BaseGameEntity;
import io.github.sasori_256.town_planning.entity.model.GameAction;
import io.github.sasori_256.town_planning.entity.model.GameContext;
import io.github.sasori_256.town_planning.map.model.GameMap;
import io.github.sasori_256.town_planning.common.core.GameConfig;
import java.awt.geom.Point2D;
import java.util.concurrent.ThreadLocalRandom;

/**
 * パニック時に周囲をあたふた移動するアクション。
 */
public class PanicMoveAction implements GameAction {
  private static final double SPEED = GameConfig.getResidentPanicSpeedTilesPerSecond(); // タイル/秒
  private static final double ARRIVAL_EPSILON = 1e-3;
  private static final double RETARGET_MIN = GameConfig.getResidentPanicRetargetMinSeconds();
  private static final double RETARGET_MAX = GameConfig.getResidentPanicRetargetMaxSeconds();
  private static final double PANIC_DURATION_MIN = GameConfig.getResidentPanicDurationMinSeconds();
  private static final double PANIC_DURATION_MAX = GameConfig.getResidentPanicDurationMaxSeconds();
  private static final double PANIC_RADIUS = GameConfig.getResidentPanicRadiusTiles();
  private static final int[][] OFFSETS = {
      { 1, 0 },
      { -1, 0 },
      { 0, 1 },
      { 0, -1 }
  };

  private boolean active;
  private double panicDuration;
  private double panicElapsed;
  private double retargetCooldown;
  private Point2D.Double anchor;
  private Point2D.Double target;

  /**
   * パニック移動が完了したかを返す。
   */
  public boolean isFinished() {
    return active && panicElapsed >= panicDuration;
  }

  /**
   * 内部状態をリセットする。
   */
  public void reset() {
    active = false;
    panicDuration = 0.0;
    panicElapsed = 0.0;
    retargetCooldown = 0.0;
    anchor = null;
    target = null;
  }

  /** {@inheritDoc} */
  @Override
  public void execute(GameContext context, BaseGameEntity self) {
    if (context == null || self == null) {
      return;
    }
    GameMap map = context.getMap();
    if (map == null) {
      return;
    }
    if (!active) {
      start(self);
    }

    double dt = context.getDeltaTime();
    if (dt <= 0.0) {
      return;
    }

    panicElapsed += dt;
    if (panicElapsed >= panicDuration) {
      return;
    }

    retargetCooldown = Math.max(0.0, retargetCooldown - dt);
    if (shouldRetarget(self)) {
      // 目標を短い間隔で更新し、落ち着きのない移動にする。
      selectTarget(map, self);
      retargetCooldown = randomRange(RETARGET_MIN, RETARGET_MAX);
    }

    moveTowardTarget(self, dt);
  }

  private void start(BaseGameEntity self) {
    active = true;
    Point2D.Double pos = self.getPosition();
    // パニック開始時の位置を基準に、移動範囲を制限する。
    anchor = new Point2D.Double(pos.getX(), pos.getY());
    panicDuration = randomRange(PANIC_DURATION_MIN, PANIC_DURATION_MAX);
    panicElapsed = 0.0;
    retargetCooldown = 0.0;
    target = null;
  }

  private boolean shouldRetarget(BaseGameEntity self) {
    if (target == null) {
      return true;
    }
    if (retargetCooldown <= 0.0) {
      return true;
    }
    return self.getPosition().distance(target) <= ARRIVAL_EPSILON;
  }

  private void selectTarget(GameMap map, BaseGameEntity self) {
    Point2D.Double current = self.getPosition();
    int cx = toCellIndex(current.getX(), map.getWidth());
    int cy = toCellIndex(current.getY(), map.getHeight());
    int[] order = shuffledOffsets();
    for (int index : order) {
      int nx = cx + OFFSETS[index][0];
      int ny = cy + OFFSETS[index][1];
      Point2D.Double candidate = new Point2D.Double(nx, ny);
      if (!map.isValidPosition(candidate)) {
        continue;
      }
      if (!map.getCell(candidate).canWalk()) {
        continue;
      }
      if (anchor != null && candidate.distance(anchor) > PANIC_RADIUS) {
        continue;
      }
      target = candidate;
      return;
    }
    target = new Point2D.Double(current.getX(), current.getY());
  }

  private void moveTowardTarget(BaseGameEntity self, double dt) {
    if (target == null) {
      return;
    }
    Point2D.Double pos = self.getPosition();
    double dx = target.getX() - pos.getX();
    double dy = target.getY() - pos.getY();
    double dist = Math.hypot(dx, dy);
    if (dist <= ARRIVAL_EPSILON) {
      self.setPosition(new Point2D.Double(target.getX(), target.getY()));
      return;
    }
    double move = Math.min(dist, SPEED * dt);
    double ratio = move / dist;
    double nx = pos.getX() + dx * ratio;
    double ny = pos.getY() + dy * ratio;
    self.setPosition(new Point2D.Double(nx, ny));
  }

  private int toCellIndex(double value, int limit) {
    int index = (int) Math.floor(value);
    if (index < 0) {
      return 0;
    }
    if (index >= limit) {
      return limit - 1;
    }
    return index;
  }

  private int[] shuffledOffsets() {
    int[] order = { 0, 1, 2, 3 };
    ThreadLocalRandom rng = ThreadLocalRandom.current();
    for (int i = order.length - 1; i > 0; i--) {
      int j = rng.nextInt(i + 1);
      int tmp = order[i];
      order[i] = order[j];
      order[j] = tmp;
    }
    return order;
  }

  private double randomRange(double min, double max) {
    return ThreadLocalRandom.current().nextDouble(min, max);
  }
}
