package io.github.sasori_256.town_planning;

import org.junit.jupiter.api.Test;

import io.github.sasori_256.town_planning.gameObject.Camera;
import io.github.sasori_256.town_planning.map.controller.GameMapController;
import io.github.sasori_256.town_planning.map.controller.handler.PlaceBuildingHandler;

import static org.junit.jupiter.api.Assertions.*;

final class mEvent extends java.awt.event.MouseEvent {
    public mEvent(int x, int y) {
        super(new java.awt.Component(){}, 0, 0L, 0, x, y, 1, false);
    }
}

public class ScreenToIsoTest {
    @Test void testScreenToIsoConversion() {
        Camera camera = new Camera(64,32);
        GameMapController gameMapController = new GameMapController(camera);
        gameMapController.mouseClicked(new mEvent(64, 32));

        // // Then the isometric coordinates should be as expected
        // assertEquals(200, isoX, "Iso X coordinate should be correct");
        // assertEquals(0, isoY, "Iso Y coordinate should be correct");
    }
}
