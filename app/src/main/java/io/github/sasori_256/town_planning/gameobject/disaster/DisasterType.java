package io.github.sasori_256.town_planning.gameobject.disaster;

import io.github.sasori_256.town_planning.gameobject.model.CategoryType;

/**
 * 災害の種類定義。
 */
public enum DisasterType {
  METEOR("隕石", 200, 3, 100, CategoryType.METEOR),
  PLAGUE("疫病", 150, 5, 20, CategoryType.PLAGUE);

  private final String displayName;
  private final int cost;
  private final int radius;
  private final int damage;
  private final CategoryType category;

  DisasterType(String displayName, int cost, int radius, int damage, CategoryType category) {
    this.displayName = displayName;
    this.cost = cost;
    this.radius = radius;
    this.damage = damage;
    this.category = category;
  }

  public String getDisplayName() {
    return displayName;
  }

  public int getCost() {
    return cost;
  }

  public int getRadius() {
    return radius;
  }

  public int getDamage() {
    return damage;
  }

  public CategoryType getCategory() {
    return category;
  }
}
