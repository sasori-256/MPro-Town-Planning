package io.github.sasori_256.town_planning.entity.model.manager;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Supplier;

import io.github.sasori_256.town_planning.entity.building.Building;
import io.github.sasori_256.town_planning.entity.model.CategoryType;
import io.github.sasori_256.town_planning.entity.resident.Resident;
import io.github.sasori_256.town_planning.entity.resident.ResidentState;

/**
 * 住民の引っ越しと住宅人口の均一化を管理する。
 */
public class RelocationManager {
  /** 住民再配置で最低限成立させる世帯人数。 */
  private static final int MIN_RESIDENTS_PER_HOUSE = 2;
  /** 状態同期に使用するロック。 */
  private final ReadWriteLock stateLock;
  /** エンティティ管理。 */
  private final EntityManager entityManager;

  /**
   * 引っ越し管理を生成する。
   *
   * @param stateLock     状態ロック
   * @param entityManager エンティティ管理
   */
  public RelocationManager(ReadWriteLock stateLock, EntityManager entityManager) {
    this.stateLock = stateLock;
    this.entityManager = entityManager;
  }

  /**
   * 住民数を住宅に均等化し、引っ越し対象を決める。
   */
  public void rebalanceResidents() {
    withWriteLock(() -> {
      List<Building> houses = new ArrayList<>();
      for (Building building : entityManager.snapshotBuildings()) {
        if (building.getType().getCategory() == CategoryType.RESIDENTIAL) {
          houses.add(building);
        }
      }
      if (houses.isEmpty()) {
        return null;
      }

      // 各家の統計情報を設定
      Map<HomeKey, HouseStats> statsByHome = new HashMap<>();
      int totalResidents = 0;
      for (Resident resident : entityManager.snapshotResidents()) {
        if (resident.getState() == ResidentState.DEAD) {
          continue;
        }
        Point2D.Double assignedHome = resident.hasRelocationTarget()
            ? resident.getRelocationTarget()
            : resident.getHomePosition();
        if (assignedHome == null) {
          continue;
        }
        HomeKey key = HomeKey.from(assignedHome);
        HouseStats stats = statsByHome.computeIfAbsent(key, k -> new HouseStats());
        stats.assignedCount++;
        if (!resident.hasRelocationTarget()) {
          stats.movableResidents.add(resident);
        }
        totalResidents++;
      }
      if (totalResidents == 0) {
        for (Building building : houses) {
          building.setCurrentPopulation(0);
        }
        return null;
      }

      // 家ごとの統計情報を集計
      List<HouseInfo> houseInfos = new ArrayList<>();
      for (Building building : houses) {
        HomeKey key = HomeKey.from(building.getPosition());
        HouseStats stats = statsByHome.get(key);
        int assignedCount = stats != null ? stats.assignedCount : 0;
        List<Resident> movable = stats != null
            ? new ArrayList<>(stats.movableResidents)
            : new ArrayList<>();
        houseInfos.add(new HouseInfo(building, assignedCount, movable));
      }
      // 住民数の降順にソートし、人口の多い家を優先的に処理できるようにする。
      houseInfos.sort(Comparator.comparingInt((HouseInfo info) -> info.assignedCount).reversed());

      // 引っ越し先の絞り込み
      int houseCount = houseInfos.size();
      List<Integer> targets = new ArrayList<>(houseCount);
      // 人口増加に必要な最小住民数を確保
      if (totalResidents >= MIN_RESIDENTS_PER_HOUSE * houseCount) {
        // 全人口が全世帯の人口増加に必要な最小住民数を満たしている場合
        int base = totalResidents / houseCount;
        int remainder = totalResidents % houseCount;
        for (int i = 0; i < houseCount; i++) {
          targets.add(base + (i < remainder ? 1 : 0));
        }
      } else {
        // 家に対して全人口が足りていない場合
        // 人口増加できる世帯の最大値
        int fullHouseCount = totalResidents / MIN_RESIDENTS_PER_HOUSE;
        // 独身世帯の数
        int remainder = totalResidents % MIN_RESIDENTS_PER_HOUSE;
        // 住民の割り振り
        for (int i = 0; i < houseCount; i++) {
          targets.add(i < fullHouseCount ? MIN_RESIDENTS_PER_HOUSE : 0);
        }
        if (remainder > 0) {
          // 独身者は2人以上の家に引っ越し
          targets.set(0, targets.get(0) + remainder);
        }
      }

      // 具体的な引っ越し先の決定
      List<Resident> movers = new ArrayList<>();
      List<HouseNeed> needs = new ArrayList<>();
      for (int i = 0; i < houseCount; i++) {
        HouseInfo info = houseInfos.get(i);
        int target = targets.get(i);
        int current = info.assignedCount;
        if (current > target) {
          // 住民が過剰にいる場合
          // 出ていく人数
          int excess = current - target;
          for (int j = 0; j < excess && !info.movableResidents.isEmpty(); j++) {
            movers.add(info.movableResidents.remove(info.movableResidents.size() - 1));
            info.assignedCount--;
          }
        } else if (current < target) {
          // 住民が不足している場合
          // 不足情報の登録
          needs.add(new HouseNeed(info, target - current));
        }
      }

      for (HouseNeed need : needs) {
        while (need.remaining > 0 && !movers.isEmpty()) {
          Resident resident = movers.remove(movers.size() - 1);
          Point2D.Double newHome = need.house.building.getPosition();
          resident.requestRelocation(newHome);
          resident.setState(ResidentState.RELOCATING);
          need.house.assignedCount++;
          need.remaining--;
        }
      }

      for (HouseInfo info : houseInfos) {
        info.building.setCurrentPopulation(info.assignedCount);
      }

      return null;
    });
  }

  /**
   * 住民の所属先を世帯単位で集計するためのキー。
   */
  private static final class HomeKey {
    private final int x;
    private final int y;

    private HomeKey(int x, int y) {
      this.x = x;
      this.y = y;
    }

    private static HomeKey from(Point2D.Double pos) {
      return new HomeKey((int) Math.round(pos.getX()), (int) Math.round(pos.getY()));
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (!(obj instanceof HomeKey)) {
        return false;
      }
      HomeKey other = (HomeKey) obj;
      return x == other.x && y == other.y;
    }

    @Override
    public int hashCode() {
      return 31 * x + y;
    }
  }

  /**
   * 世帯ごとの割り当て調整に使う作業用情報。
   */
  private static final class HouseInfo {
    private final Building building;
    private int assignedCount;
    private final List<Resident> movableResidents;

    private HouseInfo(Building building, int assignedCount, List<Resident> movableResidents) {
      this.building = building;
      this.assignedCount = assignedCount;
      this.movableResidents = movableResidents;
    }
  }

  /**
   * 需要側の家（不足人数）を表す作業用情報。
   */
  private static final class HouseNeed {
    private final HouseInfo house;
    private int remaining;

    private HouseNeed(HouseInfo house, int count) {
      this.house = house;
      this.remaining = count;
    }
  }

  /**
   * 家ごとの統計情報を保持する作業用モデル。
   */
  private static final class HouseStats {
    private int assignedCount;
    private final List<Resident> movableResidents = new ArrayList<>();
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
