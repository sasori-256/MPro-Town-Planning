package io.github.sasori_256.town_planning.entity;

import java.awt.geom.Point2D;

import io.github.sasori_256.town_planning.common.event.events.MapUpdatedEvent;
import io.github.sasori_256.town_planning.common.event.EventBus;

/**
 * カメラクラス
 * 引数は全てView基準?
 * 
 */
public class Camera {
  private double scale;
  private int cellHeight;
  private int cellWidth;
  private int offsetX;
  private int offsetY;
  private Point2D.Double center;
  private final EventBus eventBus;

  /**
   * defaultScaleが1のとき、セルの幅が64ピクセル、高さが32ピクセルになる。
   * 
   * @param defaultScale
   * @param center
   */
  public Camera(double defaultScale, Point2D.Double center, EventBus eventBus) {
    this.scale = defaultScale;
    this.cellHeight = (int) (32 * defaultScale);
    this.cellWidth = (int) (32 * 2 * defaultScale);
    this.offsetX = 0;
    this.offsetY = 0;
    this.center = center;
    this.eventBus = eventBus;
  }

  public double getScale() {
    return scale;
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

  public void setScale(double scale) {
    this.scale = scale;
    this.cellHeight = (int) (32 * scale);
    this.cellWidth = (int) (32 * 2 * scale);
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
   * @param screenPos スクリーン座標
   * @return アイソメトリック座標
   */
  public Point2D.Double screenToIso(Point2D.Double screenPos) {
    double adjX = screenPos.x - this.center.x - this.offsetX;
    double adjY = screenPos.y - this.center.y - this.offsetY; 
    double isoX = (adjX / this.cellWidth + adjY / this.cellHeight); 
    double isoY = (adjY / this.cellHeight - adjX / this.cellWidth);
    return new Point2D.Double(isoX, isoY);
  }

  /**
   * アイソメトリック座標をスクリーン座標に変換する
   * 
   * @param isoPos アイソメトリック座標
   * @return スクリーン座標
   */
  public Point2D.Double isoToScreen(Point2D.Double isoPos) {
    double screenX = (isoPos.x - isoPos.y) * (this.cellWidth / 2.0) + this.center.x + this.offsetX;
    double screenY = (isoPos.x + isoPos.y) * (this.cellHeight / 2.0) + this.center.y + this.offsetY;
    return new Point2D.Double(screenX, screenY);
  }

  public void pan(int dx, int dy) {
    this.offsetX += dx;
    this.offsetY += dy;
    eventBus.publish(new MapUpdatedEvent(new Point2D.Double(0, 0)));
  }

  // TODO: カメラ移動を滑らかにする
  public void moveUp() {
    pan(0, 10);
  }

  public void moveDown() {
    pan(0, -10);
  }

  public void moveLeft() {
    pan(10, 0);
  }

  public void moveRight() {
    pan(-10, 0);
  }
}
