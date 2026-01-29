package io.github.sasori_256.town_planning.entity.building;

import java.util.function.Supplier;
import io.github.sasori_256.town_planning.entity.building.strategy.PopulationGrowthEffect;
import io.github.sasori_256.town_planning.entity.model.CategoryType;
import io.github.sasori_256.town_planning.entity.model.GameEffect;

/**
 * 建物の種類定義。
 */
public enum BuildingType {
  // maxPopulationとPopulationGrowthStrategyの紐付けを行い、Strategy生成用Supplierを設定
  RED_ROOFED_HOUSE(
      "赤屋根の家",
      "red_roofed_house",
      50,
      4,
      100,
      () -> new PopulationGrowthEffect(4),
      CategoryType.RESIDENTIAL),
  BLUE_ROOFED_HOUSE(
      "青屋根の家",
      "blue_roofed_house",
      100,
      8,
      150,
      () -> new PopulationGrowthEffect(8),
      CategoryType.RESIDENTIAL),
  ROAD(
      "道",
      0,
      0,
      1,
      () -> null,
      CategoryType.INFRASTRUCTURE,
      1,
      1,
      singleMask(true),
      singleMask(true),
      singleCost(1),
      singleTileImages("stone_brick_floor"),
      singleDrawGroup(DrawGroup.FLOOR),
      0,
      0),
  PLAZA(
      "広場",
      0,
      0,
      1,
      () -> null,
      CategoryType.INFRASTRUCTURE,
      3,
      3,
      new boolean[][] {
          { true, true, true },
          { true, true, true },
          { true, true, true }
      },
      new boolean[][] {
          { true, true, true },
          { true, false, true },
          { true, true, true }
      },
      new int[][] {
          { 1, 1, 1 },
          { 1, 1_000_000, 1 },
          { 1, 1, 1 }
      },
      new String[][] {
          { "stone_brick_floor", "stone_brick_floor", "stone_brick_floor" },
          { "stone_brick_floor", "plaza_fountain_center", "stone_brick_floor" },
          { "stone_brick_floor", "stone_brick_floor", "stone_brick_floor" }
      },
      new DrawGroup[][] {
          { DrawGroup.FLOOR, DrawGroup.FLOOR, DrawGroup.FLOOR },
          { DrawGroup.FLOOR, DrawGroup.ACTOR, DrawGroup.FLOOR },
          { DrawGroup.FLOOR, DrawGroup.FLOOR, DrawGroup.FLOOR }
      },
      1,
      1),
  PARK(
      "公園",
      0,
      0,
      1,
      () -> null, CategoryType.INFRASTRUCTURE,
      5,
      5,
      filledMask(5, 5, true),
      filledMask(5, 5, true),
      filledCost(5, 5, 2),
      filledTileImages(5, 5, "park_floor"),
      filledDrawGroup(5, 5, DrawGroup.FLOOR),
      2,
      2),
  CHAPEL(
      "礼拝堂",
      "chapel",
      100,
      0,
      100,
      CategoryType.RELIGIOUS),
  CHURCH("教会",
      0,
      20,
      150,
      () -> null,
      CategoryType.RELIGIOUS,
      2,
      3,
      new boolean[][] {
          { true, true },
          { true, true },
          { true, true }
      },
      new boolean[][] {
          { false, false },
          { false, false },
          { false, false }
      },
      new int[][] {
          { 1_000_000, 1_000_000 },
          { 1_000_000, 1_000_000 },
          { 1_000_000, 1_000_000 }
      },
      new String[][] {
          { "none", "none" },
          { "none", "none" },
          { "none", "church" }
      },
      new DrawGroup[][] {
          { DrawGroup.FLOOR, DrawGroup.FLOOR },
          { DrawGroup.FLOOR, DrawGroup.FLOOR },
          { DrawGroup.ACTOR, DrawGroup.FLOOR }
      },
      1, 2),
  GRAVEYARD("墓地",
      "graveyard",
      100,
      0,
      100,
      CategoryType.CEMETERY),
  TREE11("木11",
      "smallTree11",
      10,
      0,
      40,
      CategoryType.NATURE),
  TREE12("木12",
      "smallTree12",
      10,
      0,
      60,
      CategoryType.NATURE),
  TREE13("木13",
      "smallTree13",
      10,
      0,
      60,
      CategoryType.NATURE),
  TREE21("木21",
      "smallTree21",
      10,
      0,
      80,
      CategoryType.NATURE),
  TREE22("木22",
      "smallTree22",
      10,
      0,
      80,
      CategoryType.NATURE),
  TREE31("木31",
      "smallTree31",
      20,
      0,
      120,
      CategoryType.NATURE),
  TREE32("木32",
      "smallTree32",
      20,
      0,
      120,
      CategoryType.NATURE);

