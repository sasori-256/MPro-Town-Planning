package io.github.sasori_256.town_planning.model.strategy.resident;

import io.github.sasori_256.town_planning.model.GameObject;
import io.github.sasori_256.town_planning.core.strategy.RenderStrategy;
import io.github.sasori_256.town_planning.model.entity.ResidentObject;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

/**
 * 住民の状態に応じて見た目を変える描画Strategy。
 * 生存時、死亡時で色や形を変える。
 */
public class ResidentRenderStrategy implements RenderStrategy {
  private final RenderStrategy aliveStrategy;

  public ResidentRenderStrategy(RenderStrategy aliveStrategy) {
    this.aliveStrategy = aliveStrategy;
  }

  @Override
  public void render(Graphics2D g, GameObject self) {
    ResidentObject resident = (ResidentObject) self;
    String state = resident.getState();

    if (state.equals("dead")) {
      // 死体の描画
      Point2D pos = self.getPosition();
      int x = (int) (pos.getX() * 32);
      int y = (int) (pos.getY() * 32);

      g.setColor(Color.GRAY);
      g.fillOval(x + 4, y + 8, 24, 16); // 横たわっているような楕円
      g.setColor(Color.BLACK);
      g.drawString("†", x + 10, y + 20);
    } else {
      // 生存時の描画（委譲）
      aliveStrategy.render(g, self);
    }
  }
}
