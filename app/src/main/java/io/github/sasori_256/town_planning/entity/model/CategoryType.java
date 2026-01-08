package io.github.sasori_256.town_planning.entity.model;

public enum CategoryType {
    RESIDENTIAL("住居"),
    RELIGIOUS("宗教施設"),
    CEMETERY("墓地"),
    INFRASTRUCTURE("インフラ"),
    NONE("なし"),
    METEOR("隕石"),
    PLAGUE("疫病");

    private final String displayName;

    CategoryType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
