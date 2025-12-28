package io.github.sasori_256.town_planning.entity.building.strategy;

import io.github.sasori_256.town_planning.entity.model.BaseGameEntity;
import io.github.sasori_256.town_planning.entity.model.GameContext;
import io.github.sasori_256.town_planning.entity.model.GameEffect;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 住居の機能：人口増加。
 * 一定時間ごとに新しい住民を生成する。
 * GameEffectとして実装（並行動作可能）。
 */
public class PopulationGrowthEffect implements GameEffect {
  private final int maxPopulation;
  private final double spawnInterval;
  private double timer;
  private int currentPopulation; // この家が生成した（管理している）住民数

  /**
   * コンストラクタ
   *
   * @param maxPopulation この建物が生成可能な最大住民数
   */
  public PopulationGrowthEffect(int maxPopulation) {
    this.maxPopulation = maxPopulation;
    this.spawnInterval = 15.0;
    this.timer = 0.0;
    this.currentPopulation = 0;
  }

  @Override
  public void execute(GameContext context, BaseGameEntity self) {
    if (currentPopulation >= maxPopulation) {
      return;
    }

    timer += context.getDeltaTime();
    if (timer >= spawnInterval) {
      timer = 0;
      // 50%の確率で住民生成
      if (ThreadLocalRandom.current().nextBoolean()) {
        currentPopulation++;
        // ここで本来は住民生成イベントなどを発火する
        System.out.println("Population increased! Total for this building: " + currentPopulation);
      }
    }
  }
}
