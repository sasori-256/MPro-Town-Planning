package io.github.sasori_256.town_planning.entity.resident;

import java.awt.geom.Point2D;

import io.github.sasori_256.town_planning.common.core.strategy.CompositeUpdateStrategy;
import io.github.sasori_256.town_planning.entity.model.BaseGameEntity;
import io.github.sasori_256.town_planning.entity.resident.strategy.ResidentBehaviorAction;
import io.github.sasori_256.town_planning.entity.resident.strategy.ResidentLifeCycleStrategy;

public class Resident extends BaseGameEntity {
  private final ResidentType type;
  private Point2D.Double homePosition;
  private Point2D.Double relocationTarget;
  private double age;
  private int faith;
  private int layerIndex;
  private ResidentState state;

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

    CompositeUpdateStrategy strategy = new CompositeUpdateStrategy();
    strategy.setAction(new ResidentBehaviorAction());
    strategy.addEffect(new ResidentLifeCycleStrategy());
    this.setUpdateStrategy(strategy);
  }

  /**
   * 住民を生成する。
   *
   * @param position     現在位置
   * @param residentType 種別
   * @param state        初期状態
   */
  public Resident(Point2D.Double position, ResidentType residentType, ResidentState state) {
    this(position, residentType, state, position);
  }

  public void setAge(double age) {
    this.age = Math.max(0.0, age);
  }

  public void setFaith(int faith) {
    this.faith = Math.max(0, faith);
  }

  @Override
  public void setLayerIndex(int layerIndex) {
    this.layerIndex = layerIndex;
  }

  public void setState(ResidentState state) {
    this.state = state;
  }

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

  public double getAge() {
    return this.age;
  }

  public double getMaxAge() {
    return this.type.getMaxAge();
  }

  public int getFaith() {
    return this.faith;
  }

  @Override
  public int getLayerIndex() {
    return this.layerIndex;
  }

  public ResidentState getState() {
    return this.state;
  }
}
