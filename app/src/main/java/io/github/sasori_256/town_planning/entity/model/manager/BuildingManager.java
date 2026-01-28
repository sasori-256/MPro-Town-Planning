package io.github.sasori_256.town_planning.entity.model.manager;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Supplier;

import io.github.sasori_256.town_planning.common.event.EventBus;
import io.github.sasori_256.town_planning.common.event.events.EntitySpawnFailedEvent;
import io.github.sasori_256.town_planning.common.event.events.EntitySpawnFailureReason;
import io.github.sasori_256.town_planning.common.event.events.EntitySpawnKind;
import io.github.sasori_256.town_planning.common.event.events.MapUpdatedEvent;
import io.github.sasori_256.town_planning.entity.building.Building;
import io.github.sasori_256.town_planning.entity.building.BuildingType;
import io.github.sasori_256.town_planning.entity.model.GameContext;
import io.github.sasori_256.town_planning.map.model.GameMap;

/**
 * 建物の建設・撤去を管理する。
 */
public class BuildingManager {
  /** イベント通知に使用するイベントバス。 */
  private final EventBus eventBus = EventBus.getInstance();
  /** マップ本体。 */
  private final GameMap gameMap;
  /** 状態同期に使用するロック。 */
  private final ReadWriteLock stateLock;
  /** 魂管理。 */
  private final SoulManager soulManager;
  /** エンティティ管理。 */
  private final EntityManager entityManager;

  /**
   * 建物管理を生成する。
   *
   * @param gameMap       マップ
   * @param stateLock     状態ロック
   * @param soulManager   魂管理
   * @param entityManager エンティティ管理
   */
  public BuildingManager(GameMap gameMap, ReadWriteLock stateLock,
      SoulManager soulManager, EntityManager entityManager) {
    this.gameMap = gameMap;
    this.stateLock = stateLock;
    this.soulManager = soulManager;
    this.entityManager = entityManager;
  }

  /**
   * 建物を建設する。
   *
   * @param context ゲームコンテキスト
   * @param pos     設置位置
   * @param type    建物種別
   * @return 建設できた場合はtrue
   */
  public boolean constructBuilding(GameContext context, Point2D.Double pos, BuildingType type) {
    return withWriteLock(() -> {
      String detail = BuildingType.getDetailString(type);
      EntitySpawnFailureReason reason = validateConstructionInternal(pos, type);
      if (reason != null) {
        publishSpawnFailure(pos, reason, detail);
        return false;
      }

      soulManager.addSoulInternal(-type.getCost());

      Building building = new Building(pos, type);
      if (gameMap.placeBuilding(pos, building)) {
        entityManager.spawnEntityInternal(building, context);
        return true;
      }

      soulManager.addSoulInternal(type.getCost());
      publishSpawnFailure(pos, EntitySpawnFailureReason.PLACEMENT_BLOCKED, detail);
      return false;
    });
  }

  /**
   * 建物を建設できるか判定する。
   *
   * @param pos  設置位置
   * @param type 建物種別
   * @return 問題があれば失敗理由、問題なければnull
   */
  public EntitySpawnFailureReason validateConstruction(Point2D.Double pos, BuildingType type) {
    return withReadLock(() -> validateConstructionInternal(pos, type));
  }

  /**
   * 建物エンティティを削除する。
   *
   * @param entity 建物
   */
  public void removeBuildingEntity(Building entity) {
    withWriteLock(() -> {
      gameMap.removeBuilding(entity.getPosition());
      eventBus.publish(new MapUpdatedEvent(entity.getPosition()));
      return null;
    });
  }

  /**
   * 指定範囲の建物にダメージを与える。
   *
   * @param context ゲームコンテキスト
   * @param center  中心座標
   * @param radius  影響半径
   * @param damage  ダメージ量
   * @return 破壊された建物の一覧
   */
  public List<Building> damageBuildings(GameContext context, Point2D.Double center, double radius,
      int damage) {
    return withWriteLock(() -> {
      List<Building> destroyed = new ArrayList<>();
      if (center == null || radius <= 0.0 || damage <= 0) {
        return destroyed;
      }
      List<Building> candidates = entityManager.snapshotBuildings();
      for (Building building : candidates) {
        if (building == null) {
          continue;
        }
        if (!isWithinRadius(building, center, radius)) {
          continue;
        }
        if (building.applyDamage(damage)) {
          gameMap.removeBuilding(building.getPosition());
          context.removeEntity(building);
          destroyed.add(building);
        }
      }
      return destroyed;
    });
  }

  private boolean isWithinRadius(Building building, Point2D.Double center, double radius) {
    BuildingType type = building.getType();
    boolean[][] footprint = type.getFootprintMask();
    int originX = building.getOriginX();
    int originY = building.getOriginY();
    for (int y = 0; y < type.getHeight(); y++) {
      for (int x = 0; x < type.getWidth(); x++) {
        if (!footprint[y][x]) {
          continue;
        }
        Point2D.Double pos = new Point2D.Double(originX + x, originY + y);
        if (pos.distance(center) <= radius) {
          return true;
        }
      }
    }
    return false;
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

  private <T> T withReadLock(Supplier<T> supplier) {
    Lock readLock = stateLock.readLock();
    readLock.lock();
    try {
      return supplier.get();
    } finally {
      readLock.unlock();
    }
  }

  private void publishSpawnFailure(Point2D.Double pos, EntitySpawnFailureReason reason,
      String detail) {
    eventBus.publish(new EntitySpawnFailedEvent(EntitySpawnKind.BUILDING, reason, pos, detail));
  }

  private EntitySpawnFailureReason validateConstructionInternal(Point2D.Double pos, BuildingType type) {
    if (type == null) {
      return EntitySpawnFailureReason.INVALID_ENTITY;
    }
    if (pos == null || !gameMap.isValidPosition(pos)) {
      return EntitySpawnFailureReason.INVALID_POSITION;
    }
    if (!gameMap.canPlaceBuilding(pos, type)) {
      return EntitySpawnFailureReason.PLACEMENT_BLOCKED;
    }
    if (!soulManager.canAffordInternal(type.getCost())) {
      return EntitySpawnFailureReason.INSUFFICIENT_SOUL;
    }
    return null;
  }
}
