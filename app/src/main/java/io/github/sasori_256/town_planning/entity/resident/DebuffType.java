package io.github.sasori_256.town_planning.entity.resident;

import io.github.sasori_256.town_planning.entity.model.GameContext;

/**
 * 住民にかかるデバフの種類定義。
 */
public enum DebuffType {
  /** 疫病。スリップダメージと感染を引き起こす。 */
  PLAGUE {
    @Override
    public void apply(GameContext context, Resident self, int level) {
      // スリップダメージ (1秒ごとに呼ばれる前提で5ダメージ)
      double damageAmount = 5.0; 
      self.damage(damageAmount);

      // 伝染処理
      if (level > 0) { // レベルが0なら感染力なし
        double infectionRadius = 1.5; // 1.5タイル以内
        double radiusSq = infectionRadius * infectionRadius;
        
        // 近くの生存者を探す
        // 注意: 毎フレーム全探索は重いが、今回はResident数が数百程度と仮定。
        // 必要ならSpatialPartitioningなどを導入するが、まずはシンプルに実装。
        context.getResidentEntities()
            .filter(r -> r.getState() != ResidentState.DEAD)
            .filter(r -> r.getState() != ResidentState.AT_HOME) // 家にいる住民は対象外
            .filter(r -> r != self) // 自分以外
            .filter(r -> r.getPosition().distanceSq(self.getPosition()) <= radiusSq)
            .forEach(target -> {
               // 感染（レベルを下げて伝染）。持続時間は10秒でリセット。
               // 既に感染していても、より高いレベル（若い世代）の菌なら更新されるロジックはaddDebuff側で制御
               target.addDebuff(PLAGUE, INFECTION_DURATION_SECONDS, level - 1);
            });
      }
    }
  };

  /**
   * 疫病デバフの持続時間（秒）。
   * 他所と値を揃える場合はここを変更する。
   */
  public static final double INFECTION_DURATION_SECONDS = 10.0;

  /**
   * デバフの効果を適用する。
   *
   * @param context ゲームコンテキスト
   * @param self    対象の住民
   * @param level   デバフの強度（感染レベルなど）
   */
  public abstract void apply(GameContext context, Resident self, int level);
}
