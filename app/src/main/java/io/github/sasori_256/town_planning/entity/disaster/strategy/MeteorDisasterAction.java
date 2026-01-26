package io.github.sasori_256.town_planning.entity.disaster.strategy;

import java.awt.geom.Point2D;

import io.github.sasori_256.town_planning.common.core.GameConfig;
import io.github.sasori_256.town_planning.common.event.EventBus;
import io.github.sasori_256.town_planning.common.event.events.DisasterOccurredEvent;
import io.github.sasori_256.town_planning.entity.disaster.Disaster;
import io.github.sasori_256.town_planning.entity.disaster.DisasterType;
import io.github.sasori_256.town_planning.entity.model.BaseGameEntity;
import io.github.sasori_256.town_planning.entity.model.GameAction;
import io.github.sasori_256.town_planning.entity.model.GameContext;

/**
 * 隕石などの単発災害のロジック。
 * 生成されてから一定時間後に着弾し、範囲ダメージを与える。
 */
public class MeteorDisasterAction implements GameAction {
  private static final String IMPACT_ANIMATION_NAME = "meteor_impact";
  private static final int IMPACT_ANIMATION_FPS = GameConfig.getDisasterMeteorAnimationFps();
  private static final double IMPACT_EFFECT_DURATION =
      GameConfig.getDisasterMeteorEffectDurationSeconds();
  private final EventBus eventBus = EventBus.getInstance();
  private final Point2D.Double targetPos;
  private final DisasterType type;
  private double timer;
  private final double impactTime;
  private boolean impacted;

  /**
   * 隕石災害アクションを生成する。
   *
   * @param targetPos 着弾目標位置
   * @param type      災害種別
   */
  public MeteorDisasterAction(Point2D.Double targetPos, DisasterType type) {
    this.targetPos = new Point2D.Double(targetPos.x, targetPos.y);
    this.type = type;
    this.timer = 0.0;
    this.impactTime = GameConfig.getDisasterMeteorImpactSeconds();
    this.impacted = false;
  }

  /** {@inheritDoc} */
  @Override
  public void execute(GameContext context, BaseGameEntity self) {
    if (!(self instanceof Disaster)) {
      return;
    }
    Disaster disaster = (Disaster) self;
    if (impacted) {
      // 着弾後の余韻（エフェクト消滅待ちなど）
      timer += context.getDeltaTime();
      if (timer > impactTime + IMPACT_EFFECT_DURATION) { // 着弾後1秒で消滅
        context.removeEntity(self);
      }
      return;
    }

    timer += context.getDeltaTime();

    // 移動 (空から降ってくる)
    double progress = Math.min(1.0, timer / impactTime);
    double targetX = targetPos.getX();
    double targetY = targetPos.getY();
    double startX = targetX - 1.0;
    double startY = targetY - 4.0; // 画面外上空
    double currentX = startX + (targetX - startX) * progress;
    double currentY = startY + (targetY - startY) * progress;
    self.setPosition(new Point2D.Double(currentX, currentY));

    if (timer >= impactTime) {
      impact(context, disaster);
    }
  }

  private void impact(GameContext context, Disaster disaster) {
    impacted = true;
    disaster.setPosition(targetPos);
    disaster.setAnimation(IMPACT_ANIMATION_NAME, IMPACT_ANIMATION_FPS, false, true);

    context.applyDisasterImpact(targetPos, type);

    eventBus.publish(new DisasterOccurredEvent(type));
  }
}