  /**
   * 描画順のグループ。
   */
  public enum DrawGroup {
    /** 床タイルとして描画する。 */
    FLOOR,
    /** 住民と同じパスで描画する。 */
    ACTOR
  }

  private final String displayName;
  private final int cost;
  private final int maxPopulation;
  private final int maxDurability;
  // GameEffect(並行機能)のファクトリ
  private final Supplier<GameEffect> effectSupplier;
  private final CategoryType category;
  private final int width;
  private final int height;
  private final boolean[][] footprintMask;
  private final boolean[][] walkableMask;
  private final int[][] moveCost;
  private final String[][] tileImages;
  private final DrawGroup[][] drawGroup;
  private final int anchorX;
  private final int anchorY;

  private static final int DEFAULT_IMPASSABLE_COST = 1_000_000;

  // Effectありのコンストラクタ
  BuildingType(
      String displayName,
      String tileImageName,
      int cost,
      int maxPopulation,
      int maxDurability,
      Supplier<GameEffect> effectSupplier,
      CategoryType category) {
    this(displayName, cost, maxPopulation, maxDurability, effectSupplier, category,
        1, 1, singleMask(true), singleMask(false), singleCost(DEFAULT_IMPASSABLE_COST),
        singleTileImages(tileImageName), singleDrawGroup(DrawGroup.ACTOR), 0, 0);
  }

  // Effectなしのコンストラクタ
  BuildingType(
      String displayName,
      String tileImageName,
      int cost,
      int maxPopulation,
      int maxDurability,
      CategoryType category) {
    this(displayName, tileImageName, cost, maxPopulation, maxDurability, () -> null, category);
  }

  BuildingType(
      String displayName,
      int cost,
      int maxPopulation,
      int maxDurability,
      Supplier<GameEffect> effectSupplier,
      CategoryType category,
      int width,
      int height,
      boolean[][] footprintMask,
      boolean[][] walkableMask,
      int[][] moveCost,
      String[][] tileImages,
      DrawGroup[][] drawGroup,
      int anchorX, int anchorY) {
    if (width <= 0 || height <= 0) {
      throw new IllegalArgumentException("width/height must be positive.");
    }
    validateMask("footprintMask", width, height, footprintMask);
    validateMask("walkableMask", width, height, walkableMask);
    validateCost("moveCost", width, height, moveCost);
    validateTileImages("tileImages", width, height, tileImages);
    validateDrawGroup("drawGroup", width, height, drawGroup);
    if (anchorX < 0 || anchorX >= width || anchorY < 0 || anchorY >= height) {
      throw new IllegalArgumentException(
          "anchorX and anchorY must be within bounds: 0 <= anchorX < " + width
              + ", 0 <= anchorY < " + height + " (got anchorX=" + anchorX + ", anchorY=" + anchorY + ")");
    }

    this.displayName = displayName;
    this.cost = cost;
    this.maxPopulation = maxPopulation;
    this.maxDurability = maxDurability;
    this.effectSupplier = effectSupplier;
    this.category = category;
    this.width = width;
    this.height = height;
    this.footprintMask = footprintMask;
    this.walkableMask = walkableMask;
    this.moveCost = moveCost;
    this.tileImages = tileImages;
    this.drawGroup = drawGroup;
    this.anchorX = anchorX;
    this.anchorY = anchorY;
  }

