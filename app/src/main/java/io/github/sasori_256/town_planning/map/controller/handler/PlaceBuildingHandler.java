package io.github.sasori_256.town_planning.map.controller.handler;

import java.awt.geom.Point2D;
import java.util.function.BiConsumer;
import java.util.function.Function;
import io.github.sasori_256.town_planning.entity.building.Building;
import io.github.sasori_256.town_planning.entity.model.BaseGameEntity;
import io.github.sasori_256.town_planning.entity.model.GameModel;
import io.github.sasori_256.town_planning.map.controller.GameMapController;

public class PlaceBuildingHandler
    implements BiConsumer<Point2D.Double, Function<Point2D.Double, ? extends BaseGameEntity>> {
  private GameModel gameModel;
  private GameMapController gameMapController;

  public PlaceBuildingHandler(GameModel gameModel, GameMapController gameMapController) {
    this.gameModel = gameModel;
    this.gameMapController = gameMapController;
  }

  @Override
  public void accept(Point2D.Double isoPoint, Function<Point2D.Double, ? extends BaseGameEntity> entityGenerator) {
    System.out.println("Placing building at: " + isoPoint);
    Point2D.Double roundedPoint = new Point2D.Double(Math.round(isoPoint.x), Math.round(isoPoint.y));
    BaseGameEntity entity = entityGenerator.apply(roundedPoint);
    if (entity instanceof Building building) {
      boolean constructed = gameModel.constructBuilding(roundedPoint, building.getType());
      if (!constructed) {
        System.err.println("Error: Failed to construct building of type " + building.getType()
            + " at position " + roundedPoint + ".");
      }
    } else {
      System.err.println("Error: Trying to place a building that is not a Building.");
    }
    gameMapController.setSelectedEntityGenerator((point) -> null); // TODO:Buildingの連続配置をしたい場合、これじゃだめ
    gameMapController.setActionOnClick(new ClickGameMapHandler());
  }
}
