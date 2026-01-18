package io.github.sasori_256.town_planning.common.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * ゲーム設定をリソースから読み込むユーティリティ。
 */
public final class GameConfig {
  private static final Properties PROPERTIES = loadProperties();

  private GameConfig() {
  }

  public static double getCorpseHarvestDelaySeconds() {
    return getDouble("corpse.harvestDelaySeconds", 2.0);
  }

  public static int getCorpseSoulBase() {
    return getNonNegativeInt("corpse.soulBase", 10);
  }

  public static int getCorpseSoulFaithDivisor() {
    return getPositiveInt("corpse.soulFaithDivisor", 5);
  }

  private static Properties loadProperties() {
    Properties props = new Properties();
    try (InputStream input = GameConfig.class.getClassLoader()
        .getResourceAsStream("game.properties")) {
      if (input != null) {
        props.load(input);
      }
    } catch (IOException e) {
      System.err.println("Failed to load game.properties: " + e.getMessage());
    }
    return props;
  }

  private static double getDouble(String key, double defaultValue) {
    String raw = PROPERTIES.getProperty(key);
    if (raw == null) {
      return defaultValue;
    }
    try {
      return Double.parseDouble(raw.trim());
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }

  private static int getNonNegativeInt(String key, int defaultValue) {
    int value = getInt(key, defaultValue);
    return value < 0 ? defaultValue : value;
  }

  private static int getPositiveInt(String key, int defaultValue) {
    int value = getInt(key, defaultValue);
    return value <= 0 ? defaultValue : value;
  }

  private static int getInt(String key, int defaultValue) {
    String raw = PROPERTIES.getProperty(key);
    if (raw == null) {
      return defaultValue;
    }
    try {
      return Integer.parseInt(raw.trim());
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }
}
