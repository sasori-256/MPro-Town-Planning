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
    double x = pos.getX(); // 中心基準
    double y = pos.getY();
    return x >= -0.5 && x < this.width - 0.5
        && y >= -0.5 && y < this.height - 0.5;
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
    if (building == null) {
      return false;
    }
    // Validate the original continuous position before snapping to the grid.
    if (!isValidPosition(pos)) {
      return false;
    }
    int anchorX = (int) Math.round(pos.getX());
    int anchorY = (int) Math.round(pos.getY());
    Point2D.Double anchorPos = new Point2D.Double(anchorX, anchorY);

    BuildingType type = building.getType();
    int originX = anchorX - type.getAnchorX();
    int originY = anchorY - type.getAnchorY();
    boolean[][] footprint = type.getFootprintMask();

    for (int y = 0; y < type.getHeight(); y++) {
      for (int x = 0; x < type.getWidth(); x++) {
        if (!footprint[y][x]) {
          continue;
        }
        int mapX = originX + x;
        int mapY = originY + y;
        Point2D.Double cellPos = new Point2D.Double(mapX, mapY);
        if (!isValidPosition(cellPos)) {
          return false;
        }
        MapCell cell = getCell(cellPos);
        if (cell.isOccupied() || !cell.getTerrain().isBuildable()) {
          return false;
        }
      }
    }

    building.setOrigin(originX, originY);
    building.setPosition(anchorPos);

    for (int y = 0; y < type.getHeight(); y++) {
      for (int x = 0; x < type.getWidth(); x++) {
        if (!footprint[y][x]) {
          continue;
        }
        int mapX = originX + x;
        int mapY = originY + y;
        Point2D.Double cellPos = new Point2D.Double(mapX, mapY);
        getCell(cellPos).setBuilding(building, x, y);
      }
    }

    eventBus.publish(new MapUpdatedEvent(anchorPos));
    return true;
  }

  @Override
  public boolean removeBuilding(Point2D.Double pos) {
    int anchorX = (int) Math.round(pos.getX());
    int anchorY = (int) Math.round(pos.getY());
    Point2D.Double anchorPos = new Point2D.Double(anchorX, anchorY);
    if (!isValidPosition(anchorPos)) {
      return false;
    }
    MapCell cell = getCell(anchorPos);
    Building building = cell.getBuilding();
    if (building == null) {
      return false;
    }

    BuildingType type = building.getType();
    int originX = building.getOriginX();
    int originY = building.getOriginY();
    boolean[][] footprint = type.getFootprintMask();

    for (int y = 0; y < type.getHeight(); y++) {
      for (int x = 0; x < type.getWidth(); x++) {
        if (!footprint[y][x]) {
          continue;
        }
        int mapX = originX + x;
        int mapY = originY + y;
        Point2D.Double cellPos = new Point2D.Double(mapX, mapY);
        if (!isValidPosition(cellPos)) {
          continue;
        }
        MapCell target = getCell(cellPos);
        if (target.getBuilding() == building) {
          target.clearBuilding();
        }
      }
    }

    eventBus.publish(new MapUpdatedEvent(anchorPos));
    return true;
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
