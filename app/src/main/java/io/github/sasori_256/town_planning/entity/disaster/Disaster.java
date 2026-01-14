package io.github.sasori_256.town_planning.entity.disaster;

import java.awt.geom.Point2D;

import io.github.sasori_256.town_planning.common.core.strategy.CompositeUpdateStrategy;
import io.github.sasori_256.town_planning.entity.model.BaseGameEntity;
import io.github.sasori_256.town_planning.entity.model.GameAction;

/**
 * 災害エンティティを表すクラス。
 *
 * <p>
 * {@code Disaster} はマップ上の特定位置に配置される {@link BaseGameEntity} で、
 * 具体的な挙動は {@link DisasterType} が生成する {@link GameAction} によって決まる。
 * アクションは {@link CompositeUpdateStrategy} に設定され、更新処理で実行される。
 * </p>
 */
public class Disaster extends BaseGameEntity {
  private final DisasterType type;
  private String animationName;
  private int animationFrameRate;
  private boolean animationLoop;
  private double animationElapsedSeconds;

  /**
   * 指定位置に災害エンティティを生成する。
   *
   * <p>
   * {@link CompositeUpdateStrategy} を生成し、災害種別のアクションファクトリから
   * 得た {@link GameAction} を設定する。位置情報はアクション生成に渡される。
   * </p>
   *
   * @param position     発生位置
   * @param disasterType 災害種別
   */
  public Disaster(Point2D.Double position, DisasterType disasterType) {
    super(position);
    this.type = disasterType;
    this.animationName = disasterType.getImageName();
    this.animationFrameRate = 6;
    this.animationLoop = true;
    this.animationElapsedSeconds = 0.0;

    CompositeUpdateStrategy strategy = new CompositeUpdateStrategy();

    GameAction action = disasterType.createAction(position);
    if (action != null) {
      strategy.setAction(action);
    }

    this.setUpdateStrategy(strategy);
  }

  /**
   * 災害種別を返す。
   *
   * @return 災害種別
   */
  public DisasterType getType() {
    return this.type;
  }

  /**
   * アニメーション名を返す。
   */
  public String getAnimationName() {
    return animationName;
  }

  /**
   * アニメーションのフレーム番号を返す。
   */
  public int getAnimationFrameIndex() {
    if (animationFrameRate <= 0) {
      return 0;
    }
    return (int) Math.floor(animationElapsedSeconds * animationFrameRate);
  }

  /**
   * アニメーションがループするかを返す。
   */
  public boolean isAnimationLoop() {
    return animationLoop;
  }

  /**
   * アニメーション設定を更新する。
   *
   * @param name      アニメーション名
   * @param frameRate フレームレート
   * @param loop      ループ再生するか
   * @param reset     進行状態をリセットするか
   */
  public void setAnimation(String name, int frameRate, boolean loop, boolean reset) {
    this.animationName = name;
    this.animationFrameRate = Math.max(0, frameRate);
    this.animationLoop = loop;
    if (reset) {
      this.animationElapsedSeconds = 0.0;
    }
  }

  /** {@inheritDoc} */
  @Override
  public void advanceAnimation(double dt) {
    if (animationName == null || animationFrameRate <= 0) {
      return;
    }
    if (dt <= 0) {
      return;
    }
    animationElapsedSeconds += dt;
  }
}
