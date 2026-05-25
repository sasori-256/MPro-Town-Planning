package io.github.sasori_256.town_planning.entity.model.manager;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Supplier;

import io.github.sasori_256.town_planning.entity.building.Building;
import io.github.sasori_256.town_planning.entity.building.BuildingType;
import io.github.sasori_256.town_planning.entity.model.GameContext;
import io.github.sasori_256.town_planning.entity.resident.Resident;
import io.github.sasori_256.town_planning.entity.resident.ResidentState;
import io.github.sasori_256.town_planning.map.model.GameMap;

/**
 * 住民のパニック状態を管理する。
 */
public class ResidentPanicManager {
  private static final int[][] HOME_ENTRY_OFFSETS = {
      { 0, 1 },
      { 1, 0 },
      { 0, -1 },
      { -1, 0 }
  };

  /** 状態同期に使用するロック。 */
  private final ReadWriteLock stateLock;
  /** エンティティ管理。 */
  private final EntityManager entityManager;

  /**
   * 住民パニック管理を生成する。
   *
   * @param stateLock     状態ロック
   * @param entityManager エンティティ管理
   */
  public ResidentPanicManager(ReadWriteLock stateLock, EntityManager entityManager) {
    this.stateLock = stateLock;
    this.entityManager = entityManager;
  }

  /**
   * 指定リング内の住民をパニック状態にする。
   *
   * @param context     ゲームコンテキスト
   * @param center      中心座標
   * @param innerRadius 内側半径
   * @param outerRadius 外側半径
   */
  public void panicResidentsInRing(GameContext context, Point2D.Double center,
      double innerRadius, double outerRadius) {
    if (center == null || outerRadius <= innerRadius) {
      return;
    }
    withWriteLock(() -> {
      for (Resident resident : entityManager.snapshotResidents()) {
        if (resident == null) {
          continue;
        }
        double distance = resident.getPosition().distance(center);
        if (distance > innerRadius && distance <= outerRadius) {
          applyPanic(context, resident);
        }
      }
      return null;
    });
  }

  /**
   * 破壊された建物の居住者をパニック状態にする。
   *
   * @param context   ゲームコンテキスト
   * @param destroyed 破壊された建物の一覧
   */
  public void panicResidentsByDestroyedBuildings(GameContext context, List<Building> destroyed) {
    if (destroyed == null || destroyed.isEmpty()) {
      return;
    }
    withWriteLock(() -> {
      for (Resident resident : entityManager.snapshotResidents()) {
        if (resident == null || resident.getState() == ResidentState.DEAD) {
          continue;
        }
        if (isHomeDestroyed(resident, destroyed)) {
          applyPanic(context, resident);
        }
      }
      return null;
    });
  }

  private boolean isHomeDestroyed(Resident resident, List<Building> destroyed) {
    Point2D.Double home = resident.getHomePosition();
    int homeX = (int) Math.round(home.getX());
    int homeY = (int) Math.round(home.getY());
    for (Building building : destroyed) {
      if (building == null) {
        continue;
      }
      BuildingType type = building.getType();
      int localX = homeX - building.getOriginX();
      int localY = homeY - building.getOriginY();
      if (localX < 0 || localY < 0 || localX >= type.getWidth() || localY >= type.getHeight()) {
        continue;
      }
      boolean[][] footprint = type.getFootprintMask();
      if (footprint[localY][localX]) {
        return true;
      }
    }
    return false;
  }

  private void applyPanic(GameContext context, Resident resident) {
    if (resident.getState() == ResidentState.DEAD || resident.getState() == ResidentState.PANICKING) {
      return;
    }
    if (resident.getState() == ResidentState.AT_HOME) {
      Point2D.Double exit = findHomeExit(context.getMap(), resident.getHomePosition());
      if (exit != null) {
        resident.setPosition(exit);
      }
    }
    resident.setState(ResidentState.PANICKING);
  }

  private Point2D.Double findHomeExit(GameMap map, Point2D.Double home) {
    if (map == null || home == null) {
      return null;
    }
    int homeX = (int) Math.round(home.getX());
    int homeY = (int) Math.round(home.getY());
    List<Point2D.Double> candidates = new ArrayList<>();
    for (int[] offset : HOME_ENTRY_OFFSETS) {
      Point2D.Double pos = new Point2D.Double(homeX + offset[0], homeY + offset[1]);
      if (isWalkable(map, pos)) {
        candidates.add(pos);
      }
    }
    if (candidates.isEmpty()) {
      return null;
    }
    return candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
  }

  private boolean isWalkable(GameMap map, Point2D.Double pos) {
    if (!map.isValidPosition(pos)) {
      return false;
    }
    return map.getCell(pos).canWalk();
  }

  /**
   * 書き込みロック内で処理を実行する。
   *
   * @param supplier 実行処理
   * @param <T>      返り値型
   * @return 実行結果
   */
  private <T> T withWriteLock(Supplier<T> supplier) {
    Lock writeLock = stateLock.writeLock();
    writeLock.lock();
    try {
      return supplier.get();
    } finally {
      writeLock.unlock();
    }
  }
}
