package io.github.sasori_256.town_planning.common.ui.resourceViewer;

public enum ResourceType {
    SOUL("soulViewerUI", "魂"),
    RESIDENT("residentViewerUI", "人"),
    DATE("dateViewerUI", "日");

    private final String imageName;
    private final String unit;

    ResourceType(String imageName, String unit) {
        this.imageName = imageName;
        this.unit = unit;
    }

    public String getImageName() {
        return imageName;
    }

    public String getUnit() {
        return unit;
    }
}