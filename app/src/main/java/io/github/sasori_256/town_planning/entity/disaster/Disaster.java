package io.github.sasori_256.town_planning.entity.disaster;

import java.awt.geom.Point2D;

import io.github.sasori_256.town_planning.common.core.strategy.CompositeUpdateStrategy;
import io.github.sasori_256.town_planning.entity.model.BaseGameEntity;
import io.github.sasori_256.town_planning.entity.model.GameAction;

/**
 * Represents a disaster entity in the game world.
 * <p>
 * A {@code Disaster} is a {@link BaseGameEntity} placed at a specific position
 * on the map. Its concrete behavior and effects are defined by the associated
 * {@link DisasterType}, which is responsible for creating the {@link GameAction}
 * that drives the disaster's update logic via a {@link CompositeUpdateStrategy}.
 * </p>
 */
public class Disaster extends BaseGameEntity {
  private final DisasterType type;

  /**
   * Creates a new disaster entity at the specified position.
   * <p>
   * Initializes the disaster by creating a {@link CompositeUpdateStrategy} and
   * populating it with a {@link GameAction} obtained from the disaster type's
   * action factory. The {@code position} parameter is passed to the action factory
   * to create position-specific disaster behavior.
   * </p>
   *
   * @param position     the position where the disaster occurs; used to create
   *                     the disaster's action via the type's action factory
   * @param disasterType the type defining the disaster's characteristics and behavior
   */
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
