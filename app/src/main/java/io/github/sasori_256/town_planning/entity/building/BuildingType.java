package io.github.sasori_256.town_planning.entity.building;

import java.util.function.Supplier;

import io.github.sasori_256.town_planning.entity.model.GameEffect;
import io.github.sasori_256.town_planning.entity.building.strategy.PopulationGrowthEffect;

/**
 * 建物の種類定義。
 */
public enum BuildingType {
  // maxPopulationとPopulationGrowthStrategyの紐付けを行い、Strategy生成用Supplierを設定
  HOUSE("住居", "error_building", 50, 4, 100, () -> new PopulationGrowthEffect(4)),
  CHURCH("教会", "error_building", 150, 0, 150),
  GRAVEYARD("墓地", "error_building", 100, 0, 100),
  NONE("none", "error_building", 0, 0, 0);

  private final String displayName;
  private final String imageName;
  private final int cost;
  private final int maxPopulation;
  private final int maxDurability;
  // GameEffect(並行機能)のファクトリ
  private final Supplier<GameEffect> effectSupplier;

  // Effectありのコンストラクタ
  BuildingType(String displayName, String imageName, int cost, int maxPopulation, int maxDurability,
      Supplier<GameEffect> effectSupplier) {
    this.displayName = displayName;
    this.imageName = imageName;
    this.cost = cost;
    this.maxPopulation = maxPopulation;
    this.maxDurability = maxDurability;
    this.effectSupplier = effectSupplier;
  }

  // Effectなしのコンストラクタ
  BuildingType(String displayName, String imageName, int cost, int maxPopulation, int maxDurability) {
    this(displayName, imageName, cost, maxPopulation, maxDurability, () -> null);
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getImageName() {
    return imageName;
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
