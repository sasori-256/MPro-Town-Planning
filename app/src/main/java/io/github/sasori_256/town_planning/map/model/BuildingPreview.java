package io.github.sasori_256.town_planning.map.model;

import java.awt.geom.Point2D;
import java.util.concurrent.locks.ReadWriteLock;

import io.github.sasori_256.town_planning.entity.building.BuildingType;
import io.github.sasori_256.town_planning.map.model.GameMap;
import io.github.sasori_256.town_planning.map.model.MapCell;

public class BuildingPreview {
  private final ReadWriteLock stateLock;
  private Point2D.Double buildingPreviewPos = null;
  private BuildingType buildingPreviewType = null;
  private boolean buildable = false;
  private GameMap gameMap;

  public BuildingPreview(ReadWriteLock stateLock, GameMap gameMap) {
    this.stateLock = stateLock;
    this.gameMap = gameMap;
  }

  public void setBuildingPreviewPos(Point2D.Double pos) {
    try {
      stateLock.writeLock().lock();
      this.buildingPreviewPos = pos;
      if (pos != null) {
        this.buildable = gameMap.canPlaceBuilding(pos, buildingPreviewType);
      }

    } finally {
      stateLock.writeLock().unlock();
    }
  }

  public Point2D.Double getBuildingPreviewPos() {
    try {
      stateLock.readLock().lock();
      return buildingPreviewPos;
    } finally {
      stateLock.readLock().unlock();
    }
  }

  public void setBuildingPreviewType(BuildingType type) {
    try {
      stateLock.writeLock().lock();
      this.buildingPreviewType = type;
      if (buildingPreviewPos != null && buildingPreviewType != null) {
        this.buildable = gameMap.canPlaceBuilding(buildingPreviewPos, buildingPreviewType);
      } else {
        this.buildable = false;
      }
    } finally {
      stateLock.writeLock().unlock();
    }
  }

  public BuildingType getBuildingPreviewType() {
    try {
      stateLock.readLock().lock();
      return buildingPreviewType;
    } finally {
      stateLock.readLock().unlock();
    }
  }

  public boolean getBuildable() {
    try {
      stateLock.readLock().lock();
      return buildable;
    } finally {
      stateLock.readLock().unlock();
    }
  }
}
