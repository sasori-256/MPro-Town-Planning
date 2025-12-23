package io.github.sasori_256.town_planning.entity.disaster;

/**
 * 災害の種類定義。
 */
public enum DisasterType {
  METEOR("隕石", 200, 3, 100),
  PLAGUE("疫病", 150, 5, 20);

  private final String displayName;
  private final int cost;
  private final int radius;
  private final int damage;

  /**
   * 天災の種類を初期化
   *
   * @param displayName 表示名
   * @param cost        発生コスト
   * @param radius      影響範囲の半径
   * @param damage      与えるダメージ量
   */
  DisasterType(String displayName, int cost, int radius, int damage) {
    this.displayName = displayName;
    this.cost = cost;
    this.radius = radius;
    this.damage = damage;
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
}
