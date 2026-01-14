package io.github.sasori_256.town_planning.map.model;

/**
 * 地形の種別を表す列挙型。
 */
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

  /** {@inheritDoc} */
  @Override
  public String getDisplayName() {
    return displayName;
  }

  /** {@inheritDoc} */
  @Override
  public boolean isWalkable() {
    return walkable;
  }

  /** {@inheritDoc} */
  @Override
  public boolean isBuildable() {
    return buildable;
  }

  /** {@inheritDoc} */
  @Override
  public int getMoveCost() {
    return moveCost;
  }

  /** {@inheritDoc} */
  @Override
  public void draw() {
  }
}
