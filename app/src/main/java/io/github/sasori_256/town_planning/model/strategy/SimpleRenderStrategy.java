package io.github.sasori_256.town_planning.model.strategy;

import io.github.sasori_256.town_planning.model.GameObject;
import io.github.sasori_256.town_planning.core.strategy.RenderStrategy;
import io.github.sasori_256.town_planning.model.BuildingType;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;

/**
 * 単純な図形として描画するStrategy。
 */
public class SimpleRenderStrategy implements RenderStrategy {
  private final Color color;
  private final String symbol;
  private final int size;

  public SimpleRenderStrategy(Color color, String symbol, int size) {
    this.color = color;
    this.symbol = symbol;
    this.size = size;
  }

  public static SimpleRenderStrategy fromBuildingType(BuildingType type) {
    return new SimpleRenderStrategy(type.getColor(), type.getSymbol(), 32); // 32x32 size
  }

  @Override
  public void render(Graphics2D g, GameObject self) {
    Point2D pos = self.getPosition();
    int x = (int) pos.getX() * 32; // グリッド座標 -> ピクセル座標変換 (仮: 1グリッド=32px)
    int y = (int) pos.getY() * 32;

    // 本来はCameraクラスなどを通して座標変換すべきだが、一旦直書き

    g.setColor(color);
    g.fillRect(x, y, size, size);

    g.setColor(Color.BLACK);
    g.drawRect(x, y, size, size);

    if (symbol != null) {
      g.drawString(symbol, x + size / 4, y + size / 2);
    }
  }
}
