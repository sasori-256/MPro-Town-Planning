package io.github.sasori_256.town_planning.map.model;

/**
 * 地形の属性を表すインターフェース。
 */
public interface Terrain {
  /**
   * 表示名を返す。
   *
   * @return 表示名
   */
  String getDisplayName();

  /**
   * 住民が歩行可能かを返す。
   *
   * @return 歩行可能ならtrue
   */
  boolean isWalkable();

  /**
   * 建築可能かを返す。
   *
   * @return 建築可能ならtrue
   */
  boolean isBuildable();

  /**
   * 移動コストを返す。
   *
   * @return 移動コスト
   */
  int getMoveCost();

  /**
   * 地形の描画処理を行う。
   */
  void draw();
}
