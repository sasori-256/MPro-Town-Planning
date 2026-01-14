package io.github.sasori_256.town_planning.entity.disaster;

import java.awt.geom.Point2D;
import java.util.function.BiFunction;

import io.github.sasori_256.town_planning.entity.disaster.strategy.MeteorDisasterAction;
import io.github.sasori_256.town_planning.entity.model.CategoryType;
import io.github.sasori_256.town_planning.entity.model.GameAction;

/**
 * 天災の種類定義。
 */
public enum DisasterType {
  METEOR("隕石", "loading", 200, 3, 100, CategoryType.METEOR, (pos, type) -> new MeteorDisasterAction(pos, type)),
  PLAGUE("疫病", "plague", 150, 5, 20, CategoryType.PLAGUE);

  private final String displayName;
  private final String imageName;
  private final int cost;
  private final int radius;
  private final int damage;
  private final CategoryType category;
  private final BiFunction<Point2D.Double, DisasterType, GameAction> actionFactory;

  /**
   * 天災の種類を初期化
   *
   * @param displayName   表示名
   * @param imageName     表示用画像の名前
   * @param cost          発生コスト
   * @param radius        影響範囲の半径
   * @param damage        与えるダメージ量
   * @param category      カテゴリ
   * @param actionFactory 発生位置に応じてActionを生成する関数
   */
  DisasterType(String displayName, String imageName, int cost, int radius, int damage,
      CategoryType category, BiFunction<Point2D.Double, DisasterType, GameAction> actionFactory) {
    this.displayName = displayName;
    this.imageName = imageName;
    this.cost = cost;
    this.radius = radius;
    this.damage = damage;
    this.category = category;
    this.actionFactory = actionFactory;
  }

  DisasterType(String displayName, String imageName, int cost, int radius, int damage, CategoryType category) {
    this(displayName, imageName, cost, radius, damage, category, null);
  }

  /**
   * 表示名を返す。
   *
   * @return 表示名
   */
  public String getDisplayName() {
    return displayName;
  }

  /**
   * 画像名を返す。
   *
   * @return 画像名
   */
  public String getImageName() {
    return imageName;
  }

  /**
   * 発生コストを返す。
   *
   * @return コスト
   */
  public int getCost() {
    return cost;
  }

  /**
   * 影響範囲の半径を返す。
   *
   * @return 半径
   */
  public int getRadius() {
    return radius;
  }

  /**
   * 与えるダメージ量を返す。
   *
   * @return ダメージ量
   */
  public int getDamage() {
    return damage;
  }

  /**
   * カテゴリを返す。
   *
   * @return カテゴリ
   */
  public CategoryType getCategory() {
    return category;
  }

  /**
   * 目標位置に応じたアクションを生成する。
   *
   * @param targetPos 目標位置
   * @return 生成したアクション。未設定ならnull
   */
  public GameAction createAction(Point2D.Double targetPos) {
    return actionFactory == null ? null : actionFactory.apply(targetPos, this);
  }
}
