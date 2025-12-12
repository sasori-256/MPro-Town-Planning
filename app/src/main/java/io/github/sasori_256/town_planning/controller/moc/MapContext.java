package io.github.sasori_256.town_planning.controller.moc;

import java.awt.geom.Point2D;

public interface MapContext {

    void placeBuilding(Point2D.Double isoPoint, BuildingGameObject selectedBuilding);

}
