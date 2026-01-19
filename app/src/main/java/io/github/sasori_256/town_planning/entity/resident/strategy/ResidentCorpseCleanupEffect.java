package io.github.sasori_256.town_planning.entity.resident.strategy;

import io.github.sasori_256.town_planning.common.core.GameConfig;
import io.github.sasori_256.town_planning.common.event.events.SoulHarvestedEvent;
import io.github.sasori_256.town_planning.entity.model.BaseGameEntity;
import io.github.sasori_256.town_planning.entity.model.GameContext;
import io.github.sasori_256.town_planning.entity.model.GameEffect;
import io.github.sasori_256.town_planning.entity.resident.Resident;
import io.github.sasori_256.town_planning.entity.resident.ResidentState;

/**
 * 死亡後一定時間経過で消滅し、魂を獲得するEffect。
 */
public class ResidentCorpseCleanupEffect implements GameEffect {
  private static final double HARVEST_DELAY_SECONDS = GameConfig.getCorpseHarvestDelaySeconds();
  private static final int BASE_SOUL = GameConfig.getCorpseSoulBase();
  private static final int FAITH_DIVISOR =
      Math.max(1, GameConfig.getCorpseSoulFaithDivisor());

  private double elapsed;
  private boolean harvested;

  /** {@inheritDoc} */
  @Override
  public void execute(GameContext context, BaseGameEntity self) {
    if (!(self instanceof Resident)) {
      return;
    }
    Resident resident = (Resident) self;
    if (resident.getState() != ResidentState.DEAD) {
      elapsed = 0.0;
      harvested = false;
      return;
    }
    if (harvested) {
      return;
    }
    double dt = context.getDeltaTime();
    if (dt <= 0.0) {
      return;
    }
    elapsed += dt;
    if (elapsed < HARVEST_DELAY_SECONDS) {
      return;
    }
    harvested = true;

    int soulAmount = BASE_SOUL + (resident.getFaith() / FAITH_DIVISOR);
    context.getEventBus().publish(new SoulHarvestedEvent(soulAmount));
    context.removeEntity(resident);
  }
}
