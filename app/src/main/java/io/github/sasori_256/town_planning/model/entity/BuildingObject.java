package io.github.sasori_256.town_planning.model;

import io.github.sasori_256.town_planning.model.GameObject;
import io.github.sasori_256.town_planning.model.BuildingType;
import java.awt.geom.Point2D;

/**
 * 建物オブジェクトを表すクラス。
 * 建物の種類や特性を持つ。
 */
public class BuildingObject extends GameObject {
  private final BuildingType type;
  private int currentDurability;
  private int currentPopulation;

  public BuildingObject(Point2D position, BuildingType buildingType) {
    super(position);
    this.type = buildingType;
    this.currentDurability = buildingType.getMaxDurability();
    this.currentPopulation = 0;
  }

  public BuildingType getType() {
    return this.type;
  }

  public void setType(BuildingType buildingType) {
    this.type = buildingType;
  }
}
