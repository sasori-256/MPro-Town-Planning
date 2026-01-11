package io.github.sasori_256.town_planning.entity.disaster;

import java.awt.geom.Point2D;

import io.github.sasori_256.town_planning.common.core.strategy.CompositeUpdateStrategy;
import io.github.sasori_256.town_planning.entity.model.BaseGameEntity;
import io.github.sasori_256.town_planning.entity.model.GameAction;

/**
 * Represents a disaster entity in the game world.
 * <p>
 * A {@code Disaster} is a {@link BaseGameEntity} placed at a specific position
 * on the map. Its concrete behavior and effects are defined by the associated
 * {@link DisasterType}, which is responsible for creating the
 * {@link GameAction}
 * that drives the disaster's update logic via a
 * {@link CompositeUpdateStrategy}.
 * </p>
 */
public class Disaster extends BaseGameEntity {
  private final DisasterType type;
  private String animationName;
  private int animationFrameRate;
  private boolean animationLoop;
  private double animationElapsedSeconds;

  /**
   * Creates a new disaster entity at the specified position.
   * <p>
   * Initializes the disaster by creating a {@link CompositeUpdateStrategy} and
   * populating it with a {@link GameAction} obtained from the disaster type's
   * action factory. The {@code position} parameter is passed to the action
   * factory
   * to create position-specific disaster behavior.
   * </p>
   *
   * @param position     the position where the disaster occurs; used to create
   *                     the disaster's action via the type's action factory
   * @param disasterType the type defining the disaster's characteristics and
   *                     behavior
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
