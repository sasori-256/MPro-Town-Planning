package io.github.sasori_256.town_planning.entity.resident;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;

import io.github.sasori_256.town_planning.common.core.GameConfig;
import io.github.sasori_256.town_planning.common.core.strategy.CompositeUpdateStrategy;
import io.github.sasori_256.town_planning.entity.model.BaseGameEntity;
import io.github.sasori_256.town_planning.entity.model.GameContext;
import io.github.sasori_256.town_planning.entity.resident.strategy.ResidentBehaviorAction;
import io.github.sasori_256.town_planning.entity.resident.strategy.ResidentCorpseCleanupEffect;
import io.github.sasori_256.town_planning.entity.resident.strategy.ResidentLifeCycleEffect;
import io.github.sasori_256.town_planning.entity.resident.strategy.ResidentStatusEffect;

/**
 * 住民エンティティを表すクラス。
 */
public class Resident extends BaseGameEntity {
  private static final double DEATH_ANIMATION_DURATION = GameConfig.getResidentDeathAnimationDurationSeconds();
  private static final double DAMAGE_EFFECT_DURATION = 0.5; // 赤色化する時間

  private final ResidentType type;
  private Point2D.Double homePosition;
  private Point2D.Double relocationTarget;
  private double age;
  private int faith;
  private int layerIndex;
  private ResidentState state;
  private double deathAnimationElapsed;
  private double maxHp;
  private double currentHp;
  
  // Status Effects
  private final Map<DebuffType, Double> debuffTimers = new HashMap<>();
  private final Map<DebuffType, Integer> debuffLevels = new HashMap<>();
  private final Map<DebuffType, Double> debuffIntervals = new HashMap<>();
  private double damageEffectTimer = 0.0;

  /**
   * 住民を生成する。
   *
   * @param position     現在位置
   * @param residentType 種別
   * @param state        初期状態
   * @param homePosition 自宅の座標
   */
  public Resident(Point2D.Double position, ResidentType residentType, ResidentState state,
      Point2D.Double homePosition) {
    super(position);
    this.type = residentType;
    this.homePosition = new Point2D.Double(homePosition.getX(), homePosition.getY());
    this.age = 0.0;
    this.faith = residentType.getInitialFaith();
    this.layerIndex = 0;
    this.state = state;
    this.deathAnimationElapsed = 0.0;
    this.maxHp = residentType.getMaxHp();
    this.currentHp = this.maxHp;

    CompositeUpdateStrategy strategy = new CompositeUpdateStrategy();
    strategy.setAction(new ResidentBehaviorAction());
    strategy.addEffect(new ResidentLifeCycleEffect());
    strategy.addEffect(new ResidentCorpseCleanupEffect());
    strategy.addEffect(new ResidentStatusEffect());
    this.setUpdateStrategy(strategy);
  }

  /**
   * 住民を生成する。
   *
   * @param position     現在位置
   * @param residentType 種別
   * @param state        初期状態
   * @implNote Strategy初期化は4引数コンストラクタに委譲する。
   */
  public Resident(Point2D.Double position, ResidentType residentType, ResidentState state) {
    this(position, residentType, state, position);
  }

  /**
   * ダメージを受ける。
   *
   * @param amount ダメージ量
   */
  public void damage(double amount) {
    if (state == ResidentState.DEAD) {
      return;
    }
    this.currentHp -= amount;
    this.damageEffectTimer = DAMAGE_EFFECT_DURATION; // 赤色化開始

    if (this.currentHp <= 0) {
      this.currentHp = 0;
      // markDead()はLifeCycleEffectで呼ぶのでここでは呼ばない
    }
  }

  /**
   * デバフを付与または更新する。
   *
   * @param type     デバフ種類
   * @param duration 持続時間
   * @param level    強度（レベルが高いほど強い、または若い世代）
   */
  public void addDebuff(DebuffType type, double duration, int level) {
    if (state == ResidentState.DEAD) {
      return;
    }
    // 現在の仕様: 同じ種類のデバフは常に上書きする（再感染として扱う）
    debuffTimers.put(type, duration);
    debuffLevels.put(type, level);
    // Intervalはリセットしない（ダメージタイミングをずらさないため）
    debuffIntervals.putIfAbsent(type, 0.0);
  }

  /**
   * デバフの状態を更新する。
   * Strategyから呼ばれることを想定。
   *
   * @param context ゲームコンテキスト
   */
  public void updateDebuffs(GameContext context) {
    if (state == ResidentState.DEAD) {
      debuffTimers.clear();
      debuffLevels.clear();
      debuffIntervals.clear();
      return;
    }

    double dt = context.getDeltaTime();

    // 赤色化タイマー更新
    if (damageEffectTimer > 0) {
      damageEffectTimer -= dt;
    }

    // デバフ更新
    // 削除リスト作成
    var iterator = debuffTimers.entrySet().iterator();
    while (iterator.hasNext()) {
      var entry = iterator.next();
      DebuffType type = entry.getKey();
      double timeLeft = entry.getValue();

      timeLeft -= dt;
      if (timeLeft <= 0) {
        iterator.remove();
        debuffLevels.remove(type);
        debuffIntervals.remove(type);
        continue;
      }
      entry.setValue(timeLeft);

      // 効果発動判定 (1秒ごと)
      double interval = debuffIntervals.getOrDefault(type, 0.0);
      interval += dt;
      if (interval >= 1.0) {
        interval -= 1.0;
        int level = debuffLevels.get(type);
        type.apply(context, this, level);
      }
      debuffIntervals.put(type, interval);
    }
  }

