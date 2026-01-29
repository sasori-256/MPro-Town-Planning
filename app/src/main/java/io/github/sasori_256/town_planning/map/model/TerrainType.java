package io.github.sasori_256.town_planning.map.model;

import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 地形の種別を表す列挙型。
 */
public enum TerrainType implements Terrain {
  ERROR("Error", "Error", false, false, 1_000_000),
  GRASS("Grass", "Grass", true, true, 2, 90),
  GRASS2("Grass2", "Grass", true, true, 2, 2),
  GRASS3("Grass3", "Grass", true, true, 2, 5),
  GRASS4("Grass4", "Grass", true, true, 2, 3),
  WATER("Water", "Water", false, false, 1_000_000),
  MOUNTAIN("Mountain", "Mountain", false, false, 1_000_000),

  COAST_XpXmYpYm("Coast_XpXmYpYm", "Water", false, false, 1_000_000),
  COAST_XmYpYm("Coast_XmYpYm", "Water", false, false, 1_000_000),
  COAST_XpYpYm("Coast_XpYpYm", "Water", false, false, 1_000_000),
  COAST_XpXmYp("Coast_XpXmYp", "Water", false, false, 1_000_000),
  COAST_XpXmYm("Coast_XpXmYm", "Water", false, false, 1_000_000),
  COAST_XpXm("Coast_XpXm", "Water", false, false, 1_000_000),
  COAST_YpYm("Coast_YpYm", "Water", false, false, 1_000_000),
  COAST_XpYp("Coast_XpYp", "Water", false, false, 1_000_000),
  COAST_XpYm("Coast_XpYm", "Water", false, false, 1_000_000),
  COAST_XmYp("Coast_XmYp", "Water", false, false, 1_000_000),
  COAST_XmYm("Coast_XmYm", "Water", false, false, 1_000_000),
  COAST_XpYpN("Coast_XpYpN", "Water", false, false, 1_000_000),
  COAST_XpYmW("Coast_XpYmW", "Water", false, false, 1_000_000),
  COAST_XmYpE("Coast_XmYpE", "Water", false, false, 1_000_000),
  COAST_XmYmS("Coast_XmYmS", "Water", false, false, 1_000_000),
  COAST_Xp("Coast_Xp", "Water", false, false, 1_000_000),
  COAST_Xm("Coast_Xm", "Water", false, false, 1_000_000),
  COAST_Yp("Coast_Yp", "Water", false, false, 1_000_000),
  COAST_Ym("Coast_Ym", "Water", false, false, 1_000_000),
  COAST_XpN("Coast_XpN", "Water", false, false, 1_000_000),
  COAST_XpW("Coast_XpW", "Water", false, false, 1_000_000),
  COAST_XmE("Coast_XmE", "Water", false, false, 1_000_000),
  COAST_XmS("Coast_XmS", "Water", false, false, 1_000_000),
  COAST_YpN("Coast_YpN", "Water", false, false, 1_000_000),
  COAST_YpE("Coast_YpE", "Water", false, false, 1_000_000),
  COAST_YmW("Coast_YmW", "Water", false, false, 1_000_000),
  COAST_YmS("Coast_YmS", "Water", false, false, 1_000_000),
  COAST_XpNW("Coast_XpNW", "Water", false, false, 1_000_000),
  COAST_XmSW("Coast_XmES", "Water", false, false, 1_000_000),
  COAST_YpNE("Coast_YpNE", "Water", false, false, 1_000_000),
  COAST_YmWS("Coast_YmWS", "Water", false, false, 1_000_000),
  COAST_N("Coast_N", "Water", false, false, 1_000_000),
  COAST_E("Coast_E", "Water", false, false, 1_000_000),
  COAST_S("Coast_S", "Water", false, false, 1_000_000),
  COAST_W("Coast_W", "Water", false, false, 1_000_000),
  COAST_NE("Coast_NE", "Water", false, false, 1_000_000),
  COAST_SE("Coast_SE", "Water", false, false, 1_000_000),
  COAST_SW("Coast_SW", "Water", false, false, 1_000_000),
  COAST_NW("Coast_NW", "Water", false, false, 1_000_000),
  COAST_NS("Coast_NS", "Water", false, false, 1_000_000),
  COAST_EW("Coast_EW", "Water", false, false, 1_000_000),
  COAST_ESW("Coast_ESW", "Water", false, false, 1_000_000),
  COAST_NSW("Coast_NSW", "Water", false, false, 1_000_000),
  COAST_NEW("Coast_NEW", "Water", false, false, 1_000_000),
  COAST_NES("Coast_NES", "Water", false, false, 1_000_000),
  COAST_NESW("Coast_NESW", "Water", false, false, 1_000_000);

  private final String displayName;
  private final String kind;
  private final boolean walkable;
  private final boolean buildable;
  private final int moveCost;
  private final int weight;

  TerrainType(String displayName, String kind, boolean buildable, boolean walkable, int moveCost, int weight) {
    this.displayName = displayName;
    this.kind = kind;
    this.buildable = buildable;
    this.walkable = walkable;
    this.moveCost = moveCost;
    this.weight = weight;
  }

  TerrainType(String displayName, String kind, boolean buildable, boolean walkable, int moveCost) {
    this(displayName, kind, buildable, walkable, moveCost, 0);
  }

  /** {@inheritDoc} */
  @Override
  public String getDisplayName() {
    return displayName;
  }

  /** {@inheritDoc} */
  @Override
  public String getKind() {
    return kind;
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

  public int getWeight() {
    return weight;
  }

  /** {@inheritDoc} */
  @Override
  public void draw() {
  }

  private static final Map<String, TerrainType> BY_DISPLAY_NAME;

  static {
    Map<String, TerrainType> m = new HashMap<>();
    for (TerrainType t : values()) {
      m.put(t.displayName, t);
    }
    BY_DISPLAY_NAME = unmodifiableMap(m);
  }

  /**
   * 指定された表示名と一致する地形種別を返す。見つからなければ null を返す。
   *
   * @param name 表示名
   * @return 一致する TerrainType または null
   */
  public static TerrainType fromDisplayName(String name) {
    if (name == null)
      return null;
    return BY_DISPLAY_NAME.get(name);
  }

  private static final List<TerrainType> GRASS_TYPES;
  private static final int TOTAL_GRASS_WEIGHT;

  static {
    List<TerrainType> grassList = new ArrayList<>();
    int totalWeight = 0;

    for (TerrainType type : values()) {
      if (type.getKind().equals("Grass") && type.getWeight() > 0) {
        grassList.add(type);
        totalWeight += type.getWeight();
      }
    }
    GRASS_TYPES = unmodifiableList(grassList);
    TOTAL_GRASS_WEIGHT = totalWeight;
  }

  public static TerrainType getRandomGrassType(Random random) {
    if (TOTAL_GRASS_WEIGHT <= 0) {
      return GRASS;
    }
    int randomValue = random.nextInt(TOTAL_GRASS_WEIGHT);
    int cumulativeWeight = 0;
    for (TerrainType type : GRASS_TYPES) {
      cumulativeWeight += type.getWeight();
      if (randomValue < cumulativeWeight) {
        return type;
      }
    }
    return GRASS;
  }
}
