package io.github.sasori_256.town_planning.common.ui.BuildPreview.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import io.github.sasori_256.town_planning.common.event.EventBus;
import io.github.sasori_256.town_planning.common.event.events.CancelBuildEvent;
import io.github.sasori_256.town_planning.common.event.events.EntitySpawnFailedEvent;
import io.github.sasori_256.town_planning.common.event.events.EntitySpawnFailureReason;
import io.github.sasori_256.town_planning.common.event.events.EntitySpawnKind;
import io.github.sasori_256.town_planning.entity.building.Building;
import io.github.sasori_256.town_planning.entity.model.BaseGameEntity;
import io.github.sasori_256.town_planning.entity.model.GameModel;
import io.github.sasori_256.town_planning.map.model.BuildingPreview;

public class BuildAccept implements ActionListener {
  private final EventBus eventBus = EventBus.getInstance();
  private GameModel gameModel;
  private BuildingPreview buildingPreview;

  public BuildAccept(GameModel gameModel) {
    this.gameModel = gameModel;
    this.buildingPreview = gameModel.getBuildingPreview();
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    // System.out.println("Placing building at: " + isoPoint);

    BaseGameEntity entity = buildingPreview.getEntityGenerator().apply(buildingPreview.getBuildingPreviewPos());
    if (entity instanceof Building building) {
      gameModel.constructBuilding(buildingPreview.getBuildingPreviewPos(), building.getType());
    } else {
      eventBus.publish(new EntitySpawnFailedEvent(
          EntitySpawnKind.BUILDING,
          EntitySpawnFailureReason.INVALID_ENTITY,
          buildingPreview.getBuildingPreviewPos(),
          "generated=" + (entity == null ? "null" : entity.getClass().getSimpleName())));
    }
    eventBus.publish(new CancelBuildEvent());
  }

}
