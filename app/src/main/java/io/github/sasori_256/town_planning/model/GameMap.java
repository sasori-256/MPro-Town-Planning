package io.github.sasori_256.town_planning.model;

import io.github.sasori_256.town_planning.event.*;
import java.awt.geom.Point2D;

public class GameMap {
  private final int width;
  private final int height;
  private final MapCell[][] cells;
  private final EventBus eventBus;

  public GameMap(int width, int height, EventBus eventBus) {
    this.width = width;
    this.height = height;
    this.eventBus = eventBus;
    this.cells = new MapCell[height][width];

    // Cellsの初期化
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        cells[y][x] = new MapCell(TerrainType.GRASS);
      }
    }
  }

  public boolean isValid(Point2D pos) {
    return pos.getX() >= 0 && pos.getX() < width
        && pos.getY() >= 0 && pos.getY() < height;
  }

  public MapCell getCell(Point2D pos) {
    if (!isValid(pos)) {
      // 無効な位置へのアクセスは例外を投げる
      // またはnullを返す、境界用のダミーセルを返すなどの方法も考えられる
      throw new IndexOutOfBoundsException("Invalid position: " + pos);
    }
    return cells[(int) pos.getY()][(int) pos.getX()];
  }

  public boolean placeBuilding(Point2D pos, GameEntity building) {
    if (!isValid(pos)) {
      return false;
    }
    MapCell cell = getCell(pos);
    if (cell.canBuild()) {
      cell.setBuilding(building);
      eventBus.publish(EventType.MAP_UPDATED, pos);
      return true;
    }
    return false;
  }

  public boolean removeBuilding(Point2D pos) {
    if (!isValid(pos)) {
      return false;
    }
    MapCell cell = getCell(pos);
    if (cell.getBuilding().isPresent()) {
      cell.removeBuilding();
      eventBus.publish(EventType.MAP_UPDATED, pos);
      return true;
    }
    return false;
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }
}
