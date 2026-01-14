package io.github.sasori_256.town_planning.entity.resident;

/**
 * 住民の属性定義。
 */
public enum ResidentType {
  CITIZEN("citizen", "people_1", 50, 10, 1.0f),
  BELIEVER("believer", "people_1", 60, 50, 1.5f),
  HERETIC("heretic", "people_1", 100, 0, 0.5f);

  private final String displayName;
  private final String imageName;
  private final int maxAge;
  private final int initialFaith;
  private final float faithMultiplier;

  /**
   * 住民の種類を初期化
   *
   * @param displayName     表示名
   * @param imageName       描画に使用する画像名
   * @param maxAge          最大年齢
   * @param initialFaith    初期信仰度
   * @param faithMultiplier 信仰度の成長倍率
   */
  ResidentType(String displayName, String imageName, int maxAge, int initialFaith, float faithMultiplier) {
    this.displayName = displayName;
    this.imageName = imageName;
    this.maxAge = maxAge;
    this.initialFaith = initialFaith;
    this.faithMultiplier = faithMultiplier;
  }

  /**
   * 表示名を返す。
   */
  public String getDisplayName() {
    return displayName;
  }

  /**
   * 描画に使用する画像名を返す。
   */
  public String getImageName() {
    return imageName;
  }

  /**
   * 最大年齢を返す。
   */
  public int getMaxAge() {
    return maxAge;
  }

  /**
   * 初期信仰度を返す。
   */
  public int getInitialFaith() {
    return initialFaith;
  }

  /**
   * 信仰度成長倍率を返す。
   */
  public float getFaithMultiplier() {
    return faithMultiplier;
  }
}
