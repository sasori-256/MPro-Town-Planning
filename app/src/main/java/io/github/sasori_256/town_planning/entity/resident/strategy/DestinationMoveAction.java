package io.github.sasori_256.town_planning.entity.resident.strategy;

import io.github.sasori_256.town_planning.entity.model.BaseGameEntity;
import io.github.sasori_256.town_planning.entity.model.GameAction;
import io.github.sasori_256.town_planning.entity.model.GameContext;
import io.github.sasori_256.town_planning.map.model.GameMap;
import io.github.sasori_256.town_planning.map.model.MapCell;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 目的地を決めて最短経路で移動するStrategy。
 * 他の動作（働く、祈るなど）と分離させたいのでGameActionとして実装（排他動作）。
 */
public class DestinationMoveAction implements GameAction {
  /**
   * 移動の状態。
   */
  public enum MoveStatus {
    /** 待機中。 */
    IDLE,
    /** 移動中。 */
    MOVING,
    /** 目的地に到着した。 */
    ARRIVED,
    /** 目的地に到達できなかった。 */
    FAILED
  }

  private static final double ARRIVAL_EPSILON = 1e-3;
  private static final double SEARCH_COOLDOWN = 0.5;
  private static final int MAX_RANDOM_TRIES = 20;
  private static final long COST_INF = 1_000_000L;

  private final double speed;
  private final boolean autoDestination;
  private double searchCooldown;
  private Point2D.Double destination;
  private List<Point2D.Double> path;
  private int pathIndex;
  private MoveStatus lastStatus;

  /**
   * 目的地を自動選択する移動アクションを作成する。
   */
  public DestinationMoveAction() {
    this(true);
  }

  /**
   * 目的地の自動選択有無を指定して作成する。
   *
   * @param autoDestination trueなら目的地を自動選択する
   */
  public DestinationMoveAction(boolean autoDestination) {
    this.speed = 2.0; // タイル/秒
    this.autoDestination = autoDestination;
    this.searchCooldown = 0.0;
    this.lastStatus = MoveStatus.IDLE;
  }

  /**
   * 目的地を設定する。
   *
   * @param destination 目的地
   */
  public void setDestination(Point2D.Double destination) {
    if (destination == null) {
      clearPath();
      return;
    }
    this.destination = new Point2D.Double(destination.getX(), destination.getY());
    this.path = null;
    this.pathIndex = 0;
    this.searchCooldown = 0.0;
    this.lastStatus = MoveStatus.IDLE;
  }

  /**
   * 目的地をクリアする。
   */
  public void clearDestination() {
    clearPath();
  }

  /**
   * 最後の移動状態を返す。
   */
  public MoveStatus getLastStatus() {
    return lastStatus;
  }

  /**
   * 目的地が設定されているかを返す。
   */
  public boolean hasDestination() {
    return destination != null;
  }

  /**
   * 現在の目的地を返す。
   */
  public Point2D.Double getDestination() {
    if (destination == null) {
      return null;
    }
    return new Point2D.Double(destination.getX(), destination.getY());
  }

  @Override
  public void execute(GameContext context, BaseGameEntity self) {
    lastStatus = MoveStatus.IDLE;
    GameMap map = context.getMap();
    if (map == null || self == null) {
      return;
    }

    double dt = context.getDeltaTime();
    if (searchCooldown > 0.0) {
      searchCooldown = Math.max(0.0, searchCooldown - dt);
    }

    if (destination == null) {
      if (!autoDestination) {
        return;
      }
      if (searchCooldown > 0.0) {
        return;
      }
      if (!selectNewDestination(map, self)) {
        searchCooldown = SEARCH_COOLDOWN;
        lastStatus = MoveStatus.FAILED;
        return;
      }
    }

    if (!isCellWalkable(map, destination)) {
      clearPath();
      searchCooldown = SEARCH_COOLDOWN;
      lastStatus = MoveStatus.FAILED;
      return;
    }

    if (path == null || pathIndex >= path.size()) {
      List<Point2D.Double> newPath = findPath(map, self.getPosition(), destination);
      if (newPath == null) {
        clearPath();
        searchCooldown = SEARCH_COOLDOWN;
        lastStatus = MoveStatus.FAILED;
        return;
      }
      path = newPath;
      pathIndex = 0;
    }

    if (pathIndex < path.size()) {
      Point2D.Double nextStep = path.get(pathIndex);
      if (!isCellWalkable(map, nextStep)) {
        if (!replanPath(map, self.getPosition())) {
          clearPath();
          searchCooldown = SEARCH_COOLDOWN;
          lastStatus = MoveStatus.FAILED;
          return;
        }
      }
    }

    moveAlongPath(self, dt);
    lastStatus = MoveStatus.MOVING;

    if (destination != null
        && self.getPosition().distance(destination) <= ARRIVAL_EPSILON) {
      self.setPosition(new Point2D.Double(destination.getX(), destination.getY()));
      clearPath();
      lastStatus = MoveStatus.ARRIVED;
    }
  }

  private void moveAlongPath(BaseGameEntity self, double dt) {
    if (path == null || pathIndex >= path.size()) {
      return;
    }

    double remaining = speed * dt;
    double x = self.getPosition().getX();
    double y = self.getPosition().getY();

    while (remaining > 0.0 && pathIndex < path.size()) {
      Point2D.Double target = path.get(pathIndex);
      double dx = target.getX() - x;
      double dy = target.getY() - y;
      double dist = Math.hypot(dx, dy);

      if (dist <= ARRIVAL_EPSILON) {
        x = target.getX();
        y = target.getY();
        pathIndex++;
        continue;
      }

      if (dist <= remaining) {
        x = target.getX();
        y = target.getY();
        remaining -= dist;
        pathIndex++;
      } else {
        double ratio = remaining / dist;
        x += dx * ratio;
        y += dy * ratio;
        remaining = 0.0;
      }
    }

    self.setPosition(new Point2D.Double(x, y));
  }

