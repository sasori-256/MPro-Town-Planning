package io.github.sasori_256.town_planning.entity.resident.strategy;

import io.github.sasori_256.town_planning.common.core.strategy.UpdateStrategy;
import io.github.sasori_256.town_planning.common.event.events.ResidentDiedEvent;
import io.github.sasori_256.town_planning.entity.model.BaseGameEntity;
import io.github.sasori_256.town_planning.entity.model.GameContext;
import io.github.sasori_256.town_planning.entity.model.GameEffect;
import io.github.sasori_256.town_planning.entity.resident.Resident;
import io.github.sasori_256.town_planning.entity.resident.ResidentState;

/**
 * 住民のライフサイクル（加齢、死亡）を管理するEffect。
 */
public class ResidentLifeCycleEffect implements UpdateStrategy, GameEffect {
  @Override
  public void update(GameContext context, BaseGameEntity self) {
    if (!(self instanceof Resident)) {
      return; // 住民オブジェクトでなければ無視
    }
    Resident resident = (Resident) self;
    // 状態取得
    ResidentState state = resident.getState();
    if (state == ResidentState.DEAD) { // equals("dead")からEnum比較に変更
      return; // 生きてなければ加齢しない（死体処理は別途）
    }

    double dt = context.getDeltaTime();

    // 年齢取得と加齢
    double currentAge = resident.getAge();

    double agingRate = 1.0 / 10.0; // 1秒で0.1歳
    double newAge = currentAge + (dt * agingRate);

    resident.setAge(newAge);

    // 寿命チェック
    double maxAge = resident.getMaxAge();
    if (newAge >= maxAge) {
      die(context, self);
    }
  }

  private void die(GameContext context, BaseGameEntity self) {
    Resident resident = (Resident) self;
    resident.setState(ResidentState.DEAD);

    // 死亡イベント発行
    context.getEventBus().publish(new ResidentDiedEvent(resident));
  }

  /**
   * 住民のライフサイクルを進める。
   * UpdateStrategy/GameEffect両方に対応するため、update() に委譲する。
   *
   * @param context ゲーム内の環境情報
   * @param self    対象エンティティ
   */
  @Override
  public void execute(GameContext context, BaseGameEntity self) {
    update(context, self);
  }
}
