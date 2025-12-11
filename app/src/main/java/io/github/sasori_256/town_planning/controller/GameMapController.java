package io.github.sasori_256.town_planning.controller;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.util.function.Consumer;

public class GameMapController implements MouseListener{
    private Camera camera;
    private Consumer<Point2D.Double> actionOnClick;

    public GameMapController(Camera camera) {
        this.camera = camera;
        this.actionOnClick = new ClickGameMapHandler();
    }

    /**
     * GameMap上でのクリック時の動作を設定する
     * @param action
     */
    public void setActionOnClick(Consumer<Point2D.Double> action) {
        this.actionOnClick = action;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        Point2D.Double isoPoint = camera.screenToIso(e.getX(), e.getY());
        // System.out.println("Iso Coordinates: (" + isoPoint.x + ", " + isoPoint.y + ")");
        if (actionOnClick != null) {
            actionOnClick.accept(isoPoint);
        }
    }

    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    
    
}