package io.github.sasori_256.town_planning.gameObject.building.strategy;

import io.github.sasori_256.town_planning.common.core.strategy.UpdateStrategy;
import io.github.sasori_256.town_planning.common.event.EventType;
import io.github.sasori_256.town_planning.gameObject.model.BaseGameEntity;
import io.github.sasori_256.town_planning.gameObject.model.GameContext;
import io.github.sasori_256.town_planning.gameObject.resident.ResidentObject;
import io.github.sasori_256.town_planning.gameObject.resident.ResidentType;

import java.util.concurrent.ThreadLocalRandom;

/**
 * 住居の機能：人口増加。
 * 一定時間ごとに新しい住民を生成する。
 */
public class PopulationGrowthStrategy implements UpdateStrategy {
  private final int maxPopulation;
  private double timer = 0;
  private final double spawnInterval = 15.0; // 15秒ごとに判定
  private int currentPopulation = 0; // この家が生成した（管理している）住民数

  public PopulationGrowthStrategy(int maxPopulation) {
    this.maxPopulation = maxPopulation;
  }

  @Override
  public void update(GameContext context, BaseGameEntity self) {
    if (currentPopulation >= maxPopulation) {
      return;
    }

    timer += context.getDeltaTime();
    if (timer >= spawnInterval) {
      timer = 0;
      // 50%の確率で住民生成
      if (ThreadLocalRandom.current().nextBoolean()) {
        currentPopulation++;
      }
    }
  }
}
