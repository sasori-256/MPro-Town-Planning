package io.github.sasori_256.town_planning.model.entity;

import io.github.sasori_256.town_planning.model.GameObject;
import io.github.sasori_256.town_planning.model.ResidentType;
import java.awt.geom.Point2D;

public class ResidentObject extends GameObject {
  private final ResidentType type;
  private double age;
  private int faith;
  private int layerIndex;
  private String state;

  public ResidentObject(Point2D position, ResidentType residentType) {
    super(position);
    this.type = residentType;
    this.age = 0.0;
    this.faith = residentType.getInitialFaith();
    this.layerIndex = 0;
    this.state = "Idle";
  }

  private void addAge(double years) {
    this.age = Math.min(this.age + years, this.type.getMaxAge());
  }

  public void setFaith(int faith) {
    this.faith = Math.max(0, faith);
  }

  public void setLayerIndex(int layerIndex) {
    this.layerIndex = layerIndex;
  }

  public void setState(String state) {
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

  public String getState() {
    return this.state;
  }
}
