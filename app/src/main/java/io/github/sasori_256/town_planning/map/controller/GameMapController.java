package io.github.sasori_256.town_planning.map.controller;

import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

import javax.swing.SwingUtilities;

import io.github.sasori_256.town_planning.entity.Camera;
import io.github.sasori_256.town_planning.entity.model.BaseGameEntity;
import io.github.sasori_256.town_planning.map.controller.handler.*;

/**
 * マップ操作に関する入力イベントを扱うコントローラ。
 */
public class GameMapController implements MouseListener, MouseMotionListener, KeyListener, MouseWheelListener {
  private Camera camera;
  private final ReadWriteLock stateLock;
  private BiConsumer<Point2D.Double, Function<Point2D.Double, ? extends BaseGameEntity>> actionOnClick;
  private Function<Point2D.Double, ? extends BaseGameEntity> selectedEntityGenerator;
  private Point previousMiddleMousePos;

  /**
   * マップコントローラを生成する。
   *
   * @param camera    カメラ
   * @param stateLock 状態ロック
   */
  public GameMapController(Camera camera, ReadWriteLock stateLock) {
    this.camera = camera;
    this.stateLock = stateLock;
    this.actionOnClick = new ClickGameMapHandler();
    this.selectedEntityGenerator = (point) -> null;
  }

  /**
   * GameMap上でのクリック時の動作を設定する
   *
   * @param action
   */
  public void setActionOnClick(BiConsumer<Point2D.Double, Function<Point2D.Double, ? extends BaseGameEntity>> action) {
    this.actionOnClick = action;
  }

  /**
   * 選択中のエンティティ生成関数を設定する。
   *
   * @param entityGenerator 生成関数
   */
  public void setSelectedEntityGenerator(Function<Point2D.Double, ? extends BaseGameEntity> entityGenerator) {
    this.selectedEntityGenerator = entityGenerator;
  }

  /**
   * クリック時の処理を行う。
   *
   * @param e マウスイベント
   */
  @Override
  public void mouseClicked(MouseEvent e) {
    if (SwingUtilities.isLeftMouseButton(e)) {
      Point2D.Double isoPoint = camera.screenToIso(new Point2D.Double(e.getX(), e.getY()));
      Lock writeLock = stateLock.writeLock();
      writeLock.lock();
      try {
        actionOnClick.accept(isoPoint, selectedEntityGenerator);
      } finally {
        writeLock.unlock();
      }
    }
  }

  /**
   * マウス押下時の処理を行う。
   *
   * @param e マウスイベント
   */
  @Override
  public void mousePressed(MouseEvent e) {
    if (SwingUtilities.isMiddleMouseButton(e)) {
      previousMiddleMousePos = new Point(e.getX(), e.getY());
    }
  }

  /**
   * マウス解放時の処理を行う。
   *
   * @param e マウスイベント
   */
  @Override
  public void mouseReleased(MouseEvent e) {
    if (SwingUtilities.isMiddleMouseButton(e)) {
      previousMiddleMousePos = null;
    }
  }

  /**
   * マウスドラッグ時の処理を行う。
   *
   * @param e マウスイベント
   */
  @Override
  public void mouseDragged(MouseEvent e) {
    if (SwingUtilities.isMiddleMouseButton(e) && previousMiddleMousePos != null) {
      int dx = e.getX() - previousMiddleMousePos.x;
      int dy = e.getY() - previousMiddleMousePos.y;

      camera.pan(dx, dy);
      previousMiddleMousePos = new Point(e.getX(), e.getY());
    }
  }

  /**
   * マウスホイール操作時の処理を行う。
   *
   * @param e マウスイベント
   */
  @Override
  public void mouseWheelMoved(MouseWheelEvent e) {
    int notches = e.getWheelRotation();
    if (notches < 0) {
      camera.zoomIn();
    } else {
      camera.zoomOut();
    }
  }

  /**
   * マウスが領域に入った際の処理を行う。
   *
   * @param e マウスイベント
   */
  @Override
  public void mouseEntered(MouseEvent e) {
  }

  /**
   * マウスが領域から出た際の処理を行う。
   *
   * @param e マウスイベント
   */
  @Override
  public void mouseExited(MouseEvent e) {
  }

  /**
   * マウス移動時の処理を行う。
   *
   * @param e マウスイベント
   */
  @Override
  public void mouseMoved(MouseEvent e) {
  }

  /**
   * キー押下時の処理を行う。
   *
   * @param e キーイベント
   */
  @Override
  public void keyPressed(KeyEvent e) {
    int k = e.getKeyCode();
    switch (k) {
      case KeyEvent.VK_W:
        camera.moveUp();
        break;
      case KeyEvent.VK_S:
        camera.moveDown();
        break;
      case KeyEvent.VK_A:
        camera.moveLeft();
        break;
      case KeyEvent.VK_D:
        camera.moveRight();
        break;
      default:
        break;
    }
  }

  /**
   * キー解放時の処理を行う。
   *
   * @param e キーイベント
   */
  @Override
  public void keyReleased(KeyEvent e) {
  }

  /**
   * キー入力時の処理を行う。
   *
   * @param e キーイベント
   */
  @Override
  public void keyTyped(KeyEvent e) {
  }

}
