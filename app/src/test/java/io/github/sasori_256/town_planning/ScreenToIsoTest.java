package io.github.sasori_256.town_planning;

import org.junit.jupiter.api.Test;

import io.github.sasori_256.town_planning.entity.Camera;
import io.github.sasori_256.town_planning.map.controller.GameMapController;

import static org.junit.jupiter.api.Assertions.*;

final class mEvent extends java.awt.event.MouseEvent {
  public mEvent(int x, int y) {
    super(new java.awt.Component() {
    }, 0, 0L, 0, x, y, 1, false);
  }
}

public class ScreenToIsoTest {
  @Test
  void testScreenToIsoConversion() {
    Camera camera = new Camera(1, new java.awt.geom.Point2D.Double(0, 0));
    GameMapController gameMapController = new GameMapController(camera, null);
    gameMapController.mouseClicked(new mEvent(64, 32));

    // // Then the isometric coordinates should be as expected
    // assertEquals(200, isoX, "Iso X coordinate should be correct");
    // assertEquals(0, isoY, "Iso Y coordinate should be correct");
  }
}
