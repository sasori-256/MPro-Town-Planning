package io.github.sasori_256.town_planning.gameobject.model;

import java.awt.geom.Point2D;

import io.github.sasori_256.town_planning.common.core.strategy.UpdateStrategy;

/**
 * ゲームのエンティティを表すインターフェース。
 * 各エンティティは一意のIDと位置情報を持つ。
 */
public interface GameEntity {
  int getLayerIndex();

  void setLayerIndex(int layerIndex);

  Point2D.Double getPosition();

  void setPosition(Point2D.Double position);

  void setUpdateStrategy(UpdateStrategy upadteStrategy);

  void removeEntity();
}
