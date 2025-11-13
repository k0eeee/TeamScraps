package screen;

import engine.FileManager;
import entity.Score;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Screen to display high scores
 */
public class HighScoreScreen extends JPanel {
    private FileManager fileManager;
    private JButton backButton;

    public HighScoreScreen(FileManager fileManager) {
        this.fileManager = fileManager;
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBackground(Color.BLACK);

        // Title
        JLabel titleLabel = new JLabel("HIGH SCORES", SwingConstants.CENTER);
        titleLabel.setForeground(Color.GREEN);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 36));
        add(titleLabel, BorderLayout.NORTH);

        // Scores display
        JPanel scoresPanel = createScoresPanel();
        add(scoresPanel, BorderLayout.CENTER);

        // Back button
        backButton = new JButton("Back to Menu");
        backButton.setFont(new Font("Arial", Font.BOLD, 16));
        backButton.setBackground(Color.DARK_GRAY);
        backButton.setForeground(Color.WHITE);
        add(backButton, BorderLayout.SOUTH);
    }

    private JPanel createScoresPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.BLACK);

        DefaultListModel<String> listModel = new DefaultListModel<>();
        JList<String> scoreList = new JList<>(listModel);
        scoreList.setBackground(Color.DARK_GRAY);
        scoreList.setForeground(Color.WHITE);
        scoreList.setFont(new Font("Monospaced", Font.BOLD, 16));
        scoreList.setSelectionBackground(Color.GREEN);
        scoreList.setSelectionForeground(Color.BLACK);

        // Load scores
        List<Score> scores = fileManager.getHighScores();

        if (scores.isEmpty()) {
            listModel.addElement("   No scores yet!   ");
            listModel.addElement("   Play the game to set records!   ");
        } else {
            for (int i = 0; i < scores.size(); i++) {
                Score score = scores.get(i);
                String display = String.format("%2d. %-30s %6d",
                        i + 1, score.getPlayerName(), score.getScore());
                listModel.addElement(display);
                // Show foods eaten on second line
                listModel.addElement(String.format("    %d foods eaten", score.getFoodsEaten()));
            }
        }

        JScrollPane scrollPane = new JScrollPane(scoreList);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.GREEN, 2));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    public JButton getBackButton() {
        return backButton;
    }

    /**
     * Refresh the high scores display
     */
    public void refreshScores() {
        removeAll();
        initializeUI();
        revalidate();
        repaint();
    }
}