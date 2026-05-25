package io.github.sasori_256.town_planning.entity.building.strategy;

import io.github.sasori_256.town_planning.common.core.GameConfig;
import io.github.sasori_256.town_planning.entity.building.Building;
import io.github.sasori_256.town_planning.entity.model.BaseGameEntity;
import io.github.sasori_256.town_planning.entity.model.GameContext;
import io.github.sasori_256.town_planning.entity.model.GameEffect;
import io.github.sasori_256.town_planning.entity.resident.Resident;
import io.github.sasori_256.town_planning.entity.resident.ResidentState;
import io.github.sasori_256.town_planning.entity.resident.ResidentType;
import java.awt.geom.Point2D;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 住居の機能：人口増加。
 * 一定時間ごとに新しい住民を生成する。
 * GameEffectとして実装（並行動作可能）。
 */
public class PopulationGrowthEffect implements GameEffect {
  // 住民生成の判定間隔(秒)。
  private static final double SPAWN_INTERVAL = GameConfig.getResidentGrowthSpawnIntervalSeconds();
  private static final int MIN_HOME_POPULATION = GameConfig.getResidentGrowthMinHomePopulation();
  private static final double SPAWN_CHANCE =
      Math.clamp(GameConfig.getResidentGrowthSpawnChance(), 0.0, 1.0);
  private final int maxPopulation;
  private final double spawnInterval;
  private double timer;

  /**
   * コンストラクタ
   *
   * @param maxPopulation この建物が生成可能な最大住民数
   */
  public PopulationGrowthEffect(int maxPopulation) {
    this.maxPopulation = maxPopulation;
    this.spawnInterval = SPAWN_INTERVAL;
    this.timer = 0.0;
  }

  /** {@inheritDoc} */
  @Override
  public void execute(GameContext context, BaseGameEntity self) {
    if (!(self instanceof Building)) {
      return;
    }
    Building building = (Building) self;
    int currentPopulation = building.getCurrentPopulation();
    if (currentPopulation < MIN_HOME_POPULATION) {
      return;
    }
    if (currentPopulation >= maxPopulation) {
      return;
    }

    timer += context.getDeltaTime();
    if (timer < spawnInterval) {
      return;
    }
    timer = 0.0;

    if (ThreadLocalRandom.current().nextDouble() >= SPAWN_CHANCE) {
      return;
    }

    Point2D.Double homePos = building.getPosition();
    ResidentType type = selectResidentType();
    Resident resident = new Resident(new Point2D.Double(homePos.getX(), homePos.getY()), type,
        ResidentState.AT_HOME, homePos);
    context.spawnEntity(resident);
    building.setCurrentPopulation(currentPopulation + 1);
  }

  private ResidentType selectResidentType() {
    ResidentType[] types = ResidentType.values();
    return types[ThreadLocalRandom.current().nextInt(types.length)];
  }
}
