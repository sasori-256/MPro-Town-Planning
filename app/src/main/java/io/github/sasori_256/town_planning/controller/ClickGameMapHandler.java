package io.github.sasori_256.town_planning.controller;

import java.awt.geom.Point2D;
import java.util.function.Consumer;

public class ClickGameMapHandler implements Consumer<Point2D.Double>{
    @Override
    public void accept(Point2D.Double isoPoint) {
        //TODO
        System.out.println("Clicked at Iso Coordinates: (" + isoPoint.x + ", " + isoPoint.y + ")");
    }

}
