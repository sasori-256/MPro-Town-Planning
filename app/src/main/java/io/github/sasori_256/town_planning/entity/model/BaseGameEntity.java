package io.github.sasori_256.town_planning.entity.model;

import java.awt.geom.Point2D;

import io.github.sasori_256.town_planning.common.core.Updatable;
import io.github.sasori_256.town_planning.common.core.strategy.UpdateStrategy;

/**
 * ゲーム内の全ての動的オブジェクトのインターフェースのデフォルト実装を提供するクラス。
 * Strategyパターンを用いて振る舞いを定義する。
 * 継承による拡張ではなく、コンポジション（Strategy）による機能追加を推奨する。
 */
public abstract class BaseGameEntity implements GameEntity, Updatable {
  protected int layerIndex = 0;
  protected Point2D.Double position;

  // Layer Indices
  public static final int LAYER_BACKGROUND = 0; // 背景層
  public static final int LAYER_GROUND = 5; // 地面層
  public static final int LAYER_DEFAULT = 10; // 通常のオブジェクト層
  public static final int LAYER_AID = 20; // 空中オブジェクト層
  public static final int LAYER_UI = 100; // UI層

  // Strategies
  private UpdateStrategy updateStrategy;

  public BaseGameEntity(Point2D.Double position) {
    this.layerIndex = LAYER_DEFAULT;
    this.position = position;
    // Default strategies (No-op)
    this.updateStrategy = (ctx, self) -> {
    };
  }

  @Override
  public int getLayerIndex() {
    return this.layerIndex;
  }

  @Override
  public void setLayerIndex(int layerIndex) {
    this.layerIndex = layerIndex;
  }

  @Override
  public Point2D.Double getPosition() {
    return position;
  }

  @Override
  public void setPosition(Point2D.Double position) {
    this.position = position;
  }

  @Override
  public void setUpdateStrategy(UpdateStrategy updateStrategy) {
    this.updateStrategy = updateStrategy;
  }

  @Override
  public void onRemoved() {
    // デフォルトでは何もしない (必要に応じてオーバーライド)
  }

  @Override
  public void update(GameContext context) {
    if (updateStrategy != null) {
      updateStrategy.update(context, this);
    }
  }
}
