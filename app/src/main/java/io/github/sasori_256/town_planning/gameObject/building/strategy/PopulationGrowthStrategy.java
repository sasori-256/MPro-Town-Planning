package io.github.sasori_256.town_planning.gameObject.building.strategy;

import io.github.sasori_256.town_planning.common.core.strategy.UpdateStrategy;
import io.github.sasori_256.town_planning.common.event.EventType;
import io.github.sasori_256.town_planning.gameObject.model.GameContext;
import io.github.sasori_256.town_planning.gameObject.model.GameObject;
import io.github.sasori_256.town_planning.gameObject.resident.ResidentObject;
import io.github.sasori_256.town_planning.gameObject.resident.ResidentType;

import java.awt.Color;
import java.awt.geom.Point2D;
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
  public void update(GameContext context, GameObject self) {
    if (currentPopulation >= maxPopulation) {
      return;
    }

    timer += context.getDeltaTime();
    if (timer >= spawnInterval) {
      timer = 0;
      // 50%の確率で住民生成
      if (ThreadLocalRandom.current().nextBoolean()) {
        spawnResident(context, self.getPosition());
        currentPopulation++;
      }
    }
  }

  private void spawnResident(GameContext context, Point2D spawnPos) {
    GameObject resident = new ResidentObject(spawnPos, ResidentType.CITIZEN);

    // Strategy設定
    resident.setUpdateStrategy(new CompositeUpdateStrategy(
        new RandomMoveStrategy(),
        new ResidentLifeCycleStrategy()));

    // 生存時の見た目
    SimpleRenderStrategy aliveVisual = new SimpleRenderStrategy(Color.ORANGE, "R", 20);
    // 状態によって切り替えるラッパー
    resident.setRenderStrategy(new ResidentRenderStrategy(aliveVisual));

    context.spawnEntity(resident);
    context.getEventBus().publish(EventType.RESIDENT_BORN, resident);
  }
}