  /**
   * 表示名を返す。
   */
  public String getDisplayName() {
    return displayName;
  }

  /**
   * タイル座標に対応する画像名を返す。
   *
   * @param localX 建物内のX座標
   * @param localY 建物内のY座標
   */
  public String getTileImageName(int localX, int localY) {
    if (localX < 0 || localY < 0 || localX >= width || localY >= height) {
      return null;
    }
    return tileImages[localY][localX];
  }

  /**
   * 指定タイルのアニメーション名を返す。
   *
   * @param localX 建物内のX座標
   * @param localY 建物内のY座標
   */
  public String getAnimationName(int localX, int localY) {
    if (localX < 0 || localY < 0 || localX >= width || localY >= height) {
      return null;
    }
    if (this == PLAZA && localX == 1 && localY == 1) {
      return "plaza_fountain_center";
    }
    return null;
  }

  /**
   * 指定タイルのアニメーションフレームレートを返す。
   *
   * @param localX 建物内のX座標
   * @param localY 建物内のY座標
   */
  public int getAnimationFrameRate(int localX, int localY) {
    return getAnimationName(localX, localY) != null ? 6 : 0;
  }

  /**
   * 指定タイルのアニメーションがループするかを返す。
   *
   * @param localX 建物内のX座標
   * @param localY 建物内のY座標
   */
  public boolean isAnimationLoop(int localX, int localY) {
    return getAnimationName(localX, localY) != null;
  }

  /**
   * アニメーションを持つ建物かを返す。
   *
   * @return アニメーションを持つ場合はtrue
   */
  public boolean hasAnimation() {
    return this == PLAZA;
  }

  /**
   * 建設コストを返す。
   */
  public int getCost() {
    return cost;
  }

  /**
   * 最大人口を返す。
   */
  public int getMaxPopulation() {
    return maxPopulation;
  }

  /**
   * 最大耐久度を返す。
   */
  public int getMaxDurability() {
    return maxDurability;
  }

  /**
   * 効果生成用のSupplierを返す。
   */
  public Supplier<GameEffect> getEffectSupplier() {
    return effectSupplier;
  }

  /**
   * 建物カテゴリを返す。
   */
  public CategoryType getCategory() {
    return category;
  }

  /**
   * 横幅(セル数)を返す。
   */
  public int getWidth() {
    return width;
  }

  /**
   * 高さ(セル数)を返す。
   */
  public int getHeight() {
    return height;
  }

  /**
   * 占有セルのマスクを返す。
   */
  public boolean[][] getFootprintMask() {
    if (footprintMask == null) {
      return null;
    }
    boolean[][] copy = new boolean[footprintMask.length][];
    for (int i = 0; i < footprintMask.length; i++) {
      boolean[] row = footprintMask[i];
      copy[i] = (row != null) ? row.clone() : null;
    }
    return copy;
  }

  /**
   * 指定タイルが歩行可能かを返す。
   */
  public boolean isWalkable(int localX, int localY) {
    if (localX < 0 || localY < 0 || localX >= width || localY >= height) {
      return false;
    }
    return walkableMask[localY][localX];
  }

  /**
   * 指定タイルの移動コストを返す。
   */
  public int getMoveCost(int localX, int localY) {
    if (localX < 0 || localY < 0 || localX >= width || localY >= height) {
      return DEFAULT_IMPASSABLE_COST;
    }
    if (!walkableMask[localY][localX]) {
      return DEFAULT_IMPASSABLE_COST;
    }
    return moveCost[localY][localX];
  }

