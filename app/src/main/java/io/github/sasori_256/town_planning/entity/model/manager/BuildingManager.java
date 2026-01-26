package io.github.sasori_256.town_planning.entity.model.manager;

import java.awt.geom.Point2D;
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
      String detail = "type=" + type + ", cost=" + type.getCost();
      if (!soulManager.canAffordInternal(type.getCost())) {
        publishSpawnFailure(pos, EntitySpawnFailureReason.INSUFFICIENT_SOUL, detail);
        return false;
      }

      if (!gameMap.isValidPosition(pos)) {
        publishSpawnFailure(pos, EntitySpawnFailureReason.INVALID_POSITION, detail);
        return false;
      }
      if (!gameMap.getCell(pos).canBuild()) {
        publishSpawnFailure(pos, EntitySpawnFailureReason.PLACEMENT_BLOCKED, detail);
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

  private void publishSpawnFailure(Point2D.Double pos, EntitySpawnFailureReason reason,
      String detail) {
    eventBus.publish(new EntitySpawnFailedEvent(EntitySpawnKind.BUILDING, reason, pos, detail));
  }
}
