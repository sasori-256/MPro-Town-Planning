package io.github.sasori_256.town_planning.entity.resident.strategy;

import io.github.sasori_256.town_planning.entity.building.Building;
import io.github.sasori_256.town_planning.entity.building.BuildingType;
import io.github.sasori_256.town_planning.entity.model.BaseGameEntity;
import io.github.sasori_256.town_planning.entity.model.CategoryType;
import io.github.sasori_256.town_planning.entity.model.GameAction;
import io.github.sasori_256.town_planning.entity.model.GameContext;
import io.github.sasori_256.town_planning.entity.resident.Resident;
import io.github.sasori_256.town_planning.entity.resident.ResidentState;
import io.github.sasori_256.town_planning.map.model.GameMap;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 住民の移動・作業・帰宅を管理するアクション。
 */
public class ResidentBehaviorAction implements GameAction {
  private static final double WORK_DURATION = 5.0;
  private static final double HOME_WAIT_MIN = 8.0;
  private static final double HOME_WAIT_MAX = 20.0;
  private static final double HOME_ENTRY_EPSILON = 0.1;
  private static final int[][] HOME_ENTRY_OFFSETS = {
      { 0, 1 },
      { 1, 0 },
      { 0, -1 },
      { -1, 0 }
  };

  private final DestinationMoveAction mover = new DestinationMoveAction(false);
  private double workTimer;
  private double homeWaitTimer;
  private double homeWaitDuration;

  /**
   * 住民の行動を進める。
   *
   * @param context ゲーム内の環境情報
   * @param self    行動を実行するエンティティ
   */
  @Override
  public void execute(GameContext context, BaseGameEntity self) {
    if (!(self instanceof Resident)) {
      return;
    }
    Resident resident = (Resident) self;
    if (resident.getState() == ResidentState.DEAD) {
      return;
    }

    GameMap map = context.getMap();
    if (map == null) {
      return;
    }

    ResidentState state = resident.getState();
    if (state == ResidentState.AT_HOME) {
      handleAtHome(context, resident, map);
      return;
    }
    if (state == ResidentState.RELOCATING) {
      handleRelocating(context, resident, map);
      return;
    }
    if (state == ResidentState.TRAVELING) {
      handleTraveling(context, resident, map);
      return;
    }
    if (state == ResidentState.WORKING) {
      handleWorking(context, resident, map);
      return;
    }
    if (state == ResidentState.RETURNING_HOME) {
      handleReturning(context, resident, map);
      return;
    }
  }

  private void handleAtHome(GameContext context, Resident resident, GameMap map) {
    if (homeWaitDuration <= 0.0) {
      scheduleHomeWait();
    }
    homeWaitTimer += context.getDeltaTime();
    if (homeWaitTimer < homeWaitDuration) {
      return;
    }

    Point2D.Double entry = findHomeEntry(map, resident.getHomePosition());
    if (entry == null) {
      scheduleHomeWait();
      return;
    }
    Point2D.Double destination = selectDestination(context, map, resident);
    if (destination == null) {
      scheduleHomeWait();
      return;
    }

    clearHomeWait();
    resident.setPosition(new Point2D.Double(entry.getX(), entry.getY()));
    resident.setState(ResidentState.TRAVELING);
    mover.setDestination(destination);
  }

  private void handleTraveling(GameContext context, Resident resident, GameMap map) {
    if (!mover.hasDestination()) {
      forceReturnHome(resident, true);
      return;
    }
    mover.execute(context, resident);
    DestinationMoveAction.MoveStatus status = mover.getLastStatus();
    if (status == DestinationMoveAction.MoveStatus.ARRIVED) {
      startWorking(resident);
    } else if (status == DestinationMoveAction.MoveStatus.FAILED) {
      forceReturnHome(resident, true);
    }
  }

