package io.github.sasori_256.town_planning.model;

public enum TerrainType implements Terrain {
  GRASS("Grass", true, true),
  WATER("Water", false, false),
  MOUNTAIN("Mountain", false, false),
  ROAD("Road", false, true);

  private final String displayName;
  private final boolean walkable;
  private final boolean buildable;

  TerrainType(String displayName, boolean buildable, boolean walkable) {
    this.displayName = displayName;
    this.walkable = walkable;
    this.buildable = buildable;
  }

  @Override
  public String getDisplayName() {
    return displayName;
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
  public void draw() {
  }
}
