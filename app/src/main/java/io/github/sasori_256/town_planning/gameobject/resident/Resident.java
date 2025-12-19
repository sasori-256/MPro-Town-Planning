package io.github.sasori_256.town_planning.gameobject.resident;

import java.awt.geom.Point2D;

import io.github.sasori_256.town_planning.gameobject.model.BaseGameEntity;

public class Resident extends BaseGameEntity {
  private final ResidentType type;
  private double age;
  private int faith;
  private int layerIndex;
  private ResidentState state;

  public Resident(Point2D.Double position, ResidentType residentType, ResidentState state) {
    super(position);
    this.type = residentType;
    this.age = 0.0;
    this.faith = residentType.getInitialFaith();
    this.layerIndex = 0;
    this.state = state;
  }

  public void setAge(double age) {
    this.age = Math.max(0.0, age);
  }

  public void setFaith(int faith) {
    this.faith = Math.max(0, faith);
  }

  public void setLayerIndex(int layerIndex) {
    this.layerIndex = layerIndex;
  }

  public void setState(ResidentState state) {
    this.state = state;
  }

  public ResidentType getType() {
    return this.type;
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

  public int getLayerIndex() {
    return this.layerIndex;
  }

  public ResidentState getState() {
    return this.state;
  }
}
