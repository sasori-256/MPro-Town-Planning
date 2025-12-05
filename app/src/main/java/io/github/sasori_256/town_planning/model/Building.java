package io.github.sasori_256.town_planning.model;

import io.github.sasori_256.town_planning.core.*;
import io.github.sasori_256.town_planning.event.*;
import java.util.*;
import java.awt.geom.Point2D;

/**
 * 建物を表すクラス。
 * 各建物は一意のID、位置情報、建物の種類を持つ。
 * TODO: 一旦abstractにはせず、将来的に必要に応じてサブクラス化する。
 */
class Building implements GameEntity, Renderable {
  private final String id;
  private final Point2D position;
  private final BuildingType type;

  public Building(String id, Point2D position, BuildingType type) {
    this.id = id;
    this.position = position;
    this.type = type;
  }

  /**
   * IDを取得する。
   *
   * @return ID
   */
  public String getID() {
    return this.id;
  }

  /**
   * 位置情報を取得する。
   *
   * @return 位置情報
   */
  public Point2D getPosition() {
    return this.position;
  }

  /**
   * 表示用のシンボルを取得する(仮置き)。
   * TODO: 将来的にグラフィックに置き換える。
   *
   * @return 表示用のシンボル(仮置き)
   */
  public String getDisplaySymbol() {
    return this.type.getSymbol();
  }

  /**
   * 建物の種類を取得する。
   *
   * @return 建物の種類
   */
  public BuildingType getType() {
    return this.type;
  }
}
