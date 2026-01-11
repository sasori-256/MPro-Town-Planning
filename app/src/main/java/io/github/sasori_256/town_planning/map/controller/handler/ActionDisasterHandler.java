package io.github.sasori_256.town_planning.map.controller.handler;

import java.awt.geom.Point2D;
import java.util.function.BiConsumer;
import java.util.function.Function;
import io.github.sasori_256.town_planning.entity.disaster.Disaster;
import io.github.sasori_256.town_planning.entity.model.BaseGameEntity;
import io.github.sasori_256.town_planning.entity.model.GameModel;
import io.github.sasori_256.town_planning.map.controller.GameMapController;
import io.github.sasori_256.town_planning.map.model.MapContext;

public class ActionDisasterHandler
    implements BiConsumer<Point2D.Double, Function<Point2D.Double, ? extends BaseGameEntity>> {
  private final GameModel gameModel;
  private final GameMapController gameMapController;
  private final MapContext mapContext;

  public ActionDisasterHandler(GameModel gameModel, GameMapController gameMapController,
      MapContext mapContext) {
    this.gameModel = gameModel;
    this.gameMapController = gameMapController;
    this.mapContext = mapContext;
  }

  @Override
  public void accept(Point2D.Double isoPoint, Function<Point2D.Double, ? extends BaseGameEntity> entityGenerator) {
    if (entityGenerator == null) {
      return;
    }
    Point2D.Double roundedPoint = new Point2D.Double(Math.round(isoPoint.x), Math.round(isoPoint.y));
    if (mapContext != null && !mapContext.isValidPosition(roundedPoint)) {
      System.err.println("Error: Invalid disaster position " + roundedPoint);
      return;
    }
    BaseGameEntity entity = entityGenerator.apply(roundedPoint);
    if (entity instanceof Disaster disaster) {
      gameModel.spawnEntity(disaster);
    } else {
      System.err.println("Error: Trying to spawn a disaster that is not a Disaster.");
    }
    gameMapController.setSelectedEntityGenerator((point) -> null);
    gameMapController.setActionOnClick(new ClickGameMapHandler());
  }
}
