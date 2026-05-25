package io.github.sasori_256.town_planning.map.model;

import java.awt.geom.Point2D;

import io.github.sasori_256.town_planning.entity.building.Building;

/**
 * マップ操作に必要な最小限の読み書きAPIを定義する。
 */
public interface MapContext {
  /**
   * マップ上のセルの位置が有効かどうかを確認する。
   *
   * @param position 確認する位置
   */
  public boolean isValidPosition(Point2D.Double position);

  /**
   * マップ上のセルを取得する。
   *
   * @param position 取得するセルの位置
   */
  public MapCell getCell(Point2D.Double position);

  /**
   * マップ上のセルに建物を配置する。
   *
   * @param position 建物を配置する位置
   * @param building 配置する建物
   */
  public boolean placeBuilding(Point2D.Double position, Building building);

  /**
   * マップ上のセルの建物を削除する。
   *
   * @param position 削除する建物の位置
   */
  public boolean removeBuilding(Point2D.Double position);

  /**
   * マップの横幅(セル数)を返す。
   *
   * @return 横幅
   */
  public int getWidth();

  /**
   * マップの縦幅(セル数)を返す。
   *
   * @return 縦幅
   */
  public int getHeight();
}
