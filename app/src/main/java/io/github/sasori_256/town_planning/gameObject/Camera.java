package io.github.sasori_256.town_planning.gameObject;

import java.awt.geom.Point2D;
import java.awt.Point;

public class Camera {
  private int cellWidth;
  private int cellHeight;
  private int offsetX;
  private int offsetY;
  private Point2D.Double center;

  public Camera(int cellWidth, int cellHeight) {
    this.cellWidth = cellWidth;
    this.cellHeight = cellHeight;
    this.offsetX = 0;
    this.offsetY = 0;
  }

  public int getCellWidth() {
    return cellWidth;
  }

  public int getCellHeight() {
    return cellHeight;
  }

  public int getOffsetX() {
    return offsetX;
  }

  public int getOffsetY() {
    return offsetY;
  }

  public Point2D.Double getCenter() {
    return center;
  }

  public void setOffset(int offsetX, int offsetY) {
    this.offsetX = offsetX;
    this.offsetY = offsetY;
  }

  public void setCenter(Point2D.Double center) {
    this.center = center;
  }

  /**
   * スクリーン座標をアイソメトリック座標に変換する
   * 
   * @param screenX スクリーンのX座標
   * @param screenY スクリーンのY座標
   * @return アイソメトリック座標
   */
  public Point2D.Double screenToIso(int screenX, int screenY) {
    double adjX = screenX + this.center.x - this.offsetX;
    double adjY = screenY + this.center.y - this.offsetY;
    double isoX = adjX / this.cellWidth + adjY / this.cellHeight;
    double isoY = adjY / this.cellHeight - adjX / this.cellWidth;
    return new Point2D.Double(isoX, isoY);
  }

  /**
   * アイソメトリック座標をスクリーン座標に変換する
   * 
   * @param isoX アイソメトリックのX座標
   * @param isoY アイソメトリックのY座標
   * @return スクリーン座標
   */
  public Point isoToScreen(double isoX, double isoY) {
    double screenX = (isoX - isoY) * (this.cellWidth / 2.0) + this.offsetX;
    double screenY = (isoX + isoY) * (this.cellHeight / 2.0) + this.offsetY;
    return new Point((int) screenX, (int) screenY);
  }

}
