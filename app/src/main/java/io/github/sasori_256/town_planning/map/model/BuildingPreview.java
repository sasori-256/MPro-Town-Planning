package io.github.sasori_256.town_planning.map.model;

import java.awt.geom.Point2D;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Function;

import io.github.sasori_256.town_planning.common.event.EventBus;
import io.github.sasori_256.town_planning.common.event.events.CancelBuildEvent;
import io.github.sasori_256.town_planning.common.event.events.RotateBuildEvent;
import io.github.sasori_256.town_planning.entity.building.BuildingType;
import io.github.sasori_256.town_planning.entity.model.BaseGameEntity;

public class BuildingPreview {
  private final EventBus eventBus = EventBus.getInstance();
  private final ReadWriteLock stateLock;
  private Function<Point2D.Double, ? extends BaseGameEntity> entityGenerator = (point) -> null;
  private Point2D.Double buildingPreviewPos = null;
  private BuildingType buildingPreviewType = null;
  private boolean isRotated = false;
  private boolean buildable = false;
  private GameMap gameMap;

  public BuildingPreview(ReadWriteLock stateLock, GameMap gameMap) {
    this.stateLock = stateLock;
    this.gameMap = gameMap;
    eventBus.subscribe(CancelBuildEvent.class, event -> {
      resetBuildingPreviewData();
    });
    eventBus.subscribe(RotateBuildEvent.class, event -> {
      switchRotated();
    });
  }

  public void setEntityGenerator(
      Function<Point2D.Double, ? extends BaseGameEntity> entityGenerator) {
    try {
      stateLock.writeLock().lock();
      this.entityGenerator = entityGenerator;
    } finally {
      stateLock.writeLock().unlock();
    }
  }

  public Function<Point2D.Double, ? extends BaseGameEntity> getEntityGenerator() {
    try {
      stateLock.readLock().lock();
      return entityGenerator;
    } finally {
      stateLock.readLock().unlock();
    }
  }

  public void setBuildingPreviewPos(Point2D.Double pos) {
    try {
      stateLock.writeLock().lock();
      if (pos == null) {
        this.buildingPreviewPos = null;
        this.buildable = false;
        return;
      }
      Point2D.Double roundedPos = new Point2D.Double(Math.round(pos.x), Math.round(pos.y));
      this.buildingPreviewPos = roundedPos;
      this.buildable = gameMap.canPlaceBuilding(roundedPos, buildingPreviewType);
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

  public void switchRotated() {
    try {
      stateLock.writeLock().lock();
      this.isRotated = !this.isRotated;
    } finally {
      stateLock.writeLock().unlock();
    }
  }

  public boolean isRotated() {
    try {
      stateLock.readLock().lock();
      return isRotated;
    } finally {
      stateLock.readLock().unlock();
    }
  }

  private void resetBuildingPreviewData() {
    try {
      stateLock.writeLock().lock();
      this.buildingPreviewPos = null;
      this.buildingPreviewType = null;
      this.buildable = false;
    } finally {
      stateLock.writeLock().unlock();
    }
  }

}
