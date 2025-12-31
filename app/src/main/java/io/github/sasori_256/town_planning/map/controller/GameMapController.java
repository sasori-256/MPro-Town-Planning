package io.github.sasori_256.town_planning.map.controller;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.util.function.BiConsumer;
import java.util.function.Function;

import javax.swing.SwingUtilities;

import io.github.sasori_256.town_planning.entity.Camera;
import io.github.sasori_256.town_planning.entity.model.BaseGameEntity;
import io.github.sasori_256.town_planning.map.controller.handler.*;

public class GameMapController implements MouseListener, MouseMotionListener, KeyListener, MouseWheelListener{
    private Camera camera;
    private BiConsumer<Point2D.Double, Function<Point2D.Double, ? extends BaseGameEntity>> actionOnClick;
    private Function<Point2D.Double, ? extends BaseGameEntity> selectedEntityGenerator;
    private Point previousMiddleMousePos;

    public GameMapController(Camera camera) {
        this.camera = camera;
        this.actionOnClick = new ClickGameMapHandler();
        this.selectedEntityGenerator = (point) -> null;
    }

    /**
     * GameMap上でのクリック時の動作を設定する
     * @param action
     */
    public void setActionOnClick(BiConsumer<Point2D.Double, Function<Point2D.Double, ? extends BaseGameEntity>> action) {
        this.actionOnClick = action;
    }

    public void setSelectedEntityGenerator(Function<Point2D.Double, ? extends BaseGameEntity> entityGenerator) {
        this.selectedEntityGenerator = entityGenerator;
    }


    @Override
    public void mouseClicked(MouseEvent e) {
        if(SwingUtilities.isLeftMouseButton(e)){
            Point2D.Double isoPoint = camera.screenToIso(new Point2D.Double(e.getX(), e.getY()));
            actionOnClick.accept(isoPoint, selectedEntityGenerator);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if(SwingUtilities.isMiddleMouseButton(e)){
            previousMiddleMousePos = new Point(e.getX(), e.getY());
        }
    }

    @Override 
    public void mouseReleased(MouseEvent e) {
        if(SwingUtilities.isMiddleMouseButton(e)){
            previousMiddleMousePos = null;
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if(SwingUtilities.isMiddleMouseButton(e) && previousMiddleMousePos != null){
            int dx = e.getX() - previousMiddleMousePos.x;
            int dy = e.getY() - previousMiddleMousePos.y;

            camera.pan(dx, dy);
            previousMiddleMousePos = new Point(e.getX(), e.getY());
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        int notches = e.getWheelRotation();
        if (notches < 0) {
            camera.zoomIn();
        } else {
            camera.zoomOut();
        }
    }

    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
    @Override public void mouseMoved(MouseEvent e) {}

    @Override 
    public void keyPressed(KeyEvent e) {
        int k = e.getKeyCode();
        switch (k) {
            case KeyEvent.VK_W:
                camera.moveUp();
                break;
            case KeyEvent.VK_S:
                camera.moveDown();
                break;
            case KeyEvent.VK_A:
                camera.moveLeft();
                break;
            case KeyEvent.VK_D:
                camera.moveRight();
                break;
            default:
                break;
        }
    }
    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}

}