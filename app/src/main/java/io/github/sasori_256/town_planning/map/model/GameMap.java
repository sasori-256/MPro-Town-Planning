package io.github.sasori_256.town_planning.map.model;

import java.awt.geom.Point2D;

import io.github.sasori_256.town_planning.common.event.EventBus;
import io.github.sasori_256.town_planning.common.event.events.MapUpdatedEvent;
import io.github.sasori_256.town_planning.entity.building.Building;
import io.github.sasori_256.town_planning.entity.building.BuildingType;

/**
 * ゲーム内のマップ状態を保持する実装。
 */
public class GameMap implements MapContext {
  private final int width;
  private final int height;
  private final MapCell[][] cells;
  private final EventBus eventBus;

  /**
   * マップを生成する。
   *
   * @param width    横幅(セル数)
   * @param height   縦幅(セル数)
   * @param eventBus イベントバス
   * @param seed     シード値
   */
  public GameMap(int width, int height, long seed, EventBus eventBus) {
    this.width = width;
    this.height = height;
    this.eventBus = eventBus;
    this.cells = new MapCell[height][width];
    GenerateMapTerrain(seed); // マップの地形を生成
    StylizeMapEdges(); // 地形の境界を整える
  }

  /**
   * マップの地形を生成する。
   *
   * @param seed シード値
   */
  private void GenerateMapTerrain(long seed) {
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        // 0以上1以下に正規化されたノイズ値を取得
        double noiseValue = (ImprovedNoise.noise(
            (x + seed % 1000) / 10.0,
            (y + seed % 1000) / 10.0,
            seed / 1000.0) + 1) / 2.0;
        // マップ端付近では高さを下げ、その値を元に地形タイプを決定
        double altitude = noiseValue * Math.min(
            1.0,
            Math.min(
                x < width / 2 ? (double) x / (width / 10) : (double) (width - x - 1) / (width / 10),
                y < height / 2 ? (double) y / (height / 10) : (double) (height - y - 1) / (height / 10)));
        TerrainType terrainType;
        if (altitude < 0.4) {
          terrainType = TerrainType.WATER;
        } else if (altitude < 0.7) {
          terrainType = TerrainType.GRASS;
        } else {
          terrainType = TerrainType.MOUNTAIN;
        }
        cells[y][x] = new MapCell(new Point2D.Double(x, y), terrainType);
      }
    }
  }

  /**
   * マップの地形の境界を整える。
   */
  private void StylizeMapEdges() {
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        StylizeCellEdge(x, y);
      }
    }
  }

  /**
   * 指定セルの地形の境界を整える。
   *
   * @param x セルのX座標
   * @param y セルのY座標
   */
  private void StylizeCellEdge(int x, int y) {
    String target = cells[y][x].getTerrain().getKind();
    String collider = "";
    if(target.equals(TerrainType.MOUNTAIN.getKind())){
      collider = TerrainType.WATER.getKind(); // 山と海の境界は山が削れる
    } else if(target.equals(TerrainType.GRASS.getKind())){
      collider = TerrainType.MOUNTAIN.getKind(); // 草原と山の境界は草原が削れる
    } else if(target.equals(TerrainType.WATER.getKind())){
      collider = TerrainType.GRASS.getKind(); // 海と草原の境界は海が削れる
    }
    // 一旦海のみ境界処理を行う
    if(!target.equals(TerrainType.WATER.getKind())){
      return;
    }
    /*
     * 隣接する8方向の地形を取得 直行表示(アイソメトリックでない)なら以下のようになる
     * あ い う
     * き ☆ え
     * く か お
     */
    String Terrain_S = (x + 1 < width && y + 1 < height) ? cells[y + 1][x + 1].getTerrain().getKind()
        : TerrainType.WATER.getKind(); // 下方向の地形//：お
    boolean S = (Terrain_S.equals(collider));
    String Terrain_E = (x + 1 < width && y - 1 >= 0) ? cells[y - 1][x + 1].getTerrain().getKind()
        : TerrainType.WATER.getKind(); // 右方向の地形：う
    boolean E = (Terrain_E.equals(collider));
    String Terrain_N = (x - 1 >= 0 && y - 1 >= 0) ? cells[y - 1][x - 1].getTerrain().getKind()
        : TerrainType.WATER.getKind(); // 上方向の地形 ：あ
    boolean N = (Terrain_N.equals(collider));
    String Terrain_W = (x - 1 >= 0 && y + 1 < height) ? cells[y + 1][x - 1].getTerrain().getKind()
        : TerrainType.WATER.getKind(); // 左方向の地形 ：き
    boolean W = (Terrain_W.equals(collider));
    String Terrain_Xp = x + 1 < width ? cells[y][x + 1].getTerrain().getKind()
        : TerrainType.WATER.getKind(); // 左上方向の地形：く
    boolean Xp = (Terrain_Xp.equals(collider));
    String Terrain_Xm = x - 1 >= 0 ? cells[y][x - 1].getTerrain().getKind()
        : TerrainType.WATER.getKind(); // 右下方向の地形：え
    boolean Xm = (Terrain_Xm.equals(collider));
    String Terrain_Yp = y + 1 < height ? cells[y + 1][x].getTerrain().getKind()
        : TerrainType.WATER.getKind(); // 右上方向の地形：い
    boolean Yp = (Terrain_Yp.equals(collider));
    String Terrain_Ym = y - 1 >= 0 ? cells[y - 1][x].getTerrain().getKind()
        : TerrainType.WATER.getKind(); // 左下方向の地形：か
    boolean Ym = (Terrain_Ym.equals(collider));
    // 隣接するいずれかの地形と異なる場合、境界処理を行う
    Boolean needToStylize = N || E || S || W || Xp || Xm || Yp || Ym;
    if (!needToStylize) {
      return; // 隣接する全ての地形と同じ場合、境界処理は不要
    }
    // 境界地形のタイプコードを定義
    String typeCode = DefineBoundaryTypeCode(target);
    // 隣接する地形の状態に基づいて向きを決定
    String orientationCode = DefineOrientationCode(N, E, S, W, Xp, Xm, Yp, Ym);
    // typeCodeとorientationCodeを組み合わせて新しい地形タイプを決定
    // 例: "Coast_XpYm"など
    String newTerrainTypeName = typeCode + "_" + orientationCode;
    // System.out.println("(" + x + ", " + y + ")" + newTerrainTypeName);
    TerrainType newTerrainType = TerrainType.fromDisplayName(newTerrainTypeName);
    if (newTerrainType != null) {
      cells[y][x].setTerrain(newTerrainType);
    } else {
      System.out.println("(" + x + ", " + y + ")" + newTerrainTypeName + " Not found ");
    }
  }

  /**
   * 境界地形のタイプコードを定義する。
   *
   * @param target 基準となる地形
   * @return タイプコード
   */
  private String DefineBoundaryTypeCode(String target) {
    if (target.equals(TerrainType.WATER.getKind()))
      return "Coast"; // 海と草原の境界は山が削れる
    if (target.equals(TerrainType.GRASS.getKind()))
      return "Hill"; // 草原と山の境界は草原が削れる
    if (target.equals(TerrainType.MOUNTAIN.getKind()))
      return "Cliff"; // 山と海の境界は山が削れる
    return "";
  }

  /**
   * 境界地形の向きコードを定義する。
   * 
   * @params 各方向の隣接地形が異なるかどうかのフラグ
   * @return 向きコード
   */
  private String DefineOrientationCode(boolean N, boolean E, boolean S, boolean W,
      boolean Xp, boolean Xm, boolean Yp, boolean Ym) {
    // 8方向の組み合わせに基づいて向きコードを決定
    // 4辺全てが境界の場合
    if (Xp && Xm && Yp && Ym)
      return "XpXmYpYm";
    // 3辺が境界の場合
    if (Xm && Yp && Ym)
      return "XmYpYm";
    if (Xp && Yp && Ym)
      return "XpYpYm";
    if (Xp && Xm && Yp)
      return "XpXmYp";
    if (Xp && Xm && Ym)
      return "XpXmYm";
    // 向かい合う2辺が境界の場合
    if (Xp && Xm)
      return "XpXm";
    if (Yp && Ym)
      return "YpYm";
    // 隣接する2辺が境界で、その対角が境界でない場合
    if (Xp && Yp && !N)
      return "XpYp";
    if (Xp && Ym && !W)
      return "XpYm";
    if (Xm && Yp && !E)
      return "XmYp";
    if (Xm && Ym && !S)
      return "XmYm";
    // 隣接する2辺が境界で、その対角も境界の場合
    if (Xp && Yp && N)
      return "XpYpN";
    if (Xp && Ym && W)
      return "XpYmW";
    if (Xm && Yp && E)
      return "XmYpE";
    if (Xm && Ym && S)
      return "XmYmS";
    // 1辺のみが境界で、その隣接する対角がどちらも境界でない場合
    if (Xp && !N && !W)
      return "Xp";
    if (Xm && !S && !E)
      return "Xm";
    if (Yp && !N && !E)
      return "Yp";
    if (Ym && !W && !S)
      return "Ym";
    // 1辺のみが境界で、その隣接する対角のうち両方が境界の場合
    if (Xp && N && W)
      return "XpNW";
    if (Xm && S && E)
      return "XmES";
    if (Yp && N && E)
      return "YpNE";
    if (Ym && W && S)
      return "YmWS";
    // 1辺のみが境界で、その隣接する対角のうち片方のみが境界の場合
    if (Xp && N && !W)
      return "XpN";
    if (Xp && W && !N)
      return "XpW";
    if (Xm && E && !S)
      return "XmE";
    if (Xm && S && !E)
      return "XmS";
    if (Yp && N && !E)
      return "YpN";
    if (Yp && E && !N)
      return "YpE";
    if (Ym && W && !S)
      return "YmW";
    if (Ym && S && !W)
      return "YmS";
    // 辺の境界が存在せず、頂点が4つとも境界の場合
    if (N && E && S && W)
      return "NESW";
    // 辺の境界が存在せず、頂点が3つ境界の場合
    if (E && S && W)
      return "ESW";
    if (N && S && W)
      return "NSW";
    if (N && E && W)
      return "NEW";
    if (N && E && S)
      return "NES";
    // 辺の境界が存在せず、向かい合う2頂点が境界の場合
    if (N && S)
      return "NS";
    if (E && W)
      return "EW";
    // 辺の境界が存在せず、隣接する2頂点が境界の場合
    if (N && E)
      return "NE";
    if (S && E)
      return "SE";
    if (S && W)
      return "SW";
    if (N && W)
      return "NW";
    // 辺の境界が存在せず、1頂点のみが境界の場合
    if (N)
      return "N";
    if (E)
      return "E";
    if (S)
      return "S";
    if (W)
      return "W";

    // どの条件にも該当しない場合、空文字を返す(ここには到達しないはず)
    System.out.println("Error: No orientation code matched.");
    return "";
  }

  /** {@inheritDoc} */
  @Override
  public boolean isValidPosition(Point2D.Double pos) {
    double x = pos.getX(); // 中心基準
    double y = pos.getY();
    return x >= -0.5 && x < this.width - 0.5
        && y >= -0.5 && y < this.height - 0.5;
  }

  /** {@inheritDoc} */
  @Override
  public MapCell getCell(Point2D.Double pos) {
    if (!isValidPosition(pos)) {
      // 無効な位置へのアクセスは例外を投げる
      // またはnullを返す、境界用のダミーセルを返すなどの方法も考えられる
      throw new IndexOutOfBoundsException("Invalid position: " + pos);
    }
    return cells[(int) pos.getY()][(int) pos.getX()];
  }

  /** {@inheritDoc} */
  @Override
  public boolean placeBuilding(Point2D.Double pos, Building building) {
    if (building == null) {
      return false;
    }
    // Validate the original continuous position before snapping to the grid.
    if (!isValidPosition(pos)) {
      return false;
    }
    int anchorX = (int) Math.round(pos.getX());
    int anchorY = (int) Math.round(pos.getY());
    Point2D.Double anchorPos = new Point2D.Double(anchorX, anchorY);

    BuildingType type = building.getType();
    int originX = anchorX - type.getAnchorX();
    int originY = anchorY - type.getAnchorY();
    boolean[][] footprint = type.getFootprintMask();

    for (int y = 0; y < type.getHeight(); y++) {
      for (int x = 0; x < type.getWidth(); x++) {
        if (!footprint[y][x]) {
          continue;
        }
        int mapX = originX + x;
        int mapY = originY + y;
        Point2D.Double cellPos = new Point2D.Double(mapX, mapY);
        if (!isValidPosition(cellPos)) {
          return false;
        }
        MapCell cell = getCell(cellPos);
        if (cell.isOccupied() || !cell.getTerrain().isBuildable()) {
          return false;
        }
      }
    }

    building.setOrigin(originX, originY);
    building.setPosition(anchorPos);

    for (int y = 0; y < type.getHeight(); y++) {
      for (int x = 0; x < type.getWidth(); x++) {
        if (!footprint[y][x]) {
          continue;
        }
        int mapX = originX + x;
        int mapY = originY + y;
        Point2D.Double cellPos = new Point2D.Double(mapX, mapY);
        getCell(cellPos).setBuilding(building, x, y);
      }
    }

    eventBus.publish(new MapUpdatedEvent(anchorPos));
    return true;
  }

  /** {@inheritDoc} */
  @Override
  public boolean removeBuilding(Point2D.Double pos) {
    int anchorX = (int) Math.round(pos.getX());
    int anchorY = (int) Math.round(pos.getY());
    Point2D.Double anchorPos = new Point2D.Double(anchorX, anchorY);
    if (!isValidPosition(anchorPos)) {
      return false;
    }
    MapCell cell = getCell(anchorPos);
    Building building = cell.getBuilding();
    if (building == null) {
      return false;
    }

    BuildingType type = building.getType();
    int originX = building.getOriginX();
    int originY = building.getOriginY();
    boolean[][] footprint = type.getFootprintMask();

    for (int y = 0; y < type.getHeight(); y++) {
      for (int x = 0; x < type.getWidth(); x++) {
        if (!footprint[y][x]) {
          continue;
        }
        int mapX = originX + x;
        int mapY = originY + y;
        Point2D.Double cellPos = new Point2D.Double(mapX, mapY);
        if (!isValidPosition(cellPos)) {
          continue;
        }
        MapCell target = getCell(cellPos);
        if (target.getBuilding() == building) {
          target.clearBuilding();
        }
      }
    }

    eventBus.publish(new MapUpdatedEvent(anchorPos));
    return true;
  }

  /** {@inheritDoc} */
  @Override
  public int getWidth() {
    return width;
  }

  /** {@inheritDoc} */
  @Override
  public int getHeight() {
    return height;
  }
}
