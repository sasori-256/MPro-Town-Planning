package io.github.sasori_256.town_planning.entity.resident;

import java.awt.geom.Point2D;

import io.github.sasori_256.town_planning.common.core.strategy.CompositeUpdateStrategy;
import io.github.sasori_256.town_planning.entity.model.BaseGameEntity;
import io.github.sasori_256.town_planning.entity.resident.strategy.ResidentBehaviorAction;
import io.github.sasori_256.town_planning.entity.resident.strategy.ResidentLifeCycleEffect;

/**
 * 住民エンティティを表すクラス。
 */
public class Resident extends BaseGameEntity {
  private static final double DEATH_ANIMATION_DURATION = 0.4;
  private final ResidentType type;
  private Point2D.Double homePosition;
  private Point2D.Double relocationTarget;
  private double age;
  private int faith;
  private int layerIndex;
  private ResidentState state;
  private double deathAnimationElapsed;

  /**
   * 住民を生成する。
   *
   * @param position     現在位置
   * @param residentType 種別
   * @param state        初期状態
   * @param homePosition 自宅の座標
   */
  public Resident(Point2D.Double position, ResidentType residentType, ResidentState state,
      Point2D.Double homePosition) {
    super(position);
    this.type = residentType;
    this.homePosition = new Point2D.Double(homePosition.getX(), homePosition.getY());
    this.age = 0.0;
    this.faith = residentType.getInitialFaith();
    this.layerIndex = 0;
    this.state = state;
    this.deathAnimationElapsed = 0.0;

    CompositeUpdateStrategy strategy = new CompositeUpdateStrategy();
    strategy.setAction(new ResidentBehaviorAction());
    strategy.addEffect(new ResidentLifeCycleEffect());
    this.setUpdateStrategy(strategy);
  }

  /**
   * 住民を生成する。
   *
   * @param position     現在位置
   * @param residentType 種別
   * @param state        初期状態
   * @implNote Strategy初期化は4引数コンストラクタに委譲する。
   */
  public Resident(Point2D.Double position, ResidentType residentType, ResidentState state) {
    this(position, residentType, state, position);
  }

  /**
   * 年齢を設定する。
   *
   * @param age 年齢
   */
  public void setAge(double age) {
    this.age = Math.max(0.0, age);
  }

  /**
   * 信仰度を設定する。
   *
   * @param faith 信仰度
   */
  public void setFaith(int faith) {
    this.faith = Math.max(0, faith);
  }

  /** {@inheritDoc} */
  @Override
  public void setLayerIndex(int layerIndex) {
    this.layerIndex = layerIndex;
  }

  /**
   * 状態を設定する。
   *
   * @param state 住民状態
   */
  public void setState(ResidentState state) {
    if (state == ResidentState.DEAD && this.state != ResidentState.DEAD) {
      this.deathAnimationElapsed = 0.0;
    }
    this.state = state;
  }

  /**
   * 死亡状態にする。
   */
  public void markDead() {
    setState(ResidentState.DEAD);
  }

  /**
   * 住民種別を返す。
   *
   * @return 住民種別
   */
  public ResidentType getType() {
    return this.type;
  }

  /**
   * 自宅の座標を返す。
   */
  public Point2D.Double getHomePosition() {
    return new Point2D.Double(homePosition.getX(), homePosition.getY());
  }

  /**
   * 自宅の座標を更新する。
   *
   * @param homePosition 自宅の座標
   */
  public void setHomePosition(Point2D.Double homePosition) {
    this.homePosition = new Point2D.Double(homePosition.getX(), homePosition.getY());
  }

  /**
   * 引っ越し先が設定されているかを返す。
   */
  public boolean hasRelocationTarget() {
    return relocationTarget != null;
  }

  /**
   * 引っ越し先の座標を返す。
   *
   * @return 引っ越し先の座標。設定がない場合はnull。
   */
  public Point2D.Double getRelocationTarget() {
    if (relocationTarget == null) {
      return null;
    }
    return new Point2D.Double(relocationTarget.getX(), relocationTarget.getY());
  }

  /**
   * 引っ越し先を設定する。
   *
   * @param target 引っ越し先の座標
   */
  public void requestRelocation(Point2D.Double target) {
    this.relocationTarget = new Point2D.Double(target.getX(), target.getY());
  }

  /**
   * 引っ越し先の設定を解除する。
   */
  public void clearRelocationTarget() {
    this.relocationTarget = null;
  }

  /**
   * 年齢を返す。
   *
   * @return 年齢
   */
  public double getAge() {
    return this.age;
  }

  /**
   * 最大年齢を返す。
   *
   * @return 最大年齢
   */
  public double getMaxAge() {
    return this.type.getMaxAge();
  }

  /**
   * 信仰度を返す。
   *
   * @return 信仰度
   */
  public int getFaith() {
    return this.faith;
  }

  /** {@inheritDoc} */
  @Override
  public int getLayerIndex() {
    return this.layerIndex;
  }

  /**
   * 現在の状態を返す。
   *
   * @return 住民状態
   */
  public ResidentState getState() {
    return this.state;
  }

  /**
   * 死亡アニメーションの進行度を返す。
   *
   * @return 0.0-1.0 の範囲
   */
  public double getDeathAnimationProgress() {
    if (state != ResidentState.DEAD) {
      return 0.0;
    }
    if (DEATH_ANIMATION_DURATION <= 0.0) {
      return 1.0;
    }
    return Math.min(1.0, deathAnimationElapsed / DEATH_ANIMATION_DURATION);
  }

  /** {@inheritDoc} */
  @Override
  public void advanceAnimation(double dt) {
    if (state != ResidentState.DEAD) {
      return;
    }
    if (deathAnimationElapsed >= DEATH_ANIMATION_DURATION) {
      return;
    }
    if (dt <= 0.0) {
      return;
    }
    deathAnimationElapsed = Math.min(DEATH_ANIMATION_DURATION, deathAnimationElapsed + dt);
  }
}
