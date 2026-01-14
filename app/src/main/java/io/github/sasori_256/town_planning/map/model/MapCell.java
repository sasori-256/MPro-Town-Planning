package io.github.sasori_256.town_planning.map.model;

import java.awt.geom.Point2D;

import org.jspecify.annotations.Nullable;

import io.github.sasori_256.town_planning.entity.building.Building;

/**
 * マップ上の1セルの状態を保持するクラス。
 */
public class MapCell {
  private final Point2D.Double position;
  private Terrain terrain;

  @Nullable
  private Building building;

  private int localX;
  private int localY;

  /**
   * セルを生成する。
   *
   * @param position    セル座標
   * @param initTerrain 初期地形
   */
  public MapCell(Point2D.Double position, Terrain initTerrain) {
    this.position = position;
    this.terrain = initTerrain;
    // Buildingはnull許容に変更してResident通過判定や描画時に余計なメンバーにアクセス不可に
    this.building = null;
    this.localX = 0;
    this.localY = 0;
  }

  /**
   * セル座標を返す。
   *
   * @return セル座標
   */
  public Point2D.Double getPosition() {
    return position;
  }

  /**
   * 地形を返す。
   *
   * @return 地形
   */
  public Terrain getTerrain() {
    return terrain;
  }

  /**
   * 地形を更新する。
   *
   * @param terrain 設定する地形
   * @return 更新できた場合はtrue
   */
  public boolean setTerrain(Terrain terrain) {
    this.terrain = terrain;
    return true;
  }

  /**
   * 建物を返す。
   *
   * @return 建物。未配置ならnull
   */
  public Building getBuilding() {
    return building;
  }

  /**
   * 建物内のローカルX座標を返す。
   *
   * @return ローカルX
   */
  public int getLocalX() {
    return localX;
  }

  /**
   * 建物内のローカルY座標を返す。
   *
   * @return ローカルY
   */
  public int getLocalY() {
    return localY;
  }

  /**
   * 建物で占有されているかを返す。
   *
   * @return 建物がある場合はtrue
   */
  public boolean isOccupied() {
    return building != null;
  }

  /**
   * 建物とローカル座標を設定する。
   *
   * @param building 建物
   * @param localX   建物内のローカルX
   * @param localY   建物内のローカルY
   */
  public void setBuilding(Building building, int localX, int localY) {
    this.building = building;
    this.localX = localX;
    this.localY = localY;
  }

  /**
   * 建物情報をクリアする。
   */
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

  /**
   * 移動コストを返す。
   *
   * @return 移動コスト
   */
  public int getMoveCost() {
    if (building == null) {
      return terrain.getMoveCost();
    }
    return building.getType().getMoveCost(localX, localY);
  }
}
