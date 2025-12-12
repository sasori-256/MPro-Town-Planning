package io.github.sasori_256.town_planning.model;

/**
 * 住民の属性定義。
 *
 * TODO: 将来的にjsonなどの外部データから読み込む形に変更する。
 */
public enum ResidentType {
  CIVILIAN("civilian", 50, 10, 1.0f),
  BELIEVER("believer", 60, 50, 1.5f),
  HERETIC("heretic", 100, 0, 0.5f);

  private final String displayName;
  private final int maxAge;
  private final int initialFaith;
  private final float faithMultiplier;

  ResidentType(String displayName, int maxAge, int initialFaith, float faithMultiplier) {
    this.displayName = displayName;
    this.maxAge = maxAge;
    this.initialFaith = initialFaith;
    this.faithMultiplier = faithMultiplier;
  }

  public String getDisplayName() {
    return displayName;
  }

  public int getMaxAge() {
    return maxAge;
  }

  public int getInitialFaith() {
    return initialFaith;
  }

  public float getFaithMultiplier() {
    return faithMultiplier;
  }
}
