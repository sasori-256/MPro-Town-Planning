package io.github.sasori_256.town_planning.map.controller;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.util.function.BiConsumer;
import java.util.function.Function;

import io.github.sasori_256.town_planning.gameobject.Camera;
import io.github.sasori_256.town_planning.gameobject.model.BaseGameEntity;
import io.github.sasori_256.town_planning.map.controller.handler.*;

public class GameMapController implements MouseListener{
    private Camera camera;
    private BiConsumer<Point2D.Double, Function<Point2D.Double, ? extends BaseGameEntity>> actionOnClick;
    private Function<Point2D.Double, ? extends BaseGameEntity> selectedEntity;

    public GameMapController(Camera camera) {
        this.camera = camera;
        this.actionOnClick = new ClickGameMapHandler();
        this.selectedEntity = (point) -> null;
    }

    /**
     * GameMap上でのクリック時の動作を設定する
     * @param action
     */
    public void setActionOnClick(BiConsumer<Point2D.Double, Function<Point2D.Double, ? extends BaseGameEntity>> action) {
        this.actionOnClick = action;
    }

    public void setSelectedEntity(Function<Point2D.Double, ? extends BaseGameEntity> entity) {
        this.selectedEntity = entity;
    }


    @Override
    public void mouseClicked(MouseEvent e) {
        Point2D.Double isoPoint = camera.screenToIso(new Point2D.Double(e.getX(), e.getY()));
        // System.out.println("Iso Coordinates: (" + isoPoint.x + ", " + isoPoint.y + ")");
        actionOnClick.accept(isoPoint, selectedEntity);
    }

    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}

}