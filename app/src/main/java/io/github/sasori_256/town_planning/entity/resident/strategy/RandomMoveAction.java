package io.github.sasori_256.town_planning.entity.resident.strategy;

import io.github.sasori_256.town_planning.entity.model.BaseGameEntity;
import io.github.sasori_256.town_planning.entity.model.GameAction;
import io.github.sasori_256.town_planning.entity.model.GameContext;
import io.github.sasori_256.town_planning.map.model.GameMap;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 目的地を決めて最短経路で移動するStrategy。
 * 他の動作（働く、祈るなど）と分離させたいのでGameActionとして実装（排他動作）。
 */
public class RandomMoveAction implements GameAction {
  private static final double ARRIVAL_EPSILON = 1e-3;
  private static final double SEARCH_COOLDOWN = 0.5;
  private static final int MAX_RANDOM_TRIES = 20;

  private final double speed;
  private double searchCooldown;
  private Point2D.Double destination;
  private List<Point2D.Double> path;
  private int pathIndex;

  public RandomMoveAction() {
    this.speed = 50.0; // ピクセル/秒
  }

  @Override
  public void execute(GameContext context, BaseGameEntity self) {
    GameMap map = context.getMap();
    if (map == null) {
      return;
    }

    double dt = context.getDeltaTime();
    if (searchCooldown > 0.0) {
      searchCooldown = Math.max(0.0, searchCooldown - dt);
    }

    if (destination == null || path == null || pathIndex >= path.size()) {
      if (searchCooldown > 0.0) {
        return;
      }
      if (!selectNewDestination(map, self)) {
        searchCooldown = SEARCH_COOLDOWN;
        return;
      }
    }

    if (!isCellWalkable(map, destination)) {
      clearPath();
      searchCooldown = SEARCH_COOLDOWN;
      return;
    }

    if (pathIndex < path.size()) {
      Point2D.Double nextStep = path.get(pathIndex);
      if (!isCellWalkable(map, nextStep)) {
        if (!replanPath(map, self.getPosition())) {
          clearPath();
          searchCooldown = SEARCH_COOLDOWN;
          return;
        }
      }
    }

    moveAlongPath(self, dt);

    if (destination != null
        && self.getPosition().distance(destination) <= ARRIVAL_EPSILON) {
      self.setPosition(new Point2D.Double(destination.getX(), destination.getY()));
      clearPath();
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

  /**
   * Finds a path from the starting position to the goal position using a
   * breadth-first search algorithm
   *
   * @param map      The game map to navigate.
   * @param startPos The starting position.
   * @param goalPos  The goal position.
   * @return A list of points representing the path, or null if no path is found.
   */
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

    boolean[][] visited = new boolean[height][width];
    Point[][] prev = new Point[height][width];
    ArrayDeque<Point> queue = new ArrayDeque<>();

    visited[startY][startX] = true;
    queue.add(new Point(startX, startY));

    int[] dx = { 1, -1, 0, 0 };
    int[] dy = { 0, 0, 1, -1 };

    while (!queue.isEmpty()) {
      Point current = queue.poll();
      if (current.x == goalX && current.y == goalY) {
        break;
      }
      for (int i = 0; i < dx.length; i++) {
        int nx = current.x + dx[i];
        int ny = current.y + dy[i];
        if (nx < 0 || ny < 0 || nx >= width || ny >= height) {
          continue;
        }
        if (visited[ny][nx]) {
          continue;
        }
        if (!isCellWalkable(map, nx, ny)) {
          continue;
        }
        visited[ny][nx] = true;
        prev[ny][nx] = current;
        queue.add(new Point(nx, ny));
      }
    }

    if (!visited[goalY][goalX]) {
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
}
