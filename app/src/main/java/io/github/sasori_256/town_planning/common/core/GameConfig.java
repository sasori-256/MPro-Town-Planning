package io.github.sasori_256.town_planning.common.core;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import io.github.sasori_256.town_planning.common.event.EventBus;
import io.github.sasori_256.town_planning.common.event.events.ConfigLoadFailedEvent;

/**
 * ゲーム設定をリソースから読み込むユーティリティ。
 */
public final class GameConfig {
  private static final List<ConfigError> LOAD_ERRORS = new ArrayList<>();
  private static final Properties PROPERTIES = loadProperties();
  private static boolean errorsReported;

  private GameConfig() {
  }

  public static double getGameLoopTimeStepSeconds() {
    return getPositiveDouble("game.loop.timeStepSeconds", 1.0 / 30.0);
  }

  public static double getAnimationStepSeconds() {
    return getPositiveDouble("game.animation.stepSeconds", 1.0 / 30.0);
  }

  public static double getTimeDayLengthSeconds() {
    return getPositiveDouble("time.dayLengthSeconds", 10.0);
  }

  public static double getTimeScale() {
    return getNonNegativeDouble("time.scale", 1.0);
  }

  public static int getSoulInitialAmount() {
    return getNonNegativeInt("soul.initialAmount", 100);
  }

  public static double getCorpseHarvestDelaySeconds() {
    return getPositiveDouble("corpse.harvestDelaySeconds", 2.0);
  }

  public static int getCorpseSoulBase() {
    return getNonNegativeInt("corpse.soulBase", 10);
  }

  public static int getCorpseSoulFaithDivisor() {
    return getPositiveInt("corpse.soulFaithDivisor", 5);
  }

  public static double getResidentWorkDurationSeconds() {
    return getPositiveDouble("resident.workDurationSeconds", 5.0);
  }

  public static double getResidentHomeWaitMinSeconds() {
    return getNonNegativeDouble("resident.homeWaitMinSeconds", 8.0);
  }

  public static double getResidentHomeWaitMaxSeconds() {
    return getNonNegativeDouble("resident.homeWaitMaxSeconds", 20.0);
  }

  public static double getResidentMoveSpeedTilesPerSecond() {
    return getPositiveDouble("resident.moveSpeedTilesPerSecond", 2.0);
  }

  public static int getResidentGrowthMinHomePopulation() {
    return getNonNegativeInt("resident.growth.minHomePopulation", 2);
  }

  public static double getResidentGrowthSpawnIntervalSeconds() {
    return getPositiveDouble("resident.growth.spawnIntervalSeconds", 15.0);
  }

  public static double getResidentGrowthSpawnChance() {
    return getNonNegativeDouble("resident.growth.spawnChance", 0.5);
  }

  public static double getResidentPanicSpeedTilesPerSecond() {
    return getPositiveDouble("resident.panic.speedTilesPerSecond", 3.0);
  }

  public static double getResidentPanicRetargetMinSeconds() {
    return getNonNegativeDouble("resident.panic.retargetMinSeconds", 0.2);
  }

  public static double getResidentPanicRetargetMaxSeconds() {
    return getNonNegativeDouble("resident.panic.retargetMaxSeconds", 0.6);
  }

  public static double getResidentPanicDurationMinSeconds() {
    return getNonNegativeDouble("resident.panic.durationMinSeconds", 4.0);
  }

  public static double getResidentPanicDurationMaxSeconds() {
    return getNonNegativeDouble("resident.panic.durationMaxSeconds", 8.0);
  }

  public static double getResidentPanicRadiusTiles() {
    return getNonNegativeDouble("resident.panic.radiusTiles", 3.0);
  }

  public static double getResidentDeathAnimationDurationSeconds() {
    return getNonNegativeDouble("resident.death.animationDurationSeconds", 0.4);
  }

  public static int getResidentRelocationMinResidentsPerHouse() {
    return getPositiveInt("resident.relocation.minResidentsPerHouse", 2);
  }

  public static double getPathfindingArrivalEpsilonTiles() {
    return getNonNegativeDouble("pathfinding.arrivalEpsilonTiles", 1e-3);
  }

  public static double getPathfindingSearchCooldownSeconds() {
    return getNonNegativeDouble("pathfinding.searchCooldownSeconds", 0.5);
  }

  public static int getPathfindingMaxRandomTries() {
    return getPositiveInt("pathfinding.maxRandomTries", 20);
  }

  public static long getPathfindingCostInf() {
    return getNonNegativeLong("pathfinding.costInf", 1_000_000L);
  }

  public static double getCameraZoomStep() {
    return getPositiveDouble("camera.zoom.step", 0.25);
  }

  public static int getCameraZoomMinLevel() {
    return getPositiveInt("camera.zoom.minLevel", 1);
  }

  public static int getCameraZoomMaxLevel() {
    return getPositiveInt("camera.zoom.maxLevel", 12);
  }

  public static int getToastDisplayMillis() {
    return getPositiveInt("ui.toast.displayMillis", 3000);
  }

  public static int getToastMaxVisible() {
    return getPositiveInt("ui.toast.maxVisible", 3);
  }

