package io.github.sasori_256.town_planning.model.strategy.resident;

import io.github.sasori_256.town_planning.model.GameContext;
import io.github.sasori_256.town_planning.model.GameObject;
import io.github.sasori_256.town_planning.core.strategy.UpdateStrategy;
import io.github.sasori_256.town_planning.event.EventType;
import io.github.sasori_256.town_planning.model.entity.ResidentObject;

/**
 * 住民のライフサイクル（加齢、死亡）を管理するStrategy。
 */
public class ResidentLifeCycleStrategy implements UpdateStrategy {

  @Override
  public void update(GameContext context, GameObject self) {
    if (!(self instanceof ResidentObject)) {
      return; // 住民オブジェクトでなければ無視
    }
    ResidentObject resident = (ResidentObject) self;
    // 状態取得
    String state = resident.getState();
    if (state.equals("dead")) {
      return; // 生きてなければ加齢しない（死体処理は別途）
    }

    double dt = context.getDeltaTime();

    // 年齢取得と加齢
    Double currentAge = resident.getAge();
    if (currentAge == null)
      currentAge = 0.0;

    // 1日 = 1歳 とする設定（仮）
    // GameContextから1日の長さを取得できないため、dtをそのまま加算し、
    // GameModel側のDay換算に依存するか、ここで独自に計算するか。
    // ここではシンプルに dt を時間として加算する。
    // バランス調整: 10秒で1日(GameModel設定) -> 10秒で1歳と仮定
    double agingRate = 1.0 / 10.0; // 1秒で0.1歳
    double newAge = currentAge + (dt * agingRate);

    resident.addAge(newAge);

    // 寿命チェック
    Double maxAge = resident.getMaxAge();
    if (maxAge != null && newAge >= maxAge) {
      die(context, self);
    }
  }

  private void die(GameContext context, GameObject self) {
    ResidentObject resident = (ResidentObject) self;
    resident.setState("dead");

    // 死亡イベント発行 (ログ表示や効果音用)
    context.getEventBus().publish(EventType.RESIDENT_DIED, self);

    // 見た目を変えるためにRenderStrategyを変更するロジックなどをここに挟むことも可能
    // 例: self.setRenderStrategy(new DeadBodyRenderStrategy());

    // 一旦、ここではシンプルに「死んだら即座に魂になる」自動回収ロジックにするか、
    // あるいは「死体として残り、クリックで回収」にするか。
    // GDDには「住人の命を刈り取り...」とあるので、能動的あるいは災害で死ぬ。
    // 自然死の場合は自動回収でもよいかもしれない。

    // 仮実装: 自然死は即座に消滅し、少量の魂を還元する
    // (GameModelへのキャストが必要なのが設計上の課題だが、EventBusで解決する)
    // context.getEventBus().publish(EventType.SOUL_HARVESTED, 10);
    // context.destroyEntity(self);

    // 今回は「死体」状態のまま残すことにする。
  }
}
