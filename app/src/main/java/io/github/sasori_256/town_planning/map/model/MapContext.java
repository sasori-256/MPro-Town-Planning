package io.github.sasori_256.town_planning.map.model;

import java.awt.geom.Point2D;

import io.github.sasori_256.town_planning.gameObject.building.BuildingObject;

public interface MapContext {
  public boolean isValidPos(Point2D.Double pos);

  public MapCell getCell(Point2D.Double pos);

  public boolean placeBuilding(Point2D.Double pos, BuildingObject building);

  public boolean removeBuilding(Point2D.Double pos);

  public int getWidth();

  public int getHeight();
}
