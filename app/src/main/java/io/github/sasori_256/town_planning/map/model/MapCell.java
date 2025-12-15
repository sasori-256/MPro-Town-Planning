package io.github.sasori_256.town_planning.map.model;

import java.util.Optional;

import io.github.sasori_256.town_planning.gameObject.building.BuildingObject;

public class MapCell {
  private Terrain terrain;
  private BuildingObject building;

  public MapCell(Terrain initTerrain) {
    this.terrain = initTerrain;
    this.building = null;
  }

  public Terrain getTerrain() {
    return terrain;
  }

  public boolean setTerrain(Terrain terrain) {
    this.terrain = terrain;
    return true;
  }

  public Optional<BuildingObject> getBuilding() {
    return Optional.ofNullable(building);
  }

  public boolean setBuilding(BuildingObject building) {
    this.building = building;
    return true;
  }

  public boolean removeBuilding() {
    this.building = null;
    return true;
  }

  public boolean canBuild() {
    return terrain.isBuildable() && building == null;
  }

  public boolean canWalk() {
    boolean terrainOk = terrain.isWalkable();
    boolean buildingOk = (building == null);
    return terrainOk && buildingOk;
  }
}
