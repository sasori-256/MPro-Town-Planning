package io.github.sasori_256.town_planning.entity.model;

import java.awt.geom.Point2D;

import io.github.sasori_256.town_planning.common.core.Animatable;
import io.github.sasori_256.town_planning.common.core.strategy.UpdateStrategy;

/**
 * ゲーム内の全ての動的オブジェクトのインターフェースのデフォルト実装を提供するクラス。
 * Strategyパターンを用いて振る舞いを定義する。
 * 継承による拡張ではなく、コンポジション（Strategy）による機能追加を推奨する。
 */
public abstract class BaseGameEntity implements GameEntity, Animatable, LifecycleAware {
  protected int layerIndex = 0;
  protected Point2D.Double position;

  // Layer Indices
  /** 背景層。 */
  public static final int LAYER_BACKGROUND = 0;
  /** 地面層。 */
  public static final int LAYER_GROUND = 5;
  /** 通常のオブジェクト層。 */
  public static final int LAYER_DEFAULT = 10;
  /** 空中オブジェクト層。 */
  public static final int LAYER_AID = 20;
  /** UI層。 */
  public static final int LAYER_UI = 100;

  // Strategies
  private UpdateStrategy updateStrategy;

  /**
   * 位置を指定してエンティティを生成する。
   *
   * @param position 初期位置
   */
  public BaseGameEntity(Point2D.Double position) {
    this.layerIndex = LAYER_DEFAULT;
    this.position = position;
    // Default strategies (No-op)
    this.updateStrategy = (ctx, self) -> {
    };
  }

  /** {@inheritDoc} */
  @Override
  public int getLayerIndex() {
    return this.layerIndex;
  }

  /** {@inheritDoc} */
  @Override
  public void setLayerIndex(int layerIndex) {
    this.layerIndex = layerIndex;
  }

  /** {@inheritDoc} */
  @Override
  public Point2D.Double getPosition() {
    return position;
  }

  /** {@inheritDoc} */
  @Override
  public void setPosition(Point2D.Double position) {
    this.position = position;
  }

  /** {@inheritDoc} */
  @Override
  public void setUpdateStrategy(UpdateStrategy updateStrategy) {
    this.updateStrategy = updateStrategy;
  }

  /** {@inheritDoc} */
  @Override
  public void onSpawn(GameContext context) {
    // デフォルトでは何もしない (必要に応じてオーバーライド)
  }

  /** {@inheritDoc} */
  @Override
  public void onRemove(GameContext context) {
    // デフォルトでは何もしない (必要に応じてオーバーライド)
  }

  /**
   * エンティティの更新処理を行う。
   *
   * @param context ゲームコンテキスト
   */
  public void update(GameContext context) {
    if (updateStrategy != null) {
      updateStrategy.update(context, this);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void advanceAnimation(double dt) {
    // No-op by default
  }
}
