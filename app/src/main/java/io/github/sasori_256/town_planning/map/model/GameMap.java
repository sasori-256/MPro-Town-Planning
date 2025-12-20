package io.github.sasori_256.town_planning.map.model;

import java.awt.geom.Point2D;

import io.github.sasori_256.town_planning.common.event.EventBus;
import io.github.sasori_256.town_planning.common.event.events.MapUpdatedEvent;
import io.github.sasori_256.town_planning.entity.building.Building;
import io.github.sasori_256.town_planning.entity.building.BuildingType;

public class GameMap implements MapContext {
  private final int width;
  private final int height;
  private final MapCell[][] cells;
  private final EventBus eventBus;

  /**
   * @param width
   * @param height
   * @param eventBus
   */
  public GameMap(int width, int height, EventBus eventBus) {
    this.width = width;
    this.height = height;
    this.eventBus = eventBus;
    this.cells = new MapCell[height][width];

    // Cellsの初期化
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        cells[y][x] = new MapCell(new Point2D.Double(x, y), TerrainType.GRASS);
      }
    }
  }

  @Override
  public boolean isValidPosition(Point2D.Double pos) {
    return pos.getX() >= 0 && pos.getX() < this.width
        && pos.getY() >= 0 && pos.getY() < this.height;
  }

  @Override
  public MapCell getCell(Point2D.Double pos) {
    if (!isValidPosition(pos)) {
      // 無効な位置へのアクセスは例外を投げる
      // またはnullを返す、境界用のダミーセルを返すなどの方法も考えられる
      throw new IndexOutOfBoundsException("Invalid position: " + pos);
    }
    return cells[(int) pos.getY()][(int) pos.getX()];
  }

  @Override
  public boolean placeBuilding(Point2D.Double pos, Building building) {
    if (!isValidPosition(pos)) {
      return false;
    }
    MapCell cell = getCell(pos);
    if (cell.canBuild()) {
      cell.setBuilding(building);
      eventBus.publish(new MapUpdatedEvent(pos));
      return true;
    }
    return false;
  }

  @Override
  public boolean removeBuilding(Point2D.Double pos) {
    if (!isValidPosition(pos)) {
      return false;
    }
    MapCell cell = getCell(pos);
    if (cell.getBuilding().getType() != BuildingType.NONE) {
      cell.removeBuilding();
      eventBus.publish(new MapUpdatedEvent(pos)); // 修正箇所
      return true;
    }
    return false;
  }

  @Override
  public int getWidth() {
    return width;
  }

  @Override
  public int getHeight() {
    return height;
  }
}
