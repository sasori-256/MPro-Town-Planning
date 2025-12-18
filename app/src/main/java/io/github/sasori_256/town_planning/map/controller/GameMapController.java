package io.github.sasori_256.town_planning.map.controller;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.util.function.Consumer;

import io.github.sasori_256.town_planning.gameobject.Camera;
import io.github.sasori_256.town_planning.gameobject.building.Building;
import io.github.sasori_256.town_planning.gameobject.building.BuildingType;
import io.github.sasori_256.town_planning.map.controller.handler.PlaceBuildingHandler;
import io.github.sasori_256.town_planning.map.model.GameMap;

public class GameMapController implements MouseListener {
  private Camera camera;
  private Consumer<Point2D.Double> actionOnClick;

  public GameMapController(Camera camera, GameMap gameMap) {
    this.camera = camera;
    this.actionOnClick = new PlaceBuildingHandler(
        gameMap,
        new Building(new Point2D.Double(0, 0), BuildingType.HOUSE),
        this); // こんな設定法でいいのか？
  }

  /**
   * GameMap上でのクリック時の動作を設定する
   * 
   * @param action
   */
  public void setActionOnClick(Consumer<Point2D.Double> action) {
    this.actionOnClick = action;
  }

  @Override
  public void mouseClicked(MouseEvent e) {
    Point2D.Double isoPoint = camera.screenToIso(new Point2D.Double(e.getX(), e.getY()));
    // System.out.println("Iso Coordinates: (" + isoPoint.x + ", " + isoPoint.y +
    // ")");
    actionOnClick.accept(isoPoint);
  }

  @Override
  public void mouseEntered(MouseEvent e) {
  }

  @Override
  public void mouseExited(MouseEvent e) {
  }

  @Override
  public void mousePressed(MouseEvent e) {
  }

  @Override
  public void mouseReleased(MouseEvent e) {
  }

}
