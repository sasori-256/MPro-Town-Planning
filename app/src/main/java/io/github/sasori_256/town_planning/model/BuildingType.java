package io.github.sasori_256.town_planning.model;

import java.awt.Color;

/**
 * 建物の種類定義。
 */
public enum BuildingType {
  HOUSE("住居", "H", Color.CYAN, 50, 4, 100),
  CHURCH("教会", "C", Color.YELLOW, 150, 0, 150),
  GRAVEYARD("墓地", "G", Color.GRAY, 100, 0, 100);

  private final String displayName;
  private final String symbol;
  private final Color color;
  private final int cost;
  private final int capacity;
  private final int maxDurability;

  BuildingType(String displayName, String symbol, Color color, int cost, int capacity, int maxDurability) {
    this.displayName = displayName;
    this.symbol = symbol;
    this.color = color;
    this.cost = cost;
    this.capacity = capacity;
    this.maxDurability = maxDurability;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getSymbol() {
    return symbol;
  }

  // スプライトと合わせるならここを画像に変更する必要がある
  public Color getColor() {
    return color;
  }

  public int getCost() {
    return cost;
  }

  public int getCapacity() {
    return capacity;
  }

  public int getMaxDurability() {
    return maxDurability;
  }
}
