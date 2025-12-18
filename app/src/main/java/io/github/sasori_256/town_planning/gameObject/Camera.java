package io.github.sasori_256.town_planning.gameObject;
import java.awt.geom.Point2D;
import java.awt.Point;


public class Camera {
    private double scale;
    private int cellWidth;
    private int cellHeight;
    private int offsetX;
    private int offsetY;
    private Point2D.Double center;

    /**
     * defaultScaleが1のとき、セルの幅が64ピクセル、高さが32ピクセルになる。
     * @param defaultScale
     * @param center
     */
    public Camera(double defaultScale, Point2D.Double center) {
        this.scale = defaultScale;
        this.cellWidth = (int)(64 * defaultScale);
        this.cellHeight = (int)(32 * defaultScale);
        this.offsetX = 0;
        this.offsetY = 0;
        this.center = center;
    }

    public double getScale() {
        return scale;
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

    public void setScale(double scale) {
        this.scale = scale;
        this.cellWidth = (int)(64 * scale);
        this.cellHeight = (int)(32 * scale);
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
     * @param screenPos スクリーン座標
     * @return アイソメトリック座標
     */
    public Point2D.Double screenToIso(Point2D.Double screenPos){
        double adjX = screenPos.x - this.center.x - this.offsetX;
        double adjY = screenPos.y - this.offsetY; // Iso座標の (0,0)はX軸上の中央にあるため、center.yは引かない
        double isoX = (adjX / this.cellWidth + adjY / this.cellHeight) - 1; //原点を調整するために-1を引く
        double isoY = (adjY / this.cellHeight - adjX / this.cellWidth) - 1; //原点を調整するために-1を引く
        return new Point2D.Double(isoX, isoY);
    }

    /**
     * アイソメトリック座標をスクリーン座標に変換する
     * @param isoPos アイソメトリック座標
     * @return スクリーン座標
     */
    public Point.Double isoToScreen(Point2D.Double isoPos){
        double screenX = (isoPos.x - isoPos.y-1) * (this.cellWidth / 2.0) + this.center.x+ this.offsetX;
        double screenY = (isoPos.x + isoPos.y) * (this.cellHeight / 2.0) + this.offsetY;
        return new Point.Double(screenX, screenY);
    }

}