  /**
   * 現在ダメージを受けているか（赤色表示するか）。
   *
   * @return trueならダメージ状態
   */
  public boolean isDamaged() {
    return damageEffectTimer > 0;
  }

  /**
   * 指定したデバフにかかっているかを返す。
   *
   * @param type デバフ種類
   * @return かかっていればtrue
   */
  public boolean hasDebuff(DebuffType type) {
    return debuffTimers.containsKey(type);
  }

  /**
   * 現在のHPを返す。

  /**
   * 現在のHPを返す。
   *
   * @return 現在のHP
   */
  public double getHp() {
    return this.currentHp;
  }

  /**
   * 最大HPを返す。
   *
   * @return 最大HP
   */
  public double getMaxHp() { // BaseGameEntityのメソッドと被らないか注意だが、BaseGameEntityにはないはず
    return this.maxHp;
  }

  /**
   * 年齢を設定する。
   *
   * @param age 年齢
   */
  public void setAge(double age) {
    this.age = Math.max(0.0, age);
  }

  /**
   * 信仰度を設定する。
   *
   * @param faith 信仰度
   */
  public void setFaith(int faith) {
    this.faith = Math.max(0, faith);
  }

  /** {@inheritDoc} */
  @Override
  public void setLayerIndex(int layerIndex) {
    this.layerIndex = layerIndex;
  }

  /**
   * 状態を設定する。
   *
   * @param state 住民状態
   */
  public void setState(ResidentState state) {
    if (state == ResidentState.DEAD && this.state != ResidentState.DEAD) {
      this.deathAnimationElapsed = 0.0;
    }
    this.state = state;
  }

  /**
   * 死亡状態にする。
   */
  public void markDead() {
    setState(ResidentState.DEAD);
  }

  /**
   * 住民種別を返す。
   *
   * @return 住民種別
   */
  public ResidentType getType() {
    return this.type;
  }

  /**
   * 自宅の座標を返す。
   */
  public Point2D.Double getHomePosition() {
    return new Point2D.Double(homePosition.getX(), homePosition.getY());
  }

  /**
   * 自宅の座標を更新する。
   *
   * @param homePosition 自宅の座標
   */
  public void setHomePosition(Point2D.Double homePosition) {
    this.homePosition = new Point2D.Double(homePosition.getX(), homePosition.getY());
  }

  /**
   * 引っ越し先が設定されているかを返す。
   */
  public boolean hasRelocationTarget() {
    return relocationTarget != null;
  }

  /**
   * 引っ越し先の座標を返す。
   *
   * @return 引っ越し先の座標。設定がない場合はnull。
   */
  public Point2D.Double getRelocationTarget() {
    if (relocationTarget == null) {
      return null;
    }
    return new Point2D.Double(relocationTarget.getX(), relocationTarget.getY());
  }

  /**
   * 引っ越し先を設定する。
   *
   * @param target 引っ越し先の座標
   */
  public void requestRelocation(Point2D.Double target) {
    this.relocationTarget = new Point2D.Double(target.getX(), target.getY());
  }

  /**
   * 引っ越し先の設定を解除する。
   */
  public void clearRelocationTarget() {
    this.relocationTarget = null;
  }

  /**
   * 年齢を返す。
   *
   * @return 年齢
   */
  public double getAge() {
    return this.age;
  }

  /**
   * 最大年齢を返す。
   *
   * @return 最大年齢
   */
  public double getMaxAge() {
    return this.type.getMaxAge();
  }

  /**
   * 信仰度を返す。
   *
   * @return 信仰度
   */
  public int getFaith() {
    return this.faith;
  }

  /** {@inheritDoc} */
  @Override
  public int getLayerIndex() {
    return this.layerIndex;
  }

  /**
   * 現在の状態を返す。
   *
   * @return 住民状態
   */
  public ResidentState getState() {
    return this.state;
  }

  /**
   * 死亡アニメーションの進行度を返す。
   *
   * @return 0.0-1.0 の範囲
   */
  public double getDeathAnimationProgress() {
    if (state != ResidentState.DEAD) {
      return 0.0;
    }
    if (DEATH_ANIMATION_DURATION <= 0.0) {
      return 1.0;
    }
    return Math.min(1.0, deathAnimationElapsed / DEATH_ANIMATION_DURATION);
  }

  /** {@inheritDoc} */
  @Override
  public void advanceAnimation(double dt) {
    if (state != ResidentState.DEAD) {
      return;
    }
    if (deathAnimationElapsed >= DEATH_ANIMATION_DURATION) {
      return;
    }
    if (dt <= 0.0) {
      return;
    }
    deathAnimationElapsed = Math.min(DEATH_ANIMATION_DURATION, deathAnimationElapsed + dt);
  }
}
