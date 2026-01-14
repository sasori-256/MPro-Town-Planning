package io.github.sasori_256.town_planning.entity.building;

import java.awt.geom.Point2D;
import java.util.function.Supplier;

import io.github.sasori_256.town_planning.common.core.strategy.CompositeUpdateStrategy;
import io.github.sasori_256.town_planning.entity.model.BaseGameEntity;
import io.github.sasori_256.town_planning.entity.model.GameEffect;

/**
 * 建物オブジェクトを表すクラス。
 * 建物の種類や特性を持つ。
 */
public class Building extends BaseGameEntity {
  private final BuildingType type;
  private int currentDurability;
  private int currentPopulation;
  private int originX;
  private int originY;
  private double animationElapsedSeconds;

  /**
   * 建物を生成する。
   *
   * @param position     設置位置
   * @param buildingType 建物種別
   */
  public Building(Point2D.Double position, BuildingType buildingType) {
    super(position);
    this.type = buildingType;
    this.currentDurability = buildingType.getMaxDurability();
    this.currentPopulation = 0;
    this.originX = (int) Math.round(position.getX());
    this.originY = (int) Math.round(position.getY());
    this.animationElapsedSeconds = 0.0;

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

  /**
   * 建物種別を返す。
   *
   * @return 建物種別
   */
  public BuildingType getType() {
    return this.type;
  }

  /**
   * 現在の耐久度を返す。
   *
   * @return 現在耐久度
   */
  public int getCurrentDurability() {
    return this.currentDurability;
  }

  /**
   * 現在の耐久度を設定する。
   *
   * @param durability 耐久度
   */
  public void setCurrentDurability(int durability) {
    this.currentDurability = Math.max(0, Math.min(durability, this.type.getMaxDurability()));
  }

  /**
   * 現在の住民数を返す。
   *
   * @return 住民数
   */
  public int getCurrentPopulation() {
    return this.currentPopulation;
  }

  /**
   * 現在の住民数を設定する。
   *
   * @param population 住民数
   */
  public void setCurrentPopulation(int population) {
    this.currentPopulation = Math.max(0, Math.min(population, this.type.getMaxPopulation()));
  }

  /**
   * 建物の原点Xを返す。
   *
   * @return 原点X
   */
  public int getOriginX() {
    return originX;
  }

  /**
   * 建物の原点Yを返す。
   *
   * @return 原点Y
   */
  public int getOriginY() {
    return originY;
  }

  /**
   * 建物の原点座標を設定する。
   *
   * @param originX 原点X
   * @param originY 原点Y
   */
  public void setOrigin(int originX, int originY) {
    this.originX = originX;
    this.originY = originY;
  }

  /** {@inheritDoc} */
  @Override
  public void advanceAnimation(double dt) {
    if (!type.hasAnimation()) {
      return;
    }
    if (dt <= 0) {
      return;
    }
    animationElapsedSeconds += dt;
  }

  /**
   * 指定タイルのアニメーションフレーム番号を返す。
   *
   * @param localX 建物内のX座標
   * @param localY 建物内のY座標
   */
  public int getAnimationFrameIndex(int localX, int localY) {
    int fps = type.getAnimationFrameRate(localX, localY);
    if (fps <= 0) {
      return 0;
    }
    return (int) Math.floor(animationElapsedSeconds * fps);
  }
}
