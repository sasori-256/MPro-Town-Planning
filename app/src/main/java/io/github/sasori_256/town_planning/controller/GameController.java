package io.github.sasori_256.town_planning.controller;

import io.github.sasori_256.town_planning.model.GameModel;
import java.awt.geom.Point2D;

/**
 * 入力を処理し、ゲームモデルを制御するコントローラークラス。
 * ユーザーの操作（マウスクリック、キー入力）をモデルのアクションに変換する。
 */
public class GameController {
  private final GameModel gameModel;

  public GameController(GameModel gameModel) {
    this.gameModel = gameModel;
  }

  public void onMapClicked(Point2D position) {
    // マップがクリックされたときの処理
    // 例: 建物建設、エンティティ選択など
    System.out.println("Map clicked at: " + position);
    // 仮の実装: クリックした位置にソウルを追加してみる
    gameModel.addSouls(10);
  }
  
  public void startGame() {
      // 描画コールバックはApp側で設定するか、あるいはここでViewを渡してもらうか設計次第だが
      // ここでは一旦nullを渡しておく（まだViewがないため）。
      // 実際にはViewの再描画メソッドを渡す必要がある。
      // gameModel.startGameLoop(() -> view.repaint());
  }
}