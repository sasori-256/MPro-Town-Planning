package io.github.sasori_256.town_planning.gameObject.building;

import java.awt.Color;
import java.util.function.Supplier;

import io.github.sasori_256.town_planning.gameObject.model.GameEffect;
import io.github.sasori_256.town_planning.gameObject.building.strategy.PopulationGrowthEffect;

/**
 * 建物の種類定義。
 */
public enum BuildingType {
  // maxPopulationとPopulationGrowthStrategyの紐付けを行い、Strategy生成用Supplierを設定
  HOUSE("住居", "H", Color.CYAN, 50, 4, 100, () -> new PopulationGrowthEffect(4)),
  CHURCH("教会", "C", Color.YELLOW, 150, 0, 150),
  GRAVEYARD("墓地", "G", Color.GRAY, 100, 0, 100),
  NONE("none", " ", Color.BLACK, 0, 0, 0);

  private final String displayName;
  private final String symbol;
  private final Color color;
  private final int cost;
  private final int maxPopulation;
  private final int maxDurability;
  // GameEffect(並行機能)のファクトリ
  private final Supplier<GameEffect> effectSupplier;

  // Effectありのコンストラクタ
  BuildingType(String displayName, String symbol, Color color, int cost, int maxPopulation, int maxDurability,
      Supplier<GameEffect> effectSupplier) {
    this.displayName = displayName;
    this.symbol = symbol;
    this.color = color;
    this.cost = cost;
    this.maxPopulation = maxPopulation;
    this.maxDurability = maxDurability;
    this.effectSupplier = effectSupplier;
  }

  // Effectなしのコンストラクタ
  BuildingType(String displayName, String symbol, Color color, int cost, int maxPopulation, int maxDurability) {
    this(displayName, symbol, color, cost, maxPopulation, maxDurability, () -> null);
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

  public int getMaxPopulation() {
    return maxPopulation;
  }

  public int getMaxDurability() {
    return maxDurability;
  }

  public Supplier<GameEffect> getEffectSupplier() {
    return effectSupplier;
  }
}
