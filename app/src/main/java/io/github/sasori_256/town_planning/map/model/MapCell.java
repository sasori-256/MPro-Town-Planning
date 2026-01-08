package io.github.sasori_256.town_planning.map.model;

import java.awt.geom.Point2D;
import io.github.sasori_256.town_planning.entity.building.Building;

public class MapCell {
  private final Point2D.Double position;
  private Terrain terrain;
  private Building building;
  private int localX;
  private int localY;

  public MapCell(Point2D.Double position, Terrain initTerrain) {
    this.position = position;
    this.terrain = initTerrain;
    // Buildingはnull許容に変更してResident通過判定や描画時に余計なメンバーにアクセス不可に
    this.building = null;
    this.localX = 0;
    this.localY = 0;
  }

  public Point2D.Double getPosition() {
    return position;
  }

  public Terrain getTerrain() {
    return terrain;
  }

  public boolean setTerrain(Terrain terrain) {
    this.terrain = terrain;
    return true;
  }

  public Building getBuilding() {
    return building;
  }

  public int getLocalX() {
    return localX;
  }

  public int getLocalY() {
    return localY;
  }

  public boolean isOccupied() {
    return building != null;
  }

  public void setBuilding(Building building, int localX, int localY) {
    this.building = building;
    this.localX = localX;
    this.localY = localY;
  }

  public void clearBuilding() {
    this.building = null;
    this.localX = 0;
    this.localY = 0;
  }

  /**
   * 建物を建てられるかどうかを判定する
   * 単純に地形が建築可能かつ建物が存在しない場合にtrueを返す
   */
  public boolean canBuild() {
    return terrain.isBuildable() && building == null;
  }

  /**
   * 住民が歩けるかどうかを判定する
   * 地形が歩行可能かつ建物が侵入可能ならtrueを返す
   */
  public boolean canWalk() {
    if (!terrain.isWalkable()) {
      return false;
    }
    if (building == null) {
      return true;
    }
    return building.getType().isWalkable(localX, localY);
  }

  public int getMoveCost() {
    if (building == null) {
      return terrain.getMoveCost();
    }
    return building.getType().getMoveCost(localX, localY);
  }
}
