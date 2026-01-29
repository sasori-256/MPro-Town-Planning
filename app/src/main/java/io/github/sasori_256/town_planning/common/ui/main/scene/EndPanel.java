package io.github.sasori_256.town_planning.common.ui.main.scene;

import java.awt.Color;
import java.awt.Font;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import io.github.sasori_256.town_planning.common.ui.main.GameFlowNavigator;
import io.github.sasori_256.town_planning.common.ui.main.GameResult;

public class EndPanel extends JPanel {
  private final GameFlowNavigator navigator;
  private final JLabel dayValue;
  private final JLabel soulValue;
  private final JLabel maxPopulationValue;
  private final JLabel totalDeathsValue;

  public EndPanel(GameFlowNavigator navigator) {
    this.navigator = navigator;
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    setBackground(Color.BLACK);
    setFocusable(false);
    setOpaque(true);

    JLabel titleLabel = new JLabel("Game Over");
    titleLabel.setForeground(Color.WHITE);
    titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 32f));
    titleLabel.setAlignmentX(CENTER_ALIGNMENT);

    add(Box.createVerticalStrut(40));
    add(titleLabel);
    add(Box.createVerticalStrut(24));

    dayValue = buildStatLine("経過日数", "-");
    soulValue = buildStatLine("魂", "-");
    maxPopulationValue = buildStatLine("最大人口", "-");
    totalDeathsValue = buildStatLine("累計死亡数", "-");

    add(Box.createVerticalStrut(16));
    add(buildActionRow());
  }

  public void setResult(GameResult result) {
    if (result == null) {
      return;
    }
    setResult(result.day(), result.soul(), result.maxPopulation(), result.totalDeaths());
  }

  public void setResult(int day, int soul, int maxPopulation, int totalDeaths) {
    dayValue.setText(String.valueOf(day));
    soulValue.setText(String.valueOf(soul));
    maxPopulationValue.setText(String.valueOf(maxPopulation));
    totalDeathsValue.setText(String.valueOf(totalDeaths));
  }

  private JPanel buildActionRow() {
    JPanel row = new JPanel();
    row.setOpaque(false);
    row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
    row.setAlignmentX(CENTER_ALIGNMENT);

    JButton titleButton = new JButton("タイトルへ");
    titleButton.addActionListener(e -> navigator.goToTitle());

    JButton restartButton = new JButton("もう一度");
    restartButton.addActionListener(e -> navigator.startNewGame());

    row.add(titleButton);
    row.add(Box.createHorizontalStrut(12));
    row.add(restartButton);
    return row;
  }

  private JLabel buildStatLine(String label, String value) {
    JPanel line = new JPanel();
    line.setOpaque(false);
    line.setLayout(new BoxLayout(line, BoxLayout.X_AXIS));
    line.setAlignmentX(CENTER_ALIGNMENT);

    JLabel labelView = new JLabel(label + ": ");
    labelView.setForeground(Color.WHITE);
    labelView.setFont(labelView.getFont().deriveFont(Font.BOLD, 18f));

    JLabel valueView = new JLabel(value);
    valueView.setForeground(Color.WHITE);
    valueView.setFont(valueView.getFont().deriveFont(Font.PLAIN, 18f));

    line.add(labelView);
    line.add(valueView);
    add(line);
    add(Box.createVerticalStrut(12));

    return valueView;
  }
}
