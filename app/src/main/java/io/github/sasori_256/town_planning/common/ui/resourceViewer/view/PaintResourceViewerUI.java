package io.github.sasori_256.town_planning.common.ui.resourceViewer.view;

import java.util.HashMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.Box;
import javax.swing.JPanel;

import io.github.sasori_256.town_planning.common.event.EventBus;
import io.github.sasori_256.town_planning.common.event.Subscription;
import io.github.sasori_256.town_planning.common.event.events.SoulChangedEvent;
import io.github.sasori_256.town_planning.common.ui.ImageManager;
import io.github.sasori_256.town_planning.common.ui.ImageManager.ImageStorage;
import io.github.sasori_256.town_planning.common.ui.resourceViewer.ResourceType;
import io.github.sasori_256.town_planning.entity.model.GameContext;

public class PaintResourceViewerUI {
    private final GameContext gameContext;
    private final EventBus eventBus;
    private final ImageManager imageManager;
    private final JPanel mapPanel;
    private double UIScale;

    private Map<ResourceType, ResourceViewerPanel> resourcePanels = new HashMap<>();

    public PaintResourceViewerUI(GameContext gameContext, ImageManager imageManager, JPanel mapPanel, double UIScale) {
        this.gameContext = gameContext;
        this.eventBus = gameContext.getEventBus();
        this.imageManager = imageManager;
        this.mapPanel = mapPanel;
        this.UIScale = UIScale;
        initUI();
        Subscription soulSub = this.eventBus.subscribe(SoulChangedEvent.class, (event) -> {
            ResourceViewerPanel resourcePanel = resourcePanels.get(ResourceType.SOUL);
            if (resourcePanel != null) {
                resourcePanel.setDisplayValue(String.valueOf(event.currentSouls()));
                resourcePanel.repaint();
            }
        });
        // TODO: 他のリソースの購読も追加
    }

    private void initUI() {
        int panelMargin = 10;
        JPanel resourcePanel = new JPanel();
        resourcePanel.setOpaque(false);
        resourcePanel.setLayout(new BoxLayout(resourcePanel, BoxLayout.X_AXIS));
        resourcePanel.setBounds(10, 10, (150 + panelMargin) * ResourceType.values().length, 50);
        resourcePanel.setAlignmentY(JPanel.TOP_ALIGNMENT);
        resourcePanel.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        mapPanel.add(resourcePanel);

        for (ResourceType type : ResourceType.values()) {
            ImageStorage imageStorage = imageManager.getImageStorage(type.getImageName());
            if (imageStorage == null || imageStorage.getImage() == null || imageStorage.getName().equals("error")) {
                System.err.println("\u001B[31mError: Image not found: " + type.getImageName() + "\u001B[0m");
            } else {
                ResourceViewerPanel panel = new ResourceViewerPanel("0", imageStorage.getImage());
                resourcePanel.add(panel);
                resourcePanel.add(Box.createHorizontalStrut(panelMargin));
                resourcePanels.put(type, panel);
            }
        }
        mapPanel.add(resourcePanel);
    }

}
