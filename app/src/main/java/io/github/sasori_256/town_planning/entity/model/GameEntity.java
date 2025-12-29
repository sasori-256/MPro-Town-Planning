package io.github.sasori_256.town_planning.entity.model;

import java.awt.geom.Point2D;

import io.github.sasori_256.town_planning.common.core.strategy.UpdateStrategy;

/**
 * ゲームのエンティティを表すインターフェース。
 * 各エンティティは位置情報を持つ。
 */
public interface GameEntity {
  /**
   * エンティティの描画レイヤーのインデックスを取得する。
   * レイヤーインデックスは、エンティティの描画順序を決定するために使用される。
   *
   * @return エンティティの描画レイヤーのインデックス
   */
  int getLayerIndex();

  /**
   * エンティティの描画レイヤーのインデックスを設定する。
   * レイヤーインデックスは、エンティティの描画順序を決定するために使用される。
   *
   * @param layerIndex エンティティの新しい描画レイヤーのインデックス
   */
  void setLayerIndex(int layerIndex);

  /**
   * エンティティの位置を取得する。
   * 位置は2D座標で表される。
   * 
   * @return エンティティの位置
   */
  Point2D.Double getPosition();

  /**
   * エンティティの位置を設定する。
   * 位置は2D座標で表される。
   * 
   * @param position エンティティの新しい位置
   */
  void setPosition(Point2D.Double position);

  /**
   * エンティティの更新Strategyを設定する。
   * 更新Strategyは、エンティティの状態を毎フレーム更新するためのロジックを定義する。
   * 
   * @param updateStrategy エンティティに設定する更新Strategy
   */
  void setUpdateStrategy(UpdateStrategy updateStrategy);

  /**
   * エンティティが削除される際に呼び出されるライフサイクルメソッド。
   * リソースの解放や終了処理を行う。
   */
  void onRemoved();
}
