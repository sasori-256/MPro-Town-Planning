package io.github.sasori_256.town_planning.entity;

import java.awt.geom.Point2D;

import io.github.sasori_256.town_planning.common.event.events.MapUpdatedEvent;
import io.github.sasori_256.town_planning.common.event.EventBus;

public class Camera {
  private double scale;
  private int cellHeight;
  private int cellWidth;
  private int offsetX;
  private int offsetY;
  private Point2D.Double isoOriginByScreen; // Iso座標系の原点をScreen座標系で表したときの位置
  private int mapWidth;
  private int mapHeight;
  private int screenWidth;
  private int screenHeight;
  private int zoomLevel;
  private static final double ZOOM_STEP = 0.25;
  private static final int MIN_ZOOM_LEVEL = 1; // 0.25倍
  private static final int MAX_ZOOM_LEVEL = 12; // 3.0倍
  private final EventBus eventBus;

  /**
   * defaultScaleが1のとき、セルの幅が64ピクセル、高さが32ピクセルになる。
   * 
   * @param defaultScale 初期スケール
   * @param screenWidth  画面の幅(実際のピクセル数)
   * @param screenHeight 画面の高さ(実際のピクセル数)
   * @param mapWidth     マップの幅（セル数）
   * @param mapHeight    マップの高さ（セル数）
   * @param eventBus     イベントバス
   */
  public Camera(double defaultScale, int screenWidth, int screenHeight, int mapWidth, int mapHeight, EventBus eventBus) {
    this.scale = defaultScale;
    this.cellHeight = (int) (32 * defaultScale);
    this.cellWidth = (int) (32 * 2 * defaultScale);
    this.offsetX = 0;
    this.offsetY = 0;
    this.mapWidth = mapWidth;
    this.mapHeight = mapHeight;
    this.screenWidth = screenWidth;
    this.screenHeight = screenHeight;
    this.zoomLevel = (int) (defaultScale / ZOOM_STEP);
    this.updateOrigin(screenWidth, screenHeight);
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

  public Point2D.Double getIsoOriginByScreen() {
    return isoOriginByScreen;
  }

  public void setOffset(int offsetX, int offsetY) {
    this.offsetX = offsetX;
    this.offsetY = offsetY;
  }

  public void setScreenSize(int screenWidth, int screenHeight) {
    this.screenWidth = screenWidth;
    this.screenHeight = screenHeight;
    applyZoomLevel();
    //画面外に行ってしまったときの対処
    Point2D.Double centerIso = screenToIso(new Point2D.Double(screenWidth / 2.0, screenHeight / 2.0));
    int clampedX = (int) Math.clamp(centerIso.x, 0, this.mapWidth - 1);
    int clampedY = (int) Math.clamp(centerIso.y, 0, this.mapHeight - 1);
    if (clampedX != (int) centerIso.x || clampedY != (int) centerIso.y){
      this.offsetX = 0;
      this.offsetY = 0;
      Point2D.Double clampedCenterScreen = isoToScreen(new Point2D.Double(clampedX, clampedY));
      this.offsetX = (int) ((screenWidth / 2.0) - clampedCenterScreen.x);
      this.offsetY = (int) ((screenHeight / 2.0) - clampedCenterScreen.y);
      applyZoomLevel();
    }
  }

  public void applyZoomLevel() {
    Point2D.Double centerScreen = new Point2D.Double(this.screenWidth / 2.0, this.screenHeight / 2.0);
    Point2D.Double centerIso = screenToIso(centerScreen);

    this.scale = zoomLevel * ZOOM_STEP;
    this.cellHeight = (int) (32 * this.scale);
    this.cellWidth = (int) (32 * 2 * this.scale);
    updateOrigin(this.screenWidth, this.screenHeight);

    this.offsetX = 0;
    this.offsetY = 0;
    Point2D.Double newCenterScreen = isoToScreen(centerIso);
    this.offsetX = (int) (centerScreen.x - newCenterScreen.x);
    this.offsetY = (int) (centerScreen.y - newCenterScreen.y);
  }

  public void setScale(double scale) {
    this.zoomLevel = Math.clamp((int) Math.round(scale / ZOOM_STEP), MIN_ZOOM_LEVEL, MAX_ZOOM_LEVEL);
    applyZoomLevel();
  }

  /**
   * Iso座標系の原点をscreen座標系で表したときの位置を更新する
   * 
   * @param screenWidth
   * @param screenHeight
   */
  public void updateOrigin(int screenWidth, int screenHeight) {
    double centerIsoX = (this.mapWidth - 1) / 2.0;
    double centerIsoY = (this.mapHeight - 1) / 2.0;
    double centerScreenX = (centerIsoX - centerIsoY) * (this.cellWidth / 2.0);
    double centerScreenY = (centerIsoX + centerIsoY) * (this.cellHeight / 2.0);
    this.isoOriginByScreen = new Point2D.Double(screenWidth / 2 - centerScreenX, screenHeight / 2 - centerScreenY);
    // System.out.println("Screen Origin Updated: (" + this.isoOriginByScreen.x + ", " + this.isoOriginByScreen.y + ")");
  }

  /**
   * スクリーン座標をアイソメトリック座標に変換する
   * 
   * @param screenPos スクリーン座標
   * @return アイソメトリック座標
   */
  public Point2D.Double screenToIso(Point2D.Double screenPos) {
    double adjX = screenPos.x - this.isoOriginByScreen.x - this.offsetX;
    double adjY = screenPos.y - this.isoOriginByScreen.y - this.offsetY;
    double isoX = adjX / this.cellWidth + adjY / this.cellHeight;
    double isoY = adjY / this.cellHeight - adjX / this.cellWidth;
    return new Point2D.Double(isoX, isoY);
  }

  /**
   * アイソメトリック座標をスクリーン座標に変換する
   * 
   * @param isoPos アイソメトリック座標
   * @return スクリーン座標
   */
  public Point2D.Double isoToScreen(Point2D.Double isoPos) {
    double screenX = (isoPos.x - isoPos.y) * (this.cellWidth / 2.0) + this.isoOriginByScreen.x + this.offsetX;
    double screenY = (isoPos.x + isoPos.y) * (this.cellHeight / 2.0) + this.isoOriginByScreen.y + this.offsetY;
    return new Point2D.Double(screenX, screenY);
  }
  
  private boolean isValidOffset(int dx, int dy) {
    Point2D.Double centerIso = screenToIso(new Point2D.Double(this.screenWidth / 2.0 - dx, this.screenHeight / 2.0 - dy));
    return 0 <= centerIso.x && centerIso.x <= this.mapWidth - 1 && 0 <= centerIso.y && centerIso.y <= this.mapHeight - 1;
  }

  public void pan(int dx, int dy) {
    if (isValidOffset(dx, dy)){
      this.offsetX += dx;
      this.offsetY += dy;
      // System.out.println("Panned to Offset: (" + this.offsetX + ", " + this.offsetY + ")");
      eventBus.publish(new MapUpdatedEvent(new Point2D.Double(0, 0)));
    }
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

  public void zoomIn() {
    if (this.zoomLevel < MAX_ZOOM_LEVEL){
      this.zoomLevel += 1;
      applyZoomLevel();
      // System.out.println("Zoomed In: New Scale = " + this.scale);
      eventBus.publish(new MapUpdatedEvent(new Point2D.Double(0, 0)));
    }
  }

  public void zoomOut() {
    if (this.zoomLevel > MIN_ZOOM_LEVEL){
      this.zoomLevel -= 1;
      applyZoomLevel();
      // System.out.println("Zoomed Out: New Scale = " + this.scale);
      eventBus.publish(new MapUpdatedEvent(new Point2D.Double(0, 0)));
    }
  }
}
