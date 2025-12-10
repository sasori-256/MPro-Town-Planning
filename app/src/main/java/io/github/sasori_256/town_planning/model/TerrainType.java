package io.github.sasori_256.town_planning.model;

import io.github.sasori_256.town_planning.core.Terrain;

public enum TerrainType implements Terrain {
  GRASS(true, true, "Grass"),
  WATER(false, false, "Water"),
  MOUNTAIN(false, false, "Mountain"),
  ROAD(true, false, "Road");

  private final boolean walkable;
  private final boolean buildable;
  private final String id;

  TerrainType(boolean walkable, boolean buildable, String id) {
    this.walkable = walkable;
    this.buildable = buildable;
    this.id = id;
  }

  @Override
  public boolean isWalkable() {
    return walkable;
  }

  @Override
  public boolean isBuildable() {
    return buildable;
  }

  @Override
  public String getId() {
    return id;
  }
}
