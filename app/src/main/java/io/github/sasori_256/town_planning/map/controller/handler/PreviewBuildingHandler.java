package io.github.sasori_256.town_planning.map.controller.handler;

import java.awt.geom.Point2D;
import java.util.function.BiConsumer;
import java.util.function.Function;

import io.github.sasori_256.town_planning.entity.model.BaseGameEntity;
import io.github.sasori_256.town_planning.entity.model.GameModel;
import io.github.sasori_256.town_planning.common.event.EventBus;
import io.github.sasori_256.town_planning.entity.building.BuildingType;

public class PreviewBuildingHandler
    implements BiConsumer<Point2D.Double, Function<Point2D.Double, ? extends BaseGameEntity>> {
  GameModel gameModel;
  BuildingType buildingType;
  EventBus eventBus = EventBus.getInstance();

  public PreviewBuildingHandler(GameModel gameModel, BuildingType buildingType) {
    this.gameModel = gameModel;
    this.buildingType = buildingType;
    gameModel.getBuildingPreview().setBuildingPreviewType(buildingType);
  }

  @Override
  public void accept(Point2D.Double isoPoint, Function<Point2D.Double, ? extends BaseGameEntity> entityGenerator) {
    gameModel.getBuildingPreview().setBuildingPreviewPos(isoPoint);

  }

}
