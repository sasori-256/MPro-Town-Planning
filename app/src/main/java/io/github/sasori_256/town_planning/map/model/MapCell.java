package io.github.sasori_256.town_planning.map.model;

import java.awt.geom.Point2D;

import io.github.sasori_256.town_planning.entity.building.Building;
import io.github.sasori_256.town_planning.entity.building.BuildingType;

public class MapCell {
  private final Point2D.Double position;
  private Terrain terrain;
  private Building building;

  public MapCell(Point2D.Double position, Terrain initTerrain) {
    this.position = position;
    this.terrain = initTerrain;
    // TODO: インスタンスプールを導入してメモリ効率を改善する
    this.building = new Building(position, BuildingType.NONE);
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

  public void setBuilding(Building building) {
    this.building = building;
  }

  public void removeBuilding() {
    this.building = new Building(this.position, BuildingType.NONE); // nullではなくNONEタイプを設定
  }

  /**
   * 建物を建てられるかどうかを判定する
   * 単純に地形が建築可能かつ建物が存在しない場合にtrueを返す
   */
  public boolean canBuild() {
    return terrain.isBuildable() && building.getType() == BuildingType.NONE;
  }

  /**
   * 住民が歩けるかどうかを判定する
   * 地形が歩行可能かつ建物が存在しない場合にtrueを返す
   */
  public boolean canWalk() {
    boolean terrainOk = terrain.isWalkable();
    boolean buildingOk = (building.getType() == BuildingType.NONE);
    return terrainOk && buildingOk;
  }
}
