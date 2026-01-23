package io.github.sasori_256.town_planning.entity.model.manager;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Supplier;

import io.github.sasori_256.town_planning.common.event.EventBus;
import io.github.sasori_256.town_planning.common.event.Subscription;
import io.github.sasori_256.town_planning.common.event.events.GameOverEvent;
import io.github.sasori_256.town_planning.common.event.events.ResidentBornEvent;
import io.github.sasori_256.town_planning.common.event.events.ResidentDiedEvent;
import io.github.sasori_256.town_planning.entity.resident.Resident;
import io.github.sasori_256.town_planning.entity.resident.ResidentState;

/**
 * 住民数の集計を管理する。
 */
public class PopulationManager {
  /** イベント通知に使用するイベントバス。 */
  private final EventBus eventBus;
  /** 状態同期に使用するロック。 */
  private final ReadWriteLock stateLock;
  /** エンティティ管理。 */
  private final EntityManager entityManager;
  /** ResidentDiedEvent用のunsubscriber。 */
  private Subscription diedSub;
  /** ResidentBornEvent用のunsubscriber。 */
  private Subscription bornSub;
  /** 最大生存住民数。 */
  private int maxPopulation;
  /** 累計死亡住民数。 */
  private int totalDeaths;

  /**
   * 住民数管理を生成する。
   *
   * @param eventBus      イベントバス
   * @param stateLock     状態ロック
   * @param entityManager エンティティ管理
   */
  public PopulationManager(EventBus eventBus, ReadWriteLock stateLock, EntityManager entityManager) {
    this.eventBus = eventBus;
    this.stateLock = stateLock;
    this.entityManager = entityManager;

    this.maxPopulation = getAlivePopulation();
    this.totalDeaths = 0;

    this.bornSub = this.eventBus.subscribe(ResidentBornEvent.class, event -> {
      withWriteLock(() -> {
        if (event.populationAlive() > maxPopulation) {
          maxPopulation = event.populationAlive();
        }
      });
    });

    this.diedSub = this.eventBus.subscribe(ResidentDiedEvent.class, event -> {
      withWriteLock(() -> {
        totalDeaths++;
      });
      if (event.populationAlive() == 0) {
        // ゲーム全体でゲームオーバー処理を開始するためにイベントを発行。
        this.eventBus.publish(new GameOverEvent());
        // ResidentDiedEventの購読を解除。
        diedSub.unsubscribe();
        if (bornSub != null) {
          bornSub.unsubscribe();
        }
      }
    });
  }

  /**
   * 総住民数(生存者のみ)を返す。
   *
   * @return 総住民数
   */
  public int getTotalPopulation() {
    return getAlivePopulation();
  }

  /**
   * 生存住民数を返す。
   *
   * @return 生存住民数
   */
  public int getAlivePopulation() {
    return withReadLock(() -> {
      int count = 0;
      for (Resident resident : entityManager.snapshotResidents()) {
        if (resident.getState() != ResidentState.DEAD) {
          count++;
        }
      }
      return count;
    });
  }

  /**
   * 死亡住民数を返す。
   *
   * @return 死亡住民数
   */
  public int getDeadPopulation() {
    return withReadLock(() -> {
      int count = 0;
      for (Resident resident : entityManager.snapshotResidents()) {
        if (resident.getState() == ResidentState.DEAD) {
          count++;
        }
      }
      return count;
    });
  }

  /**
   * 最大生存住民数を返す。
   *
   * @return 最大生存住民数
   */
  public int getMaxPopulation() {
    return withReadLock(() -> maxPopulation);
  }

  /**
   * 累計死亡住民数を返す。
   *
   * @return 累計死亡住民数
   */
  public int getTotalDeaths() {
    return withReadLock(() -> totalDeaths);
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
    Lock writeLock = stateLock.writeLock();
    writeLock.lock();
    try {
      action.run();
    } finally {
      writeLock.unlock();
    }
  }
}
