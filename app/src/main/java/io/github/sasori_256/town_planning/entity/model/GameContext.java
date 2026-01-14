package io.github.sasori_256.town_planning.entity.model;

import io.github.sasori_256.town_planning.common.event.EventBus;
import io.github.sasori_256.town_planning.entity.building.Building;
import io.github.sasori_256.town_planning.entity.disaster.Disaster;
import io.github.sasori_256.town_planning.entity.resident.Resident;
import io.github.sasori_256.town_planning.map.model.GameMap;

// Streamとは
// Streamは、Java 8で導入されたjava.util.streamパッケージに属するクラスであり、
// コレクションや配列などのデータソースに対して、関数型プログラミングのスタイルで操作を行うためのAPIを提供します。
// Streamを使用することで、データのフィルタリング、変換、集約などの操作を簡潔かつ効率的に記述できます。
// Streamは、遅延評価を採用しており、必要なときにのみデータを処理します。
// これにより、大量のデータを扱う際にも効率的な処理が可能となります。
// また、Streamは並列処理をサポートしており、複数のスレッドを利用してデータの処理を高速化することができます。
// Streamは、主に以下の3つのステップで操作を行います。
// 1. データソースの生成: コレクションや配列からStreamを生成します。
// 2. 中間操作: フィルタリングやマッピングなどの操作を連鎖的に適用します。
// 3. 終端操作: 集約や収集などの最終的な操作を実行します。
// これにより、コードの可読性が向上し、複雑なデータ操作を簡潔に表現できるようになります。
import java.util.stream.Stream;

/**
 * ゲームの更新処理に必要なコンテキスト情報を提供するインターフェース。
 * DI (Dependency Injection) のような役割を果たす。
 */
public interface GameContext {
  /**
   * イベントバスを返す。
   *
   * @return イベントバス
   */
  EventBus getEventBus();

  /**
   * マップを返す。
   *
   * @return マップ
   */
  GameMap getMap();

  /**
   * 建物エンティティのストリームを返す。
   *
   * @return 建物エンティティのストリーム
   */
  Stream<Building> getBuildingEntities();

  /**
   * 住民エンティティのストリームを返す。
   *
   * @return 住民エンティティのストリーム
   */
  Stream<Resident> getResidentEntities();

  /**
   * 災害エンティティのストリームを返す。
   *
   * @return 災害エンティティのストリーム
   */
  Stream<Disaster> getDisasterEntities();

  /**
   * 前フレームからの経過時間(秒)を返す。
   *
   * @return 経過時間(秒)
   */
  double getDeltaTime(); // 前フレームからの経過時間（秒）

  /**
   * 現在のゲーム内日数を返す。
   */
  int getDay();

  /**
   * 1日の経過秒を返す。
   */
  double getTimeOfDaySeconds();

  /**
   * 1日の経過率(0.0-1.0)を返す。
   */
  double getTimeOfDayNormalized();

  /**
   * 1日の長さ(秒)を返す。
   */
  double getDayLengthSeconds();

  // Entity Lifecycle

  /**
   * エンティティを生成してゲームに追加する。
   *
   * @param entity 追加するエンティティ
   * @param <T>    エンティティ型
   */
  <T extends BaseGameEntity> void spawnEntity(T entity);

  /**
   * エンティティをゲームから削除する。
   *
   * @param entity 削除するエンティティ
   * @param <T>    エンティティ型
   */
  <T extends BaseGameEntity> void removeEntity(T entity);

}