  /**
   * 指定タイルの描画グループを返す。
   */
  public DrawGroup getDrawGroup(int localX, int localY) {
    if (localX < 0 || localY < 0 || localX >= width || localY >= height) {
      return DrawGroup.FLOOR;
    }
    return drawGroup[localY][localX];
  }

  /**
   * アンカーXを返す。
   */
  public int getAnchorX() {
    return anchorX;
  }

  /**
   * アンカーYを返す。
   */
  public int getAnchorY() {
    return anchorY;
  }

  /**
   * 建物の詳細情報を文字列として返す。
   * 
   * @return 建物の種類とコストを含む詳細文字列
   */
  public String getDetail() {
    return "type=" + this + ", cost=" + cost;
  }

  /**
   * 建物種別の詳細情報を安全に取得する静的メソッド。
   * nullを含む任意のBuildingTypeを処理できる。
   * 
   * @param type 建物種別(nullの場合あり)
   * @return 建物の詳細文字列
   */
  public static String getDetailString(BuildingType type) {
    if (type == null) {
      return "type=null";
    }
    return type.getDetail();
  }

  private static boolean[][] singleMask(boolean value) {
    return new boolean[][] { { value } };
  }

  private static int[][] singleCost(int value) {
    return new int[][] { { value } };
  }

  private static String[][] singleTileImages(String value) {
    return new String[][] { { value } };
  }

  private static DrawGroup[][] singleDrawGroup(DrawGroup value) {
    return new DrawGroup[][] { { value } };
  }

  private static boolean[][] filledMask(int width, int height, boolean value) {
    boolean[][] mask = new boolean[height][width];
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        mask[y][x] = value;
      }
    }
    return mask;
  }

  private static int[][] filledCost(int width, int height, int value) {
    int[][] cost = new int[height][width];
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        cost[y][x] = value;
      }
    }
    return cost;
  }

  private static String[][] filledTileImages(int width, int height, String value) {
    String[][] tiles = new String[height][width];
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        tiles[y][x] = value;
      }
    }
    return tiles;
  }

  private static DrawGroup[][] filledDrawGroup(int width, int height, DrawGroup value) {
    DrawGroup[][] groups = new DrawGroup[height][width];
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        groups[y][x] = value;
      }
    }
    return groups;
  }

  private static void validateMask(String name, int width, int height, boolean[][] mask) {
    if (mask == null || mask.length != height) {
      throw new IllegalArgumentException(name + " height mismatch.");
    }
    for (int y = 0; y < height; y++) {
      if (mask[y] == null || mask[y].length != width) {
        throw new IllegalArgumentException(name + " width mismatch at y=" + y);
      }
    }
  }

  private static void validateCost(String name, int width, int height, int[][] cost) {
    if (cost == null || cost.length != height) {
      throw new IllegalArgumentException(name + " height mismatch.");
    }
    for (int y = 0; y < height; y++) {
      if (cost[y] == null || cost[y].length != width) {
        throw new IllegalArgumentException(name + " width mismatch at y=" + y);
      }
    }
  }

  private static void validateTileImages(String name, int width, int height, String[][] tiles) {
    if (tiles == null || tiles.length != height) {
      throw new IllegalArgumentException(name + " height mismatch.");
    }
    for (int y = 0; y < height; y++) {
      if (tiles[y] == null || tiles[y].length != width) {
        throw new IllegalArgumentException(name + " width mismatch at y=" + y);
      }
      for (int x = 0; x < width; x++) {
        if (tiles[y][x] == null) {
          throw new IllegalArgumentException(
              name + " contains null at x=" + x + ", y=" + y);
        }
      }
    }
  }

  private static void validateDrawGroup(String name, int width, int height, DrawGroup[][] group) {
    if (group == null || group.length != height) {
      throw new IllegalArgumentException(name + " height mismatch.");
    }
    for (int y = 0; y < height; y++) {
      if (group[y] == null || group[y].length != width) {
        throw new IllegalArgumentException(name + " width mismatch at y=" + y);
      }
    }
  }
}
