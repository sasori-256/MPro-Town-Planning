package io.github.sasori_256.town_planning.entity.disaster;

import java.awt.geom.Point2D;

import io.github.sasori_256.town_planning.common.core.strategy.CompositeUpdateStrategy;
import io.github.sasori_256.town_planning.entity.model.BaseGameEntity;
import io.github.sasori_256.town_planning.entity.model.GameAction;

public class Disaster extends BaseGameEntity {
  private final DisasterType type;

  public Disaster(Point2D.Double position, DisasterType disasterType) {
    super(position);
    this.type = disasterType;

    CompositeUpdateStrategy strategy = new CompositeUpdateStrategy();

    GameAction action = disasterType.createAction(position);
    if (action != null) {
      strategy.setAction(action);
    }

    this.setUpdateStrategy(strategy);
  }

  public DisasterType getType() {
    return this.type;
  }
}
