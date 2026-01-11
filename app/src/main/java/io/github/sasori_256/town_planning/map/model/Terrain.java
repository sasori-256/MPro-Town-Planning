package io.github.sasori_256.town_planning.map.model;

public interface Terrain {
  String getDisplayName();

  boolean isWalkable();

  boolean isBuildable();

  int getMoveCost();

  void draw();
}
