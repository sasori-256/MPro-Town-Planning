package io.github.sasori_256.town_planning.model;

import io.github.sasori_256.town_planning.core.*;
import io.github.sasori_256.town_planning.event.*;
import java.util.HashMap;
import java.util.Map;
import java.awt.geom.Point2D;

/**
 * ゲームの主要なモデルクラス。
 * ゲームの状態を管理し、イベントを発行する。
 * TODO: Mapの管理をMapModelクラスで行うようにリファクタリングする。
 */
public class GameModel implements Updatable {
  private final EventBus eventBus;
  private final Map<String, GameEntity> tile = new HashMap<>(); // TODO: MapModelクラスに移動する
  private int souls = 100; // 初期ソウル数

  public GameModel(EventBus eventBus) {
    this.eventBus = eventBus;
  }

  public boolean addSouls(int amount) {
    this.souls += amount;
    eventBus.publish(EventType.SOUL_CHANGED, this.souls);
    return true;
  }

  public int getSouls() {
    return this.souls;
  }

  /**
   * シミュレーションロジックを行う
   * ex: 10%の確率で人口が増えるなど
   * TODO: 実装する
   *
   */
  @Override
  public void tick() {
  }
}
