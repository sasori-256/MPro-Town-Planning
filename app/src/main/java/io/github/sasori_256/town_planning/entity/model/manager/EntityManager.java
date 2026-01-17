package io.github.sasori_256.town_planning.entity.model.manager;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Supplier;
import java.util.stream.Stream;

import io.github.sasori_256.town_planning.common.event.EventBus;
import io.github.sasori_256.town_planning.common.event.events.DisasterOccurredEvent;
import io.github.sasori_256.town_planning.common.event.events.MapUpdatedEvent;
import io.github.sasori_256.town_planning.common.event.events.ResidentBornEvent;
import io.github.sasori_256.town_planning.entity.building.Building;
import io.github.sasori_256.town_planning.entity.disaster.Disaster;
import io.github.sasori_256.town_planning.entity.model.BaseGameEntity;
import io.github.sasori_256.town_planning.entity.model.GameContext;
import io.github.sasori_256.town_planning.entity.resident.Resident;
import io.github.sasori_256.town_planning.entity.resident.ResidentState;

/**
 * エンティティの生成・削除・更新をまとめて扱う管理クラス。
 */
public class EntityManager {
  /** イベント通知に使用するイベントバス。 */
  private final EventBus eventBus;
  /** 状態同期に使用するロック。 */
  private final ReadWriteLock stateLock;
  /** 建物エンティティ一覧。 */
  private final List<Building> buildingEntities = new CopyOnWriteArrayList<>();
  /** 住民エンティティ一覧。 */
  private final List<Resident> residentEntities = new CopyOnWriteArrayList<>();
  /** 災害エンティティ一覧。 */
  private final List<Disaster> disasterEntities = new CopyOnWriteArrayList<>();
  /** updateサイクル中かどうかを判定するフラグ。 */
  private final ThreadLocal<Boolean> inUpdateCycle = ThreadLocal.withInitial(() -> false);
  /** update中に追加されたエンティティの待機列。 */
  private final List<BaseGameEntity> entitiesToSpawn = new CopyOnWriteArrayList<>();
  /** update中に削除されたエンティティの待機列。 */
  private final List<BaseGameEntity> entitiesToRemove = new CopyOnWriteArrayList<>();

  /**
   * エンティティ管理を生成する。
   *
   * @param eventBus  イベントバス
   * @param stateLock 状態ロック
   */
  public EntityManager(EventBus eventBus, ReadWriteLock stateLock) {
    this.eventBus = eventBus;
    this.stateLock = stateLock;
  }

  /**
   * 建物エンティティのストリームを返す。
   *
   * @return 建物エンティティのストリーム
   */
  public Stream<Building> getBuildingEntities() {
    return buildingEntities.stream();
  }

  /**
   * 住民エンティティのストリームを返す。
   *
   * @return 住民エンティティのストリーム
   */
  public Stream<Resident> getResidentEntities() {
    return residentEntities.stream();
  }

  /**
   * 災害エンティティのストリームを返す。
   *
   * @return 災害エンティティのストリーム
   */
  public Stream<Disaster> getDisasterEntities() {
    return disasterEntities.stream();
  }

  /**
   * 建物のスナップショットを返す。
   *
   * @return 建物リストのコピー
   */
  public List<Building> snapshotBuildings() {
    return withReadLock(() -> new ArrayList<>(buildingEntities));
  }

  /**
   * 住民のスナップショットを返す。
   *
   * @return 住民リストのコピー
   */
  public List<Resident> snapshotResidents() {
    return withReadLock(() -> new ArrayList<>(residentEntities));
  }

  /**
   * 災害のスナップショットを返す。
   *
   * @return 災害リストのコピー
   */
  public List<Disaster> snapshotDisasters() {
    return withReadLock(() -> new ArrayList<>(disasterEntities));
  }

  /**
   * updateサイクル開始時にフラグを立てる。
   */
  public void beginUpdateCycle() {
    inUpdateCycle.set(true);
  }

  /**
   * updateサイクル終了時にフラグを戻す。
   */
  public void endUpdateCycle() {
    inUpdateCycle.set(false);
  }

  /**
   * エンティティをゲーム世界に追加する。
   *
   * <p>更新サイクル中は待機列に積み、後でまとめて追加する。</p>
   *
   * @param entity  追加するエンティティ
   * @param context ゲームコンテキスト
   * @param <T>     エンティティ型
   */
  public <T extends BaseGameEntity> void spawnEntity(T entity, GameContext context) {
    if (entity == null) {
      return;
    }
    if (inUpdateCycle.get()) {
      entitiesToSpawn.add(entity);
      return;
    }
    withWriteLock(() -> spawnEntityInternal(entity, context));
  }

