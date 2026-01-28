package io.github.sasori_256.town_planning.entity.disaster.strategy;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.stream.Collectors;

import io.github.sasori_256.town_planning.common.event.EventBus;
import io.github.sasori_256.town_planning.common.event.events.DisasterOccurredEvent;
import io.github.sasori_256.town_planning.entity.disaster.Disaster;
import io.github.sasori_256.town_planning.entity.disaster.DisasterType;
import io.github.sasori_256.town_planning.entity.model.BaseGameEntity;
import io.github.sasori_256.town_planning.entity.model.GameAction;
import io.github.sasori_256.town_planning.entity.model.GameContext;
import io.github.sasori_256.town_planning.entity.resident.DebuffType;
import io.github.sasori_256.town_planning.entity.resident.Resident;
import io.github.sasori_256.town_planning.entity.resident.ResidentState;

/**
 * 疫病災害のロジック。
 * 一定範囲内の住民に疫病デバフをばら撒く。
 */
public class PlagueDisasterAction implements GameAction {
  // 定数定義
  private static final double DURATION = 10.0;
  private static final double SPREAD_INTERVAL = 1.0;
  private static final int INITIAL_INFECTION_LEVEL = 5; // 初期感染レベル（最強）
  private static final double INFECTION_DURATION = 10.0; // デバフ持続時間

  private final EventBus eventBus = EventBus.getInstance();
  private final Point2D.Double center;
  private final DisasterType type;
  private double timer;
  private double lastSpreadTime;
  private boolean started;

  /**
   * 疫病災害アクションを生成する。
   *
   * @param center 発生中心位置
   * @param type   災害種別
   */
  public PlagueDisasterAction(Point2D.Double center, DisasterType type) {
    this.center = new Point2D.Double(center.x, center.y);
    this.type = type;
    this.timer = 0.0;
    this.lastSpreadTime = -SPREAD_INTERVAL; // 初回即時実行のため
    this.started = false;
  }

  /** {@inheritDoc} */
  @Override
  public void execute(GameContext context, BaseGameEntity self) {
    if (!(self instanceof Disaster)) {
      return;
    }
    
    // 初回実行時の処理
    if (!started) {
      started = true;
      eventBus.publish(new DisasterOccurredEvent(type));
      // 位置を確定
      self.setPosition(center);
      // アニメーション設定（仮でloadingを使用）
      // DisasterType側で設定されている画像名が使われるが、もし変更したいならここで行う。
      // 今回はloading画像がDisasterTypeで指定される前提。
      // ただし、Disasterクラス側でアニメーションFPSなどはデフォルト値が使われる。
      // loadingアニメは8枚あるとしてFPSを設定するならここ。
       ((Disaster) self).setAnimation("loading", 8, true, true);
    }

    double dt = context.getDeltaTime();
    timer += dt;

    if (timer >= DURATION) {
      context.removeEntity(self);
      return;
    }

    // 感染拡大処理 (一定間隔)
    if (timer - lastSpreadTime >= SPREAD_INTERVAL) {
      spreadPlague(context);
      lastSpreadTime = timer;
    }
  }

  private void spreadPlague(GameContext context) {
    double radiusSq = type.getRadius() * type.getRadius();

    // 範囲内の生存住民を取得
    List<Resident> targets = context.getResidentEntities()
        .filter(r -> r.getState() != ResidentState.DEAD)
        .filter(r -> r.getState() != ResidentState.AT_HOME) // 家にいる住民は対象外
        .filter(r -> r.getPosition().distanceSq(center) <= radiusSq)
        .collect(Collectors.toList());

    for (Resident resident : targets) {
      // デバフ付与（上書き）
      resident.addDebuff(DebuffType.PLAGUE, INFECTION_DURATION, INITIAL_INFECTION_LEVEL);

      // パニック状態へ遷移 (接触感染では発生しない、このActionからの直接感染のみ)
      if (resident.getState() != ResidentState.PANICKING) {
        resident.setState(ResidentState.PANICKING);
      }
    }
  }
}
