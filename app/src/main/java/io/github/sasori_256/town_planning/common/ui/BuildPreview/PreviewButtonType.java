package io.github.sasori_256.town_planning.common.ui.BuildPreview;

;

public enum PreviewButtonType {
  ACCEPT("accept"),
  CANCEL("cancel"),
  ROTATE("rotate");

  private final String imageName;

  PreviewButtonType(String imageName) {
    this.imageName = imageName;
  }

  public String getImageName() {
    return imageName;
  }
}
