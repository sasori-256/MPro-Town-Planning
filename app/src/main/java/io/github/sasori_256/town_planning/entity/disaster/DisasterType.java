package io.github.sasori_256.town_planning.entity.disaster;

import io.github.sasori_256.town_planning.entity.model.CategoryType;

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

  /**
   * 天災の種類を初期化
   *
   * @param displayName 表示名
   * @param cost        発生コスト
   * @param radius      影響範囲の半径
   * @param damage      与えるダメージ量
   * @param category    カテゴリ
   */
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