  private void handleRelocating(GameContext context, Resident resident, GameMap map) {
    Point2D.Double target = resident.getRelocationTarget();
    if (target == null) {
      cancelRelocation(resident);
      return;
    }

    Point2D.Double entry = findHomeEntry(map, target);
    if (entry == null) {
      cancelRelocation(resident);
      return;
    }
    Point2D.Double currentDestination = mover.getDestination();
    if (currentDestination == null || currentDestination.distance(entry) > HOME_ENTRY_EPSILON) {
      Point2D.Double currentHome = resident.getHomePosition();
      if (resident.getPosition().distance(currentHome) <= HOME_ENTRY_EPSILON) {
        Point2D.Double exit = findHomeEntry(map, currentHome);
        if (exit != null) {
          resident.setPosition(new Point2D.Double(exit.getX(), exit.getY()));
        }
      }
      mover.setDestination(entry);
    }

    mover.execute(context, resident);
    DestinationMoveAction.MoveStatus status = mover.getLastStatus();
    if (status == DestinationMoveAction.MoveStatus.ARRIVED) {
      completeRelocation(resident, target);
    } else if (status == DestinationMoveAction.MoveStatus.FAILED) {
      cancelRelocation(resident);
    }
  }

  private void handleWorking(GameContext context, Resident resident, GameMap map) {
    workTimer += context.getDeltaTime();
    if (workTimer >= WORK_DURATION) {
      startReturnHome(resident, map);
    }
  }

  private void handleReturning(GameContext context, Resident resident, GameMap map) {
    if (!mover.hasDestination()) {
      forceReturnHome(resident, true);
      return;
    }
    mover.execute(context, resident);
    DestinationMoveAction.MoveStatus status = mover.getLastStatus();
    if (status == DestinationMoveAction.MoveStatus.ARRIVED) {
      completeReturnHome(resident);
    } else if (status == DestinationMoveAction.MoveStatus.FAILED) {
      forceReturnHome(resident, true);
    }
  }

  private void startWorking(Resident resident) {
    workTimer = 0.0;
    resident.setState(ResidentState.WORKING);
  }

  private void startReturnHome(Resident resident, GameMap map) {
    Point2D.Double entry = findHomeEntry(map, resident.getHomePosition());
    if (entry == null) {
      forceReturnHome(resident, true);
      return;
    }
    mover.setDestination(entry);
    resident.setState(ResidentState.RETURNING_HOME);
  }

  private void completeReturnHome(Resident resident) {
    Point2D.Double home = resident.getHomePosition();
    resident.setPosition(new Point2D.Double(home.getX(), home.getY()));
    resident.setState(ResidentState.AT_HOME);
    mover.clearDestination();
    workTimer = 0.0;
    scheduleHomeWait();
  }

  private void completeRelocation(Resident resident, Point2D.Double target) {
    resident.setHomePosition(target);
    resident.setPosition(new Point2D.Double(target.getX(), target.getY()));
    resident.clearRelocationTarget();
    resident.setState(ResidentState.AT_HOME);
    mover.clearDestination();
    workTimer = 0.0;
    scheduleHomeWait();
  }

  private void cancelRelocation(Resident resident) {
    resident.clearRelocationTarget();
    Point2D.Double home = resident.getHomePosition();
    resident.setPosition(new Point2D.Double(home.getX(), home.getY()));
    resident.setState(ResidentState.AT_HOME);
    mover.clearDestination();
    workTimer = 0.0;
    scheduleHomeWait();
  }

  private void forceReturnHome(Resident resident, boolean penalizeFaith) {
    if (penalizeFaith) {
      resident.setFaith(resident.getFaith() - 1);
    }
    Point2D.Double home = resident.getHomePosition();
    resident.setPosition(new Point2D.Double(home.getX(), home.getY()));
    resident.setState(ResidentState.AT_HOME);
    mover.clearDestination();
    workTimer = 0.0;
    scheduleHomeWait();
  }

  private void scheduleHomeWait() {
    homeWaitTimer = 0.0;
    homeWaitDuration = ThreadLocalRandom.current().nextDouble(HOME_WAIT_MIN, HOME_WAIT_MAX);
  }

  private void clearHomeWait() {
    homeWaitTimer = 0.0;
    homeWaitDuration = 0.0;
  }

  private Point2D.Double selectDestination(GameContext context, GameMap map, Resident resident) {
    List<Building> candidates = context.getBuildingEntities()
        .filter(building -> isEligibleDestination(building, resident))
        .collect(Collectors.toCollection(ArrayList::new));
    if (candidates.isEmpty()) {
      return null;
    }

    Collections.shuffle(candidates);
    for (Building building : candidates) {
      Point2D.Double entry = findBuildingEntry(map, building);
      if (entry != null) {
        return entry;
      }
    }
    return null;
  }

