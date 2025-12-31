package io.github.sasori_256.town_planning.entity.building;

import java.util.function.Supplier;

import io.github.sasori_256.town_planning.entity.building.strategy.PopulationGrowthEffect;
import io.github.sasori_256.town_planning.entity.model.CategoryType;
import io.github.sasori_256.town_planning.entity.model.GameEffect;

/**
 * 建物の種類定義。
 */
public enum BuildingType {
  // maxPopulationとPopulationGrowthStrategyの紐付けを行い、Strategy生成用Supplierを設定
  HOUSE("住居", "red_roofed_house", 50, 4, 100, () -> new PopulationGrowthEffect(4), CategoryType.RESIDENTIAL),
  CHURCH("教会", "error_building", 150, 0, 150, CategoryType.RELIGIOUS),
  GRAVEYARD("墓地", "error_building", 100, 0, 100, CategoryType.CEMETERY),
  NONE("none", "none", 0, 0, 0, CategoryType.NONE);

  private final String displayName;
  private final String imageName;
  private final int cost;
  private final int maxPopulation;
  private final int maxDurability;
  // GameEffect(並行機能)のファクトリ
  private final Supplier<GameEffect> effectSupplier;
  private final CategoryType category;

  // Effectありのコンストラクタ
  BuildingType(String displayName, String imageName, int cost, int maxPopulation, int maxDurability,
      Supplier<GameEffect> effectSupplier, CategoryType category) {
    this.displayName = displayName;
    this.imageName = imageName;
    this.cost = cost;
    this.maxPopulation = maxPopulation;
    this.maxDurability = maxDurability;
    this.effectSupplier = effectSupplier;
    this.category = category;
  }

  // Effectなしのコンストラクタ
  BuildingType(String displayName, String imageName, int cost, int maxPopulation, int maxDurability,
      CategoryType category) {
    this(displayName, imageName, cost, maxPopulation, maxDurability, () -> null, category);
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

  public CategoryType getCategory() {
    return category;
  }
}
