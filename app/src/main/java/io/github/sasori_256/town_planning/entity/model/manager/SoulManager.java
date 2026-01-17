package io.github.sasori_256.town_planning.entity.model.manager;

import java.awt.geom.Point2D;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Supplier;

import io.github.sasori_256.town_planning.common.event.EventBus;
import io.github.sasori_256.town_planning.common.event.events.SoulChangedEvent;
import io.github.sasori_256.town_planning.common.event.events.SoulHarvestedEvent;
import io.github.sasori_256.town_planning.entity.model.GameContext;
import io.github.sasori_256.town_planning.entity.resident.Resident;
import io.github.sasori_256.town_planning.entity.resident.ResidentState;

/**
 * 魂の所持量と魂回収処理を管理する。
 */
public class SoulManager {
  /** イベント通知に使用するイベントバス。 */
  private final EventBus eventBus;
  /** 状態同期に使用するロック。 */
  private final ReadWriteLock stateLock;
  /** 住民の検索と削除に使用するエンティティ管理。 */
  private final EntityManager entityManager;
  /** 現在の魂所持量。 */
  private int soul;

  /**
   * 魂管理を生成する。
   *
   * @param eventBus    イベントバス
   * @param stateLock   状態ロック
   * @param entityManager エンティティ管理
   * @param initialSoul 初期魂量
   */
  public SoulManager(EventBus eventBus, ReadWriteLock stateLock, EntityManager entityManager,
      int initialSoul) {
    this.eventBus = eventBus;
    this.stateLock = stateLock;
    this.entityManager = entityManager;
    this.soul = initialSoul;

    this.eventBus.subscribe(SoulHarvestedEvent.class, event -> addSoul(event.amount()));
  }

  /**
   * 現在の魂所持量を返す。
   *
   * @return 魂所持量
   */
  public int getSoul() {
    return withReadLock(() -> soul);
  }

  /**
   * 魂所持量を設定する。
   *
   * @param soul 設定する魂量
   */
  public void setSoul(int soul) {
    withWriteLock(() -> {
      this.soul = soul;
      return null;
    });
  }

  /**
   * 魂を加算する。
   *
   * @param amount 加算量
   */
  public void addSoul(int amount) {
    withWriteLock(() -> {
      addSoulInternal(amount);
      return null;
    });
  }

  /**
   * ロック取得済みで魂を加算する内部処理。
   *
   * @param amount 加算量
   */
  void addSoulInternal(int amount) {
    this.soul += amount;
    eventBus.publish(new SoulChangedEvent(this.soul));
  }

  /**
   * 魂の支払いが可能かを判定する。
   *
   * @param cost 必要魂量
   * @return 支払い可能ならtrue
   */
  public boolean canAfford(int cost) {
    return withReadLock(() -> soul >= cost);
  }

  /**
   * ロック取得済みで支払い可否を判定する内部処理。
   *
   * @param cost 必要魂量
   * @return 支払い可能ならtrue
   */
  boolean canAffordInternal(int cost) {
    return soul >= cost;
  }

  /**
   * 魂を消費する。足りない場合は何もしない。
   *
   * @param cost 必要魂量
   * @return 消費できた場合はtrue
   */
  public boolean tryConsumeSoul(int cost) {
    return withWriteLock(() -> {
      if (soul < cost) {
        return false;
      }
      addSoulInternal(-cost);
      return true;
    });
  }

  /**
   * 指定座標付近の死体から魂を刈り取る。
   *
   * @param context ゲームコンテキスト
   * @param pos     対象位置
   * @return 収穫できた場合はtrue
   */
  public boolean harvestSoulAt(GameContext context, Point2D pos) {
    double harvestRadius = 1.0;
    Resident target = entityManager.findDeadResidentWithin(pos, harvestRadius);
    if (target == null) {
      return false;
    }

    if (target.getState() != ResidentState.DEAD) {
      return false;
    }

    int soulAmount = 10;
    int faith = target.getFaith();
    soulAmount += faith / 5;

    eventBus.publish(new SoulHarvestedEvent(soulAmount));
    entityManager.removeEntity(target, context);
    return true;
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
