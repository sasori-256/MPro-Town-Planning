package io.github.sasori_256.town_planning.entity.disaster.strategy;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.github.sasori_256.town_planning.common.event.events.DisasterOccurredEvent;
import io.github.sasori_256.town_planning.common.event.events.ResidentDiedEvent;
import io.github.sasori_256.town_planning.entity.disaster.DisasterType;
import io.github.sasori_256.town_planning.entity.model.BaseGameEntity;
import io.github.sasori_256.town_planning.entity.model.GameAction;
import io.github.sasori_256.town_planning.entity.model.GameContext;
import io.github.sasori_256.town_planning.entity.resident.Resident;
import io.github.sasori_256.town_planning.entity.resident.ResidentState;

/**
 * 隕石などの単発災害のロジック。
 * 生成されてから一定時間後に着弾し、範囲ダメージを与える。
 */
public class MeteorDisasterAction implements GameAction {
  private final Point2D.Double targetPos;
  private final DisasterType type;
  private double timer;
  private final double impactTime;
  private boolean impacted;

  public MeteorDisasterAction(Point2D.Double targetPos, DisasterType type) {
    this.targetPos = new Point2D.Double(targetPos.x, targetPos.y);
    this.type = type;
    this.timer = 0.0;
    this.impactTime = 2.0;
    this.impacted = false;
  }

  @Override
  public void execute(GameContext context, BaseGameEntity self) {
    if (impacted) {
      // 着弾後の余韻（エフェクト消滅待ちなど）
      timer += context.getDeltaTime();
      if (timer > impactTime + 1.0) { // 着弾後1秒で消滅
        context.removeEntity(self);
      }
      return;
    }

    timer += context.getDeltaTime();

    // 移動 (空から降ってくる)
    double progress = Math.min(1.0, timer / impactTime);
    double startY = -10.0; // 画面外上空
    double currentY = startY + (targetPos.getY() - startY) * progress;
    self.setPosition(new Point2D.Double(targetPos.getX(), currentY));

    if (timer >= impactTime) {
      impact(context, self);
    }
  }

  private void impact(GameContext context, BaseGameEntity self) {
    impacted = true;
    self.setPosition(targetPos);

    // 範囲内のエンティティを検索
    // getEntities() がなくなったため、ResidentとBuildingをそれぞれ取得して結合
    Stream<BaseGameEntity> allEntities = Stream.concat(
        context.getResidentEntities(),
        context.getBuildingEntities());

    List<BaseGameEntity> targets = allEntities
        .filter(e -> e != null && e.getPosition().distance(targetPos) <= type.getRadius())
        .collect(Collectors.toList());

    for (BaseGameEntity target : targets) {
      // 住民への処理
      if (target instanceof Resident) {
        Resident resident = (Resident) target;
        if (resident.getState() != ResidentState.DEAD) {
          // 即死させる
          resident.setState(ResidentState.DEAD);
          // ResidentDiedEventを発行
          context.getEventBus().publish(new ResidentDiedEvent(resident));
        }
      }

      // 建物への処理 (属性チェックなどで判定)
      // String buildingType = target.getAttribute("building_type");
      // if (buildingType != null) { ... }
    }

    context.getEventBus().publish(new DisasterOccurredEvent(type));
  }
}
