package io.github.sasori_256.town_planning.map.controller.handler;

import java.awt.geom.Point2D;
import java.util.function.BiConsumer;
import java.util.function.Function;

import io.github.sasori_256.town_planning.entity.model.BaseGameEntity;

/**
 * クリック時のデフォルト動作を行うハンドラ。
 */
public class ClickGameMapHandler
    implements BiConsumer<Point2D.Double, Function<Point2D.Double, ? extends BaseGameEntity>> {
  /**
   * クリック位置の処理を行う。
   *
   * @param isoPoint        クリック位置(アイソメトリック座標)
   * @param entityGenerator 生成関数
   */
  @Override
  public void accept(Point2D.Double isoPoint, Function<Point2D.Double, ? extends BaseGameEntity> entityGenerator) {
    // TODO:
    System.out.println("Clicked at Iso Coordinates: (" + isoPoint.x + ", " + isoPoint.y + ")");
  }

}
