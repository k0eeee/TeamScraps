package screen;

import engine.Core;
import engine.FileManager;
import engine.StateMachine;
import engine.GameStates;
import entity.Score;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import java.util.List;

/**
 * Screen to display high scores
 * (refactored from original JPanel-based version to fit the game loop)
 */
public class HighScoreScreen implements Screen, MouseListener {

    private final Core core;  // used for mouse listener and navigation
    private final StateMachine states;
    private final FileManager fileManager;

    // "Back to Menu" control - now a drawn button instead of JButton
    private Rectangle backBtn;

    // list of scores
    private List<Score> scores;

    public HighScoreScreen(Core core, StateMachine states, FileManager fileManager) {
        this.core = core;
        this.states = states;
        this.fileManager = fileManager;
        initLayout(); // similar role to original initializeUI()
    }

    // sets up button position (instead of Swing layout)
    private void initLayout() {
        int btnWidth = 260;
        int btnHeight = 45;
        int x = Core.WIDTH / 2 - btnWidth / 2;
        int y = Core.HEIGHT - 100;
        backBtn = new Rectangle(x, y, btnWidth, btnHeight);
    }

    @Override
    public void onEnter() {
        // original behaviour: load scores when screen is shown
        scores = fileManager.getHighScores();
        initButtons();
        core.addMouseListener(this);
        core.requestFocusInWindow();
    }

    @Override
    public void onExit() {
        core.removeMouseListener(this);
    }

    private void initButtons() {
        int btnWidth = 220;
        int btnHeight = 50;
        int centerX = Core.WIDTH / 2 - btnWidth / 2;
        int y = Core.HEIGHT - 100;
        backBtn = new Rectangle(centerX, y, btnWidth, btnHeight);
    }

    @Override
    public void update(double dt) {
        // no per-frame logic for now (static screen)
    }

    @Override
    public void render(Graphics2D g) {
        // background similar to original setBackground(Color.BLACK);
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, Core.WIDTH, Core.HEIGHT);

        // title (similar to original JLabel "HIGH SCORES")
        g.setColor(Color.GREEN);
        g.setFont(new Font("Arial", Font.BOLD, 36));
        drawCentered(g, "HIGH SCORES", Core.WIDTH, 80);

        // score list area (replaces JList inside JScrollPane)
        g.setFont(new Font("Monospaced", Font.BOLD, 18));
        g.setColor(Color.WHITE);

        int y = 140;
        int lineHeight = 26;

        if (scores == null || scores.isEmpty()) {
            drawCentered(g, "No scores yet!", Core.WIDTH, y);
        } else {
            int rank = 1;
            for (Score s : scores) {
                String line = String.format("%2d. %-20s %3d foods",
                        rank++, s.getPlayerName(), s.getFoodsEaten());
                g.drawString(line, 80, y);
                y += lineHeight;
                if (y > Core.HEIGHT - 160) break; // simple cutoff
            }
        }

        // "Back to Menu" button (replaces JButton backButton)
        drawButton(g, backBtn, "Back to Menu");
    }

    private void drawButton(Graphics2D g, Rectangle rect, String text) {
        g.setColor(Color.DARK_GRAY);
        g.fill(rect);
        g.setColor(Color.WHITE);
        g.draw(rect);

        g.setFont(new Font("Arial", Font.BOLD, 16));
        FontMetrics fm = g.getFontMetrics();
        Rectangle2D bounds = fm.getStringBounds(text, g);
        int tx = rect.x + (rect.width  - (int) bounds.getWidth())  / 2;
        int ty = rect.y + (rect.height - (int) bounds.getHeight()) / 2 + fm.getAscent();
        g.drawString(text, tx, ty);
    }

    private void drawCentered(Graphics2D g, String text, int width, int y) {
        FontMetrics fm = g.getFontMetrics();
        Rectangle2D bounds = fm.getStringBounds(text, g);
        int x = (int) ((width - bounds.getWidth()) / 2);
        g.drawString(text, x, y);
    }

    // MouseListener – replaces original JButton's ActionListener / backButton.getBackButton()
    @Override
    public void mouseClicked(MouseEvent e) {
        if (backBtn.contains(e.getPoint())) {
            // equivalent to "Back to Menu" from original design
            core.toMenu();
        }
    }

    @Override public void mousePressed(MouseEvent e) { }
    @Override public void mouseReleased(MouseEvent e) { }
    @Override public void mouseEntered(MouseEvent e) { }
    @Override public void mouseExited(MouseEvent e) { }

    /**
     * Refresh the high scores display
     * (preserves original semantic: reload scores data)
     */
    public void refreshScores() {
        scores = fileManager.getHighScores();
    }
}
