package io.github.sasori_256.town_planning.model;

import java.awt.geom.Point2D;

/**
 * ゲームのエンティティを表すインターフェース。
 * 各エンティティは一意のIDと位置情報を持つ。
 */
public interface GameEntity {
  String getId();

  Point2D getPosition();
}
