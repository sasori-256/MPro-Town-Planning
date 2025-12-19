package io.github.sasori_256.town_planning.gameobject.building;

import java.awt.geom.Point2D;
import java.util.function.Supplier;

import io.github.sasori_256.town_planning.gameobject.model.BaseGameEntity;
import io.github.sasori_256.town_planning.common.core.CompositeUpdateStrategy;
import io.github.sasori_256.town_planning.gameobject.model.GameEffect;

/**
 * 建物オブジェクトを表すクラス。
 * 建物の種類や特性を持つ。
 */
public class Building extends BaseGameEntity {
  private final BuildingType type;
  private int currentDurability;
  private int currentPopulation;

  public Building(Point2D.Double position, BuildingType buildingType) {
    super(position);
    this.type = buildingType;
    this.currentDurability = buildingType.getMaxDurability();
    this.currentPopulation = 0;

    // CompositeUpdateStrategy を使用して、排他アクションと並行エフェクトを管理可能にする
    CompositeUpdateStrategy strategy = new CompositeUpdateStrategy();
    
    // Typeに定義されたEffectSupplierから、このインスタンス専用のEffectを生成して追加
    Supplier<GameEffect> effectSupplier = buildingType.getEffectSupplier();
    if (effectSupplier != null) {
        GameEffect effect = effectSupplier.get();
        if (effect != null) {
            strategy.addEffect(effect);
        }
    }
    
    this.setUpdateStrategy(strategy);
  }

  public BuildingType getType() {
    return this.type;
  }

  public int getCurrentDurability() {
    return this.currentDurability;
  }

  public void setCurrentDurability(int durability) {
    this.currentDurability = Math.max(0, Math.min(durability, this.type.getMaxDurability()));
  }

  public int getCurrentPopulation() {
    return this.currentPopulation;
  }

  public void setCurrentPopulation(int population) {
    this.currentPopulation = Math.max(0, Math.min(population, this.type.getMaxPopulation()));
  }
}
