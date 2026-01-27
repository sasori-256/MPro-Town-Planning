package io.github.sasori_256.town_planning.map.controller.handler;

import java.awt.geom.Point2D;
import java.util.function.BiConsumer;
import java.util.function.Function;

import io.github.sasori_256.town_planning.common.event.EventBus;
import io.github.sasori_256.town_planning.common.event.events.EntitySpawnFailedEvent;
import io.github.sasori_256.town_planning.common.event.events.EntitySpawnFailureReason;
import io.github.sasori_256.town_planning.common.event.events.EntitySpawnKind;
import io.github.sasori_256.town_planning.common.event.events.TemporaryBuildEvent;
import io.github.sasori_256.town_planning.entity.building.BuildingType;
import io.github.sasori_256.town_planning.entity.model.BaseGameEntity;
import io.github.sasori_256.town_planning.entity.model.GameModel;
import io.github.sasori_256.town_planning.map.controller.GameMapController;
import io.github.sasori_256.town_planning.map.model.BuildingPreview;

/**
 * クリック位置に建物を配置するハンドラ。
 */
public class PlaceBuildingHandler
    implements BiConsumer<Point2D.Double, Function<Point2D.Double, ? extends BaseGameEntity>> {
  private GameModel gameModel;
  private final EventBus eventBus = EventBus.getInstance();

  /**
   * 配置ハンドラを生成する。
   *
   * @param gameModel         ゲームモデル
   * @param gameMapController マップコントローラ
   */
  public PlaceBuildingHandler(GameModel gameModel) {
    this.gameModel = gameModel;
  }

  /**
   * クリック位置に対して建物配置を試みる。
   *
   * @param isoPoint        クリック位置(アイソメトリック座標)
   * @param entityGenerator 生成関数
   */
  @Override
  public void accept(Point2D.Double isoPoint, Function<Point2D.Double, ? extends BaseGameEntity> entityGenerator) {
    BuildingPreview preview = gameModel.getBuildingPreview();
    preview.setEntityGenerator(entityGenerator);
    preview.setBuildingPreviewPos(isoPoint);
    Point2D.Double pos = preview.getBuildingPreviewPos();
    BuildingType type = preview.getBuildingPreviewType();
    EntitySpawnFailureReason reason = gameModel.validateConstruction(pos, type);
    if (reason == null) {
      eventBus.publish(new TemporaryBuildEvent());
      return;
    }
    String detail = BuildingType.getDetailString(type);
    eventBus.publish(new EntitySpawnFailedEvent(EntitySpawnKind.BUILDING, reason, pos, detail));
  }
}
