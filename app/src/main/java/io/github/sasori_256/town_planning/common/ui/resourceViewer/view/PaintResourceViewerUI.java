package io.github.sasori_256.town_planning.common.ui.resourceViewer.view;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;

import io.github.sasori_256.town_planning.common.ui.ImageManager;
import io.github.sasori_256.town_planning.common.ui.ImageManager.ImageStorage;
import io.github.sasori_256.town_planning.common.ui.resourceViewer.ResourceType;

public class PaintResourceViewerUI {
    private final ImageManager imageManager;
    private final JPanel mapPanel;
    private double UIScale;

    private Map<ResourceType, ResourceViewerPanel> resourcePanels = new HashMap<>();

    public PaintResourceViewerUI(ImageManager imageManager, JPanel mapPanel, double UIScale) {
        this.imageManager = imageManager;
        this.mapPanel = mapPanel;
        this.UIScale = UIScale;
        initUI();
    }

    private void initUI() {
        JPanel resourcePanel = new JPanel();
        resourcePanel.setOpaque(false);
        resourcePanel.setLayout(null);
        resourcePanel.setBounds(10, 10, 150 * ResourceType.values().length, 50);
        mapPanel.add(resourcePanel);

        for (ResourceType type : ResourceType.values()) {
            ImageStorage imageStorage = imageManager.getImageStorage(type.getImageName());
            if (imageStorage == null || imageStorage.getImage() == null || imageStorage.getName().equals("error")) {
                System.err.println("\u001B[31mError: Image not found: " + type.getImageName() + "\u001B[0m");
            } else {
                ResourceViewerPanel panel = new ResourceViewerPanel("0", imageStorage.getImage());
                panel.setBounds(type.ordinal() * 150, 0, 143, 50);
                resourcePanel.add(panel);
                resourcePanels.put(type, panel);
            }
        }
        mapPanel.add(resourcePanel);
    }

}
