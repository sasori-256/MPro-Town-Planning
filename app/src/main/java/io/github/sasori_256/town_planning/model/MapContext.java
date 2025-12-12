package io.github.sasori_256.town_planning.model;

import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;

public interface MapContext {
    public boolean isValid(Point2D.Double pos);
    public MapCell getCell(Point2D.Double pos);
    public boolean placeBuilding(Point2D.Double pos, GameObject building);
    public boolean removeBuilding(Point2D.Double pos);
    public int getWidth() ;
    public int getHeight() ;
}
