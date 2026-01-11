package io.github.sasori_256.town_planning.map.model;

public enum TerrainType implements Terrain {
  GRASS("Grass", true, true, 2),
  WATER("Water", false, false, 1_000_000),
  MOUNTAIN("Mountain", false, false, 1_000_000);

  private final String displayName;
  private final boolean walkable;
  private final boolean buildable;
  private final int moveCost;

  TerrainType(String displayName, boolean buildable, boolean walkable, int moveCost) {
    this.displayName = displayName;
    this.buildable = buildable;
    this.walkable = walkable;
    this.moveCost = moveCost;
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
  public int getMoveCost() {
    return moveCost;
  }

  @Override
  public void draw() {
  }
}
