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
  RED_ROOFED_HOUSE("赤屋根の家", "red_roofed_house", 50, 4, 100, () -> new PopulationGrowthEffect(4),
      CategoryType.RESIDENTIAL),
  BLUE_ROOFED_HOUSE("青屋根の家", "blue_roofed_house", 100, 8, 150, () -> new PopulationGrowthEffect(8),
      CategoryType.RESIDENTIAL),
  ROAD("道", "road", 0, 0, 1, () -> null, CategoryType.INFRASTRUCTURE,
      1, 1, singleMask(true), singleMask(true), singleCost(1), singleTile("road"), 0, 0),
  PLAZA("広場", "plaza_fountain_center", 0, 0, 1, () -> null, CategoryType.INFRASTRUCTURE,
      3, 3, filledMask(3, 3, true), plazaWalkable(), plazaCost(), plazaTiles(), 1, 1),
  PARK("公園", "park_floor", 0, 0, 1, () -> null, CategoryType.INFRASTRUCTURE,
      5, 5, filledMask(5, 5, true), filledMask(5, 5, true), filledCost(5, 5, 2),
      filledTiles(5, 5, "park_floor"), 2, 2),
  CHAPEL("礼拝堂", "chapel", 100, 0, 100, CategoryType.RELIGIOUS),
  CHURCH("教会", "church", 150, 0, 150, CategoryType.RELIGIOUS),
  GRAVEYARD("墓地", "graveyard", 100, 0, 100, CategoryType.CEMETERY);

  private final String displayName;
  private final String imageName;
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
  private final String[][] tileImageNames;
  private final int anchorX;
  private final int anchorY;

  private static final int DEFAULT_IMPASSABLE_COST = 1_000_000;

  // Effectありのコンストラクタ
  BuildingType(String displayName, String imageName, int cost, int maxPopulation, int maxDurability,
      Supplier<GameEffect> effectSupplier, CategoryType category) {
    this(displayName, imageName, cost, maxPopulation, maxDurability, effectSupplier, category,
        1, 1, singleMask(true), singleMask(false), singleCost(DEFAULT_IMPASSABLE_COST),
        singleTile(imageName), 0, 0);
  }

  // Effectなしのコンストラクタ
  BuildingType(String displayName, String imageName, int cost, int maxPopulation, int maxDurability,
      CategoryType category) {
    this(displayName, imageName, cost, maxPopulation, maxDurability, () -> null, category);
  }

  BuildingType(String displayName, String imageName, int cost, int maxPopulation, int maxDurability,
      Supplier<GameEffect> effectSupplier, CategoryType category,
      int width, int height, boolean[][] footprintMask, boolean[][] walkableMask,
      int[][] moveCost, String[][] tileImageNames, int anchorX, int anchorY) {
    if (width <= 0 || height <= 0) {
      throw new IllegalArgumentException("width/height must be positive.");
    }
    validateMask("footprintMask", width, height, footprintMask);
    validateMask("walkableMask", width, height, walkableMask);
    validateCost("moveCost", width, height, moveCost);
    validateTiles("tileImageNames", width, height, tileImageNames);

    this.displayName = displayName;
    this.imageName = imageName;
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
    this.tileImageNames = tileImageNames;
    this.anchorX = anchorX;
    this.anchorY = anchorY;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getImageName() {
    return imageName;
  }

  public String getImageName(int localX, int localY) {
    if (localX < 0 || localY < 0 || localX >= width || localY >= height) {
      return imageName;
    }
    String name = tileImageNames[localY][localX];
    return name != null ? name : imageName;
  }

  public int getCost() {
    return cost;
  }

  public int getMaxPopulation() {
    return maxPopulation;
  }

  public int getMaxDurability() {
    return maxDurability;
  }

  public Supplier<GameEffect> getEffectSupplier() {
    return effectSupplier;
  }

  public CategoryType getCategory() {
    return category;
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  public boolean[][] getFootprintMask() {
    return footprintMask;
  }

  public boolean isWalkable(int localX, int localY) {
    if (localX < 0 || localY < 0 || localX >= width || localY >= height) {
      return false;
    }
    return walkableMask[localY][localX];
  }

  public int getMoveCost(int localX, int localY) {
    if (localX < 0 || localY < 0 || localX >= width || localY >= height) {
      return DEFAULT_IMPASSABLE_COST;
    }
    if (!walkableMask[localY][localX]) {
      return DEFAULT_IMPASSABLE_COST;
    }
    return moveCost[localY][localX];
  }

  public int getAnchorX() {
    return anchorX;
  }

  public int getAnchorY() {
    return anchorY;
  }

  private static boolean[][] singleMask(boolean value) {
    return new boolean[][]{{value}};
  }

  private static int[][] singleCost(int value) {
    return new int[][]{{value}};
  }

  private static String[][] singleTile(String value) {
    return new String[][]{{value}};
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

  private static String[][] filledTiles(int width, int height, String value) {
    String[][] tiles = new String[height][width];
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        tiles[y][x] = value;
      }
    }
    return tiles;
  }

  private static boolean[][] plazaWalkable() {
    boolean[][] mask = filledMask(3, 3, true);
    mask[1][1] = false;
    return mask;
  }

  private static int[][] plazaCost() {
    int[][] cost = filledCost(3, 3, 1);
    cost[1][1] = DEFAULT_IMPASSABLE_COST;
    return cost;
  }

  private static String[][] plazaTiles() {
    String[][] tiles = filledTiles(3, 3, "road");
    tiles[1][1] = "plaza_fountain_center";
    return tiles;
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

  private static void validateTiles(String name, int width, int height, String[][] tiles) {
    if (tiles == null || tiles.length != height) {
      throw new IllegalArgumentException(name + " height mismatch.");
    }
    for (int y = 0; y < height; y++) {
      if (tiles[y] == null || tiles[y].length != width) {
        throw new IllegalArgumentException(name + " width mismatch at y=" + y);
      }
    }
  }
}