  private boolean isEligibleDestination(Building building, Resident resident) {
    BuildingType type = building.getType();
    CategoryType category = type.getCategory();
    if (category != CategoryType.RELIGIOUS
        && category != CategoryType.CEMETERY
        && category != CategoryType.INFRASTRUCTURE) {
      return false;
    }
    if (type == BuildingType.ROAD) {
      return false;
    }
    Point2D.Double home = resident.getHomePosition();
    int homeX = (int) Math.round(home.getX());
    int homeY = (int) Math.round(home.getY());
    return !(building.getOriginX() == homeX && building.getOriginY() == homeY);
  }

  private Point2D.Double findHomeEntry(GameMap map, Point2D.Double home) {
    int homeX = (int) Math.round(home.getX());
    int homeY = (int) Math.round(home.getY());

    Point2D.Double door = new Point2D.Double(homeX + HOME_ENTRY_OFFSETS[0][0],
        homeY + HOME_ENTRY_OFFSETS[0][1]);
    if (isWalkable(map, door)) {
      return door;
    }

    List<Point2D.Double> alternatives = new ArrayList<>();
    for (int i = 1; i < HOME_ENTRY_OFFSETS.length; i++) {
      int dx = HOME_ENTRY_OFFSETS[i][0];
      int dy = HOME_ENTRY_OFFSETS[i][1];
      Point2D.Double candidate = new Point2D.Double(homeX + dx, homeY + dy);
      if (isWalkable(map, candidate)) {
        alternatives.add(candidate);
      }
    }

    if (alternatives.isEmpty()) {
      return null;
    }
    return alternatives.get(ThreadLocalRandom.current().nextInt(alternatives.size()));
  }

  private Point2D.Double findBuildingEntry(GameMap map, Building building) {
    BuildingType type = building.getType();
    int originX = building.getOriginX();
    int originY = building.getOriginY();
    boolean[][] footprint = type.getFootprintMask();

    List<Point2D.Double> walkableInside = new ArrayList<>();
    for (int y = 0; y < type.getHeight(); y++) {
      for (int x = 0; x < type.getWidth(); x++) {
        if (!footprint[y][x]) {
          continue;
        }
        if (!type.isWalkable(x, y)) {
          continue;
        }
        int mapX = originX + x;
        int mapY = originY + y;
        if (isWalkable(map, mapX, mapY)) {
          walkableInside.add(new Point2D.Double(mapX, mapY));
        }
      }
    }

    if (!walkableInside.isEmpty()) {
      return walkableInside.get(ThreadLocalRandom.current().nextInt(walkableInside.size()));
    }

    Set<Point> adjacent = new LinkedHashSet<>();
    int[] dx = { 1, -1, 0, 0 };
    int[] dy = { 0, 0, 1, -1 };
    for (int y = 0; y < type.getHeight(); y++) {
      for (int x = 0; x < type.getWidth(); x++) {
        if (!footprint[y][x]) {
          continue;
        }
        int baseX = originX + x;
        int baseY = originY + y;
        for (int i = 0; i < dx.length; i++) {
          int nx = baseX + dx[i];
          int ny = baseY + dy[i];
          if (isWalkable(map, nx, ny)) {
            adjacent.add(new Point(nx, ny));
          }
        }
      }
    }

    if (adjacent.isEmpty()) {
      return null;
    }

    List<Point> candidateList = new ArrayList<>(adjacent);
    Point selected = candidateList.get(ThreadLocalRandom.current().nextInt(candidateList.size()));
    return new Point2D.Double(selected.x, selected.y);
  }

  private boolean isWalkable(GameMap map, Point2D.Double pos) {
    if (!map.isValidPosition(pos)) {
      return false;
    }
    return map.getCell(pos).canWalk();
  }

  private boolean isWalkable(GameMap map, int x, int y) {
    Point2D.Double pos = new Point2D.Double(x, y);
    return isWalkable(map, pos);
  }
}

