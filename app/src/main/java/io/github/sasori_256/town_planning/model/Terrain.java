package io.github.sasori_256.town_planning.core;

public interface Terrain {
  boolean isWalkable();

  boolean isBuildable();

  String getId();
}
