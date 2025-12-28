package io.github.sasori_256.town_planning.entity.disaster;

import java.awt.geom.Point2D;
import java.util.function.BiFunction;

import io.github.sasori_256.town_planning.entity.disaster.strategy.MeteorDisasterAction;
import io.github.sasori_256.town_planning.entity.model.GameAction;

/**
 * 天災の種類定義。
 */
public enum DisasterType {
  METEOR("隕石", "meteor", 200, 3, 100, (pos, type) -> new MeteorDisasterAction(pos, type)),
  PLAGUE("疫病", "plague", 150, 5, 20, (pos, type) -> null);

  private final String displayName;
  private final String imageName;
  private final int cost;
  private final int radius;
  private final int damage;

  private final BiFunction<Point2D.Double, DisasterType, GameAction> actionFactory;

  /**
   * 天災の種類を初期化
   *
   * @param displayName 表示名
   * @param imageName   表示用画像の名前
   * @param cost        発生コスト
   * @param radius      影響範囲の半径
   * @param damage      与えるダメージ量
   * @param actionFactory 発生位置に応じてActionを生成する関数
   */
  DisasterType(String displayName, String imageName, int cost, int radius, int damage,
      BiFunction<Point2D.Double, DisasterType, GameAction> actionFactory) {
    this.displayName = displayName;
    this.imageName = imageName;
    this.cost = cost;
    this.radius = radius;
    this.damage = damage;
    this.actionFactory = actionFactory;
  }

  DisasterType(String displayName, String imageName, int cost, int radius, int damage) {
    this(displayName, imageName, cost, radius, damage, null);
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

  public GameAction createAction(Point2D.Double targetPos) {
    return actionFactory == null ? null : actionFactory.apply(targetPos, this);
  }
}
