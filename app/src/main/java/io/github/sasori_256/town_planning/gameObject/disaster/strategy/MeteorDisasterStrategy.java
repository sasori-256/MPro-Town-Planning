package io.github.sasori_256.town_planning.gameObject.disaster.strategy;

import java.awt.geom.Point2D;
import java.util.List;
import java.util.stream.Collectors;

import io.github.sasori_256.town_planning.common.core.strategy.UpdateStrategy;
import io.github.sasori_256.town_planning.common.event.EventType;
import io.github.sasori_256.town_planning.gameObject.disaster.DisasterType;
import io.github.sasori_256.town_planning.gameObject.model.BaseGameEntity;
import io.github.sasori_256.town_planning.gameObject.model.GameContext;
import io.github.sasori_256.town_planning.gameObject.resident.ResidentObject;
import io.github.sasori_256.town_planning.gameObject.resident.ResidentState;

/**
 * 隕石などの単発災害のロジック。
 * 生成されてから一定時間後に着弾し、範囲ダメージを与える。
 */
public class MeteorDisasterStrategy implements UpdateStrategy {
  private final DisasterType type;
  private final Point2D.Double targetPos;
  private double timer = 0;
  private final double impactTime = 2.0; // 2秒後に着弾
  private boolean impacted = false;

  public MeteorDisasterStrategy(DisasterType type, Point2D.Double targetPos) {
    this.type = type;
    this.targetPos = targetPos;
  }

  @Override
  public void update(GameContext context, BaseGameEntity self) {
    if (impacted) {
      // 着弾後の余韻（エフェクト消滅待ちなど）
      timer += context.getDeltaTime();
      if (timer > impactTime + 1.0) { // 着弾後1秒で消滅
        context.removeEntity(self);
      }
      return;
    }

    timer += context.getDeltaTime();

    // 移動アニメーション (空から降ってくる)
    double progress = timer / impactTime;
    double startY = -10.0; // 画面外上空
    double currentY = startY + (targetPos.getY() - startY) * progress;
    self.setPosition(new Point2D.Double(targetPos.getX(), currentY));

    if (timer >= impactTime) {
      impact(context, self);
    }
  }

  // TODO: ResidentObject用に直す
  private void impact(GameContext context, BaseGameEntity self) {
    impacted = true;
    self.setPosition(targetPos);

    // 範囲内のエンティティを検索
    List<BaseGameEntity> targets = context.getEntities()
        .filter(e -> e != null && e.getPosition().distance(targetPos) <= type.getRadius())
        .collect(Collectors.toList());

    for (BaseGameEntity target : targets) {
      // 住民への処理
      if (target instanceof ResidentObject) {
        ResidentObject resident = (ResidentObject) target;
        if (resident.getState() != ResidentState.DEAD) {
          // 即死させる
          resident.setState(ResidentState.DEAD);
          context.getEventBus().publish(EventType.RESIDENT_DIED, resident);

          // 災害による死亡は魂を即時回収できるボーナスがあるかも？
          // ここでは単純に死亡させるのみとし、回収は別途クリック等で行うか、
          // あるいは「刈り取る」災害ならここで回収イベントを投げる。
          // 今回は「隕石で死ぬ -> 死体になる」だけにする。
        }
      }

      // 建物への処理 (属性チェックなどで判定)
      // String buildingType = target.getAttribute("building_type");
      // if (buildingType != null) { ... }
    }

    context.getEventBus().publish(EventType.DISASTER_OCCURRED, type);
  }

  // @Override
  // public void render(Graphics2D g, GameObject self) {
  // Point2D pos = self.getPosition();
  // int x = (int) (pos.getX() * 32);
  // int y = (int) (pos.getY() * 32);
  // int radiusPx = type.getRadius() * 32;

  // if (!impacted) {
  // // 落下中の隕石
  // g.setColor(Color.RED);
  // g.fillOval(x - 10, y - 10, 20, 20);

  // // 落下地点予測
  // Point2D target = this.targetPos;
  // int tx = (int) (target.getX() * 32);
  // int ty = (int) (target.getY() * 32);
  // g.setColor(new Color(255, 0, 0, 50));
  // g.drawOval(tx - radiusPx, ty - radiusPx, radiusPx * 2, radiusPx * 2);
  // } else {
  // // 爆発エフェクト
  // g.setColor(new Color(255, 100, 0, 150)); // Orange
  // g.fillOval(x - radiusPx, y - radiusPx, radiusPx * 2, radiusPx * 2);
  // }
  // }
}