  private boolean selectNewDestination(GameMap map, BaseGameEntity self) {
    Point2D.Double current = self.getPosition();
    int startX = toCellIndex(current.getX(), map.getWidth());
    int startY = toCellIndex(current.getY(), map.getHeight());

    ThreadLocalRandom rng = ThreadLocalRandom.current();
    for (int i = 0; i < MAX_RANDOM_TRIES; i++) {
      int x = rng.nextInt(map.getWidth());
      int y = rng.nextInt(map.getHeight());
      if (x == startX && y == startY) {
        continue;
      }
      if (!isCellWalkable(map, x, y)) {
        continue;
      }
      Point2D.Double candidate = new Point2D.Double(x, y);
      List<Point2D.Double> newPath = findPath(map, current, candidate);
      if (newPath != null) {
        destination = candidate;
        path = newPath;
        pathIndex = 0;
        return true;
      }
    }

    for (int y = 0; y < map.getHeight(); y++) {
      for (int x = 0; x < map.getWidth(); x++) {
        if (x == startX && y == startY) {
          continue;
        }
        if (!isCellWalkable(map, x, y)) {
          continue;
        }
        Point2D.Double candidate = new Point2D.Double(x, y);
        List<Point2D.Double> newPath = findPath(map, current, candidate);
        if (newPath != null) {
          destination = candidate;
          path = newPath;
          pathIndex = 0;
          return true;
        }
      }
    }

    return false;
  }

  private boolean replanPath(GameMap map, Point2D.Double startPos) {
    if (destination == null) {
      return false;
    }
    List<Point2D.Double> newPath = findPath(map, startPos, destination);
    if (newPath == null) {
      return false;
    }
    path = newPath;
    pathIndex = 0;
    return true;
  }

  private List<Point2D.Double> findPath(
      GameMap map,
      Point2D.Double startPos,
      Point2D.Double goalPos) {
    int width = map.getWidth();
    int height = map.getHeight();
    int startX = toCellIndex(startPos.getX(), width);
    int startY = toCellIndex(startPos.getY(), height);
    int goalX = toCellIndex(goalPos.getX(), width);
    int goalY = toCellIndex(goalPos.getY(), height);

    if (startX == goalX && startY == goalY) {
      return new ArrayList<>();
    }
    if (!isCellWalkable(map, goalX, goalY)) {
      return null;
    }

    long[][] dist = new long[height][width];
    for (int y = 0; y < height; y++) {
      Arrays.fill(dist[y], COST_INF);
    }
    Point[][] prev = new Point[height][width];
    PriorityQueue<Node> queue = new PriorityQueue<>(Comparator.comparingLong(n -> n.cost));

    dist[startY][startX] = 0;
    queue.add(new Node(startX, startY, 0));

    int[] dx = { 1, -1, 0, 0 };
    int[] dy = { 0, 0, 1, -1 };

    while (!queue.isEmpty()) {
      Node current = queue.poll();
      if (current.cost != dist[current.y][current.x]) {
        continue;
      }
      if (current.x == goalX && current.y == goalY) {
        break;
      }
      for (int i = 0; i < dx.length; i++) {
        int nx = current.x + dx[i];
        int ny = current.y + dy[i];
        if (nx < 0 || ny < 0 || nx >= width || ny >= height) {
          continue;
        }
        if (!isCellWalkable(map, nx, ny)) {
          continue;
        }
        MapCell cell = map.getCell(new Point2D.Double(nx, ny));
        long stepCost = cell.getMoveCost();
        if (stepCost >= COST_INF) {
          continue;
        }
        long newCost = current.cost + stepCost;
        if (newCost < dist[ny][nx]) {
          dist[ny][nx] = newCost;
          prev[ny][nx] = new Point(current.x, current.y);
          queue.add(new Node(nx, ny, newCost));
        }
      }
    }

    if (dist[goalY][goalX] >= COST_INF) {
      return null;
    }

    List<Point2D.Double> reversed = new ArrayList<>();
    int cx = goalX;
    int cy = goalY;
    while (!(cx == startX && cy == startY)) {
      reversed.add(new Point2D.Double(cx, cy));
      Point step = prev[cy][cx];
      if (step == null) {
        return null;
      }
      cx = step.x;
      cy = step.y;
    }
    Collections.reverse(reversed);
    return reversed;
  }

  private boolean isCellWalkable(GameMap map, Point2D.Double pos) {
    int x = toCellIndex(pos.getX(), map.getWidth());
    int y = toCellIndex(pos.getY(), map.getHeight());
    return isCellWalkable(map, x, y);
  }

  private boolean isCellWalkable(GameMap map, int x, int y) {
    Point2D.Double pos = new Point2D.Double(x, y);
    if (!map.isValidPosition(pos)) {
      return false;
    }
    return map.getCell(pos).canWalk();
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

  private void clearPath() {
    destination = null;
    path = null;
    pathIndex = 0;
  }

  private static final class Node {
    private final int x;
    private final int y;
    private final long cost;

    private Node(int x, int y, long cost) {
      this.x = x;
      this.y = y;
      this.cost = cost;
    }
  }
}
