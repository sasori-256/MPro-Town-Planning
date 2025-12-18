package io.github.sasori_256.town_planning.gameobject;

import java.awt.geom.Point2D;
import java.awt.Point;

/**
 * カメラクラス
 * 引数は全てView基準?
 * 
 * @param cellWidth  アイソメトリックセルの幅
 * @param cellHeight アイソメトリックセルの高さ
 * @param offsetX    カメラのXオフセット
 * @param offsetY    カメラのYオフセット
 * @param center     カメラの中心点
 */
public class Camera {
  private int cellHeight;
  private int cellWidth;
  private int offsetX;
  private int offsetY;
  private Point2D.Double center;

  public Camera(int cellWidth, int cellHeight, Point2D.Double center) {
    this.cellWidth = cellWidth;
    this.cellHeight = cellHeight;
    this.offsetX = 0;
    this.offsetY = 0;
    this.center = center;
  }

  public int getCellHeight() {
    return cellHeight;
  }

  public int getCellWidth() {
    return cellWidth;
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
    double adjX = screenX - this.center.x - this.offsetX;
    double adjY = screenY - this.center.y * 0 - this.offsetY;
    double isoX = adjX / this.cellWidth + adjY / this.cellHeight - 1;
    double isoY = adjY / this.cellHeight - adjX / this.cellWidth - 1;
    return new Point2D.Double(isoX, isoY);
  }

  /**
   * アイソメトリック座標をスクリーン座標に変換する
   * 
   * @param isoX アイソメトリックのX座標
   * @param isoY アイソメトリックのY座標
   * @return スクリーン座標
   */
  public Point2D.Double isoToScreen(Point2D.Double isoPos) {
    double screenX = (this.cellWidth / 2) * (isoPos.x - isoPos.y - 1) + this.center.x + this.offsetX;
    double screenY = (this.cellHeight / 2) * (isoPos.x + isoPos.y) + this.center.y * 0 + this.offsetY;
    return new Point2D.Double(screenX, screenY);
  }
}
