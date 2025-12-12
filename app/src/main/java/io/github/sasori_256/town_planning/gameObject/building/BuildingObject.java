package io.github.sasori_256.town_planning.gameObject.building;

import java.awt.geom.Point2D;

import io.github.sasori_256.town_planning.gameObject.model.GameObject;

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

  public int getCurrentDurability() {
    return this.currentDurability;
  }

  public void setCurrentDurability(int durability) {
    this.currentDurability = Math.max(0, Math.min(durability, this.type.getMaxDurability()));
  }

  public int getCurrentPopulation() {
    return this.currentPopulation;
  }

  public void setCurrentPopulation(int population) {
    this.currentPopulation = Math.max(0, Math.min(population, this.type.getMaxPopulation()));
  }
}
