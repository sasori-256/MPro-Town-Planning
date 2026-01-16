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

/**
 * リソースビューアのUIを担当するクラス。
 * 魂、人数、経過日数を表示する。
 */
public class PaintResourceViewerUI extends JPanel {
    private final GameContext gameContext;
    private final EventBus eventBus;
    private final ImageManager imageManager;
    private double uiScale;

    private Map<ResourceType, ResourceViewerPanel> resourcePanels = new HashMap<>();

    public PaintResourceViewerUI(GameContext gameContext, ImageManager imageManager, double uiScale) {
        this.gameContext = gameContext;
        this.eventBus = gameContext.getEventBus();
        this.imageManager = imageManager;
        this.uiScale = uiScale;
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
        this.setOpaque(false);
        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.setBounds(10, 10, (150 + panelMargin) * ResourceType.values().length, 50);
        this.setAlignmentY(JPanel.TOP_ALIGNMENT);
        this.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        for (ResourceType type : ResourceType.values()) {
            ImageStorage imageStorage = imageManager.getImageStorage(type.getImageName());
            if (imageStorage == null || imageStorage.getImage() == null || imageStorage.getName().equals("error")) {
                System.err.println("\u001B[31mError: Image not found: " + type.getImageName() + "\u001B[0m");
            } else {
                ResourceViewerPanel panel = new ResourceViewerPanel("0", imageStorage.getImage());
                this.add(panel);
                this.add(Box.createHorizontalStrut(panelMargin));
                resourcePanels.put(type, panel);
            }
        }
    }

}
