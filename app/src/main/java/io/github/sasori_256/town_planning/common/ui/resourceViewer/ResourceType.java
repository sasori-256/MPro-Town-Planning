package io.github.sasori_256.town_planning.common.ui.resourceViewer;

public enum ResourceType {
    SOUL("soulViewerUI"),
    RESIDENT("residentViewerUI"),
    DATE("dateViewerUI");

    private final String imageName;

    ResourceType(String imageName) {
        this.imageName = imageName;
    }

    public String getImageName() {
        return imageName;
    }
}