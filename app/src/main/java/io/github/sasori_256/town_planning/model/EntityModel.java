package io.github.sasori_256.town_planning.model;

import java.awt.geom.Point2D;

/**
 * エンティティの基本モデルを表す抽象クラス。
 * すべてのエンティティは位置情報を持ち、描画および再描画のメソッドを提供する。
 * Interfaceの方に定義を置くべきか検討中。
 * TODO: 将来的に必要に応じてサブクラス化するか、インターフェースに統合する。
 */
public abstract class EntityModel {
  public Point2D worldPos;

  public abstract boolean draw();

  public abstract boolean redraw();
}
