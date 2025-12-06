package io.github.sasori_256.town_planning.model;

/**
 * 住民の属性定義。
 */
public enum ResidentType {
  CIVILIAN("一般人", 1.0f),
  BELIEVER("信者", 1.5f),
  HERETIC("異端者", 0.5f);

  private final String displayName;
  private final float faithMultiplier;

  ResidentType(String displayName, float faithMultiplier) {
    this.displayName = displayName;
    this.faithMultiplier = faithMultiplier;
  }

  public String getDisplayName() {
    return displayName;
  }

  public float getFaithMultiplier() {
    return faithMultiplier;
  }
}
