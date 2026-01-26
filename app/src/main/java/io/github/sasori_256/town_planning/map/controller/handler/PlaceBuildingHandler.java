package io.github.sasori_256.town_planning.map.controller.handler;

import java.awt.geom.Point2D;
import java.util.function.BiConsumer;
import java.util.function.Function;

import io.github.sasori_256.town_planning.common.event.EventBus;
import io.github.sasori_256.town_planning.common.event.events.TemporaryBuildEvent;
import io.github.sasori_256.town_planning.entity.model.BaseGameEntity;
import io.github.sasori_256.town_planning.entity.model.GameModel;

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
    gameModel.getBuildingPreview().setEntityGenerator(entityGenerator);
    gameModel.getBuildingPreview().setBuildingPreviewPos(isoPoint);
    if (gameModel.getBuildingPreview().getBuildable()) {
      eventBus.publish(new TemporaryBuildEvent());
    }
  }
}
