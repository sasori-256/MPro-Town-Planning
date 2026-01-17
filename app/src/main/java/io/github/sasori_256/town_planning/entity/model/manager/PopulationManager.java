package io.github.sasori_256.town_planning.entity.model.manager;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Supplier;

import io.github.sasori_256.town_planning.entity.resident.Resident;
import io.github.sasori_256.town_planning.entity.resident.ResidentState;

/**
 * 住民数の集計を管理する。
 */
public class PopulationManager {
  /** 状態同期に使用するロック。 */
  private final ReadWriteLock stateLock;
  /** エンティティ管理。 */
  private final EntityManager entityManager;

  /**
   * 住民数管理を生成する。
   *
   * @param stateLock     状態ロック
   * @param entityManager エンティティ管理
   */
  public PopulationManager(ReadWriteLock stateLock, EntityManager entityManager) {
    this.stateLock = stateLock;
    this.entityManager = entityManager;
  }

  /**
   * 総住民数を返す。
   *
   * @return 総住民数
   */
  public int getTotalPopulation() {
    return withReadLock(() -> entityManager.snapshotResidents().size());
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
}
