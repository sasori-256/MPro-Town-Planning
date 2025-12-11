package io.github.sasori_256.town_planning.controller;

import java.awt.geom.Point2D;

import io.github.sasori_256.town_planning.controller.moc.BuildingGameObject;

public interface MapContext {

    void placeBuilding(Point2D.Double isoPoint, BuildingGameObject selectedBuilding);

}
