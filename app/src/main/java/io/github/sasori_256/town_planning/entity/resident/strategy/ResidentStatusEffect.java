package io.github.sasori_256.town_planning.entity.resident.strategy;

import io.github.sasori_256.town_planning.common.core.strategy.UpdateStrategy;
import io.github.sasori_256.town_planning.entity.model.BaseGameEntity;
import io.github.sasori_256.town_planning.entity.model.GameContext;
import io.github.sasori_256.town_planning.entity.model.GameEffect;
import io.github.sasori_256.town_planning.entity.resident.Resident;

/**
 * 住民の状態異常（デバフなど）を管理するEffect。
 */
public class ResidentStatusEffect implements UpdateStrategy, GameEffect {

  /** {@inheritDoc} */
  @Override
  public void update(GameContext context, BaseGameEntity self) {
    if (!(self instanceof Resident)) {
      return;
    }
    Resident resident = (Resident) self;
    resident.updateDebuffs(context);
  }

  /** {@inheritDoc} */
  @Override
  public void execute(GameContext context, BaseGameEntity self) {
    update(context, self);
  }
}