  /**
   * 書き込みロックを取得せずにエンティティを追加する内部処理。
   *
   * @param entity  追加するエンティティ
   * @param context ゲームコンテキスト
   */
  void spawnEntityInternal(BaseGameEntity entity, GameContext context) {
    if (entity instanceof Resident) {
      addResidentInternal((Resident) entity, context);
    } else if (entity instanceof Building) {
      addBuildingInternal((Building) entity, context);
    } else if (entity instanceof Disaster) {
      addDisasterInternal((Disaster) entity, context);
    }
  }

  /**
   * 住民を追加する内部処理。
   */
  private void addResidentInternal(Resident entity, GameContext context) {
    residentEntities.add(entity);
    entity.onSpawn(context);
    eventBus.publish(new ResidentBornEvent(entity.getPosition()));
  }

  /**
   * 建物を追加する内部処理。
   */
  private void addBuildingInternal(Building entity, GameContext context) {
    buildingEntities.add(entity);
    entity.onSpawn(context);
    eventBus.publish(new MapUpdatedEvent(entity.getPosition()));
  }

  /**
   * 災害を追加する内部処理。
   */
  private void addDisasterInternal(Disaster entity, GameContext context) {
    disasterEntities.add(entity);
    entity.onSpawn(context);
    eventBus.publish(new DisasterOccurredEvent(entity.getType()));
  }

  /**
   * エンティティをゲーム世界から削除する。
   *
   * <p>更新サイクル中は待機列に積み、後でまとめて削除する。</p>
   *
   * @param entity  削除するエンティティ
   * @param context ゲームコンテキスト
   * @param <T>     エンティティ型
   */
  public <T extends BaseGameEntity> void removeEntity(T entity, GameContext context) {
    if (entity == null) {
      return;
    }
    if (inUpdateCycle.get()) {
      entitiesToRemove.add(entity);
      return;
    }
    withWriteLock(() -> removeEntityInternal(entity, context));
  }

  /**
   * 書き込みロックを取得せずにエンティティを削除する内部処理。
   *
   * @param entity  削除するエンティティ
   * @param context ゲームコンテキスト
   */
  void removeEntityInternal(BaseGameEntity entity, GameContext context) {
    entity.onRemove(context);
    if (entity instanceof Resident) {
      residentEntities.remove(entity);
    } else if (entity instanceof Building) {
      buildingEntities.remove(entity);
    } else if (entity instanceof Disaster) {
      disasterEntities.remove(entity);
    }
  }

  /**
   * update処理で各エンティティの状態を更新する。
   *
   * @param context ゲームコンテキスト
   */
  public void updateEntities(GameContext context) {
    for (Resident resident : residentEntities) {
      resident.update(context);
    }
    for (Building building : buildingEntities) {
      building.update(context);
    }
    for (Disaster disaster : disasterEntities) {
      disaster.update(context);
    }
  }

  /**
   * 各エンティティのアニメーション進行を行う。
   *
   * @param dt 前回からの経過秒
   */
  public void advanceAnimations(double dt) {
    for (Resident resident : residentEntities) {
      resident.advanceAnimation(dt);
    }
    for (Building building : buildingEntities) {
      building.advanceAnimation(dt);
    }
    for (Disaster disaster : disasterEntities) {
      disaster.advanceAnimation(dt);
    }
  }

  /**
   * update中に溜まった生成・削除処理をまとめて反映する。
   *
   * @param context ゲームコンテキスト
   */
  public void processDeferredOperations(GameContext context) {
    withWriteLock(() -> {
      for (BaseGameEntity entity : entitiesToRemove) {
        removeEntityInternal(entity, context);
      }
      entitiesToRemove.clear();

      for (BaseGameEntity entity : entitiesToSpawn) {
        spawnEntityInternal(entity, context);
      }
      entitiesToSpawn.clear();
    });
  }

  /**
   * 指定座標の周辺にいる死亡住民を検索する。
   *
   * @param pos    検索中心
   * @param radius 探索半径
   * @return 対象の住民。見つからない場合はnull
   */
  public Resident findDeadResidentWithin(Point2D pos, double radius) {
    return withReadLock(() -> {
      for (Resident resident : residentEntities) {
        if (resident.getState() == ResidentState.DEAD
            && resident.getPosition().distance(pos) <= radius) {
          return resident;
        }
      }
      return null;
    });
  }

  /**
   * 読み込みロック内で処理を実行する。
   *
   * @param supplier 実行処理
   * @param <T>      返り値型
   * @return 実行結果
   */
  private <T> T withReadLock(Supplier<T> supplier) {
    Lock readLock = stateLock.readLock();
    readLock.lock();
    try {
      return supplier.get();
    } finally {
      readLock.unlock();
    }
  }

  /**
   * 書き込みロック内で処理を実行する。
   *
   * @param action 実行処理
   */
  private void withWriteLock(Runnable action) {
    withWriteLock(() -> {
      action.run();
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
}
