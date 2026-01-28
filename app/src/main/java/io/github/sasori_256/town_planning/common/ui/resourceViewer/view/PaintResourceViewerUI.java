package io.github.sasori_256.town_planning.common.ui.resourceViewer.view;

import java.awt.Image;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.Box;
import javax.swing.JPanel;

import io.github.sasori_256.town_planning.common.event.EventBus;
import io.github.sasori_256.town_planning.common.event.Subscription;
import io.github.sasori_256.town_planning.common.event.events.DayPassedEvent;
import io.github.sasori_256.town_planning.common.event.events.ResidentDiedEvent;
import io.github.sasori_256.town_planning.common.event.events.ResidentBornEvent;
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
    private final EventBus eventBus = EventBus.getInstance();
    private final ImageManager imageManager;
    private double uiScale;

    private Map<ResourceType, ResourceViewerPanel> resourcePanels = new HashMap<>();

    public PaintResourceViewerUI(GameContext gameContext, ImageManager imageManager, double uiScale) {
        this.gameContext = gameContext;
        this.imageManager = imageManager;
        this.uiScale = uiScale;
        initUI();
        Subscription soulSub = this.eventBus.subscribe(SoulChangedEvent.class, (event) -> {
            updateValue(ResourceType.SOUL, String.valueOf(event.currentSoul()));
        });
        Subscription bornSub = this.eventBus.subscribe(ResidentBornEvent.class, (event) -> {
            updateValue(ResourceType.RESIDENT, String.valueOf(event.populationAlive()));
        });
        Subscription diedSub = this.eventBus.subscribe(ResidentDiedEvent.class, (event) -> {
            updateValue(ResourceType.RESIDENT, String.valueOf(event.populationAlive()));
        });
        Subscription daySub = this.eventBus.subscribe(DayPassedEvent.class, (event) -> {
            updateValue(ResourceType.DATE, String.valueOf(event.dayNumber()));
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

        // int defaultPanelWidth = 150;
        // int totalWidth = 0;
        for (ResourceType type : ResourceType.values()) {
            ImageStorage imageStorage = imageManager.getImageStorage(type.getImageName());
            if (imageStorage == null || imageStorage.getImage() == null || imageStorage.getName().equals("error")) {
                System.err.println("\u001B[31mError: Image not found: " + type.getImageName() + "\u001B[0m");
            } else {
                Image panelImage = imageStorage.getImage();
                // int panelWidth = panelImage.getWidth(null) > 0 ? panelImage.getWidth(null) :
                // defaultPanelWidth;
                // totalWidth += panelWidth + panelMargin;
                String initialValue = resolveInitialValue(type);
                ResourceViewerPanel panel = new ResourceViewerPanel(initialValue, type.getUnit(), panelImage);
                this.add(panel);
                this.add(Box.createHorizontalStrut(panelMargin));
                resourcePanels.put(type, panel);
            }
        }
        // this.setBounds(10, 10, totalWidth, 50);
    }

    private String resolveInitialValue(ResourceType type) {
        if (type == ResourceType.SOUL) {
            return String.valueOf(gameContext.getSoul());
        }
        if (type == ResourceType.RESIDENT) {
            return String.valueOf(gameContext.getPopulationAlive());
        }
        if (type == ResourceType.DATE) {
            return String.valueOf(gameContext.getDay());
        }
        return "0";
    }

    private void updateValue(ResourceType type, String value) {
        ResourceViewerPanel panel = resourcePanels.get(type);
        if (panel == null) {
            return;
        }
        if (javax.swing.SwingUtilities.isEventDispatchThread()) {
            panel.setDisplayValue(value, type.getUnit());
            panel.repaint();
        } else {
            javax.swing.SwingUtilities.invokeLater(() -> {
                panel.setDisplayValue(value, type.getUnit());
                panel.repaint();
            });
        }
    }

}