  public static int getToastMarginPixels() {
    return getNonNegativeInt("ui.toast.marginPixels", 12);
  }

  public static int getToastSpacingPixels() {
    return getNonNegativeInt("ui.toast.spacingPixels", 8);
  }

  public static double getDisasterPanicRadiusOffsetTiles() {
    return getNonNegativeDouble("disaster.panicRadiusOffsetTiles", 3.0);
  }

  public static double getDisasterMeteorImpactSeconds() {
    return getPositiveDouble("disaster.meteor.impactSeconds", 2.0);
  }

  public static double getDisasterMeteorEffectDurationSeconds() {
    return getNonNegativeDouble("disaster.meteor.effectDurationSeconds", 1.0);
  }

  public static int getDisasterMeteorAnimationFps() {
    return getPositiveInt("disaster.meteor.animationFps", 6);
  }

  public static void preload() {
    getGameLoopTimeStepSeconds();
    getAnimationStepSeconds();
    getTimeDayLengthSeconds();
    getTimeScale();
    getSoulInitialAmount();
    getCorpseHarvestDelaySeconds();
    getCorpseSoulBase();
    getCorpseSoulFaithDivisor();
    getResidentWorkDurationSeconds();
    getResidentHomeWaitMinSeconds();
    getResidentHomeWaitMaxSeconds();
    getResidentMoveSpeedTilesPerSecond();
    getResidentGrowthMinHomePopulation();
    getResidentGrowthSpawnIntervalSeconds();
    getResidentGrowthSpawnChance();
    getResidentPanicSpeedTilesPerSecond();
    getResidentPanicRetargetMinSeconds();
    getResidentPanicRetargetMaxSeconds();
    getResidentPanicDurationMinSeconds();
    getResidentPanicDurationMaxSeconds();
    getResidentPanicRadiusTiles();
    getResidentDeathAnimationDurationSeconds();
    getResidentRelocationMinResidentsPerHouse();
    getPathfindingArrivalEpsilonTiles();
    getPathfindingSearchCooldownSeconds();
    getPathfindingMaxRandomTries();
    getPathfindingCostInf();
    getCameraZoomStep();
    getCameraZoomMinLevel();
    getCameraZoomMaxLevel();
    getToastDisplayMillis();
    getToastMaxVisible();
    getToastMarginPixels();
    getToastSpacingPixels();
    getDisasterPanicRadiusOffsetTiles();
    getDisasterMeteorImpactSeconds();
    getDisasterMeteorEffectDurationSeconds();
    getDisasterMeteorAnimationFps();
  }

  public static void reportErrors() {
    List<ConfigError> errors;
    synchronized (GameConfig.class) {
      if (errorsReported) {
        return;
      }
      errorsReported = true;
      errors = new ArrayList<>(LOAD_ERRORS);
      LOAD_ERRORS.clear();
    }
    for (ConfigError error : errors) {
      EventBus.getInstance().publish(new ConfigLoadFailedEvent(error.code, error.message));
    }
  }

  private static Properties loadProperties() {
    Properties props = new Properties();
    try (InputStream input = GameConfig.class.getClassLoader()
        .getResourceAsStream("game.properties")) {
      if (input == null) {
        recordError("CONFIG_NOT_FOUND", "game.properties not found.");
        return props;
      }
      props.load(input);
    } catch (IOException e) {
      recordError("CONFIG_LOAD_FAILED", "Failed to load game.properties: " + e.getMessage());
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
      recordError("CONFIG_PARSE_FAILED", "Invalid double for " + key + ": " + raw);
      return defaultValue;
    }
  }

  private static long getLong(String key, long defaultValue) {
    String raw = PROPERTIES.getProperty(key);
    if (raw == null) {
      return defaultValue;
    }
    try {
      return Long.parseLong(raw.trim());
    } catch (NumberFormatException e) {
      recordError("CONFIG_PARSE_FAILED", "Invalid long for " + key + ": " + raw);
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

  private static double getNonNegativeDouble(String key, double defaultValue) {
    double value = getDouble(key, defaultValue);
    return value < 0.0 ? defaultValue : value;
  }

  private static double getPositiveDouble(String key, double defaultValue) {
    double value = getDouble(key, defaultValue);
    return value <= 0.0 ? defaultValue : value;
  }

  private static long getNonNegativeLong(String key, long defaultValue) {
    long value = getLong(key, defaultValue);
    return value < 0L ? defaultValue : value;
  }

  private static int getInt(String key, int defaultValue) {
    String raw = PROPERTIES.getProperty(key);
    if (raw == null) {
      return defaultValue;
    }
    try {
      return Integer.parseInt(raw.trim());
    } catch (NumberFormatException e) {
      recordError("CONFIG_PARSE_FAILED", "Invalid int for " + key + ": " + raw);
      return defaultValue;
    }
  }

  private static void recordError(String code, String message) {
    LOAD_ERRORS.add(new ConfigError(code, message));
  }

  private static final class ConfigError {
    private final String code;
    private final String message;

    private ConfigError(String code, String message) {
      this.code = code;
      this.message = message;
    }
  }
}
