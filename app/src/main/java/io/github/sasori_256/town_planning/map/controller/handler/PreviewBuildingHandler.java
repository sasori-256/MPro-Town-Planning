package io.github.sasori_256.town_planning.map.controller.handler;

import java.awt.geom.Point2D;
import java.util.function.BiConsumer;
import java.util.function.Function;

import io.github.sasori_256.town_planning.entity.model.BaseGameEntity;
import io.github.sasori_256.town_planning.entity.model.GameModel;
import io.github.sasori_256.town_planning.entity.building.BuildingType;

public class PreviewBuildingHandler
    implements BiConsumer<Point2D.Double, Function<Point2D.Double, ? extends BaseGameEntity>> {
  private final GameModel gameModel;

  public PreviewBuildingHandler(GameModel gameModel, BuildingType buildingType) {
    this.gameModel = gameModel;
    gameModel.getBuildingPreview().setBuildingPreviewType(buildingType);
  }

  @Override
  public void accept(Point2D.Double isoPoint, Function<Point2D.Double, ? extends BaseGameEntity> entityGenerator) {
    gameModel.getBuildingPreview().setBuildingPreviewPos(isoPoint);

  }

}
