package screen;

import engine.Core;
import engine.GameStates;
import engine.StateMachine;
import engine.FileManager;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;

// menu implementation
public class MenuScreen implements Screen, MouseListener {

    private final Core core;
    private final StateMachine states;
    private final FileManager files; // not used yet, but available if needed later

    // simple button hitboxes
    private Rectangle startBtn;
    private Rectangle highScoreBtn;
    private Rectangle quitBtn;

    public MenuScreen(Core core, StateMachine states, FileManager files) { // constructor
        this.core = core;
        this.states = states;
        this.files = files;
        initButtons();
    }

    // layout buttons
    private void initButtons() {
        int btnWidth = 220;
        int btnHeight = 50;
        int centerX = Core.WIDTH / 2 - btnWidth / 2;
        int firstY = 260;

        startBtn      = new Rectangle(centerX, firstY, btnWidth, btnHeight);
        highScoreBtn  = new Rectangle(centerX, firstY + 70, btnWidth, btnHeight);
        quitBtn       = new Rectangle(centerX, firstY + 140, btnWidth, btnHeight);
    }

    @Override

    public void onEnter() {
        core.addMouseListener(this); // start listening for clicks
    }

    @Override
    public void onExit() {
        core.removeMouseListener(this); // stop listening when leaving menu
    }

    @Override
    public void update(double dt) {
        // no per-frame logic needed for static menu right now
    }

    @Override
    public void render(Graphics2D g) {
        // background
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, Core.WIDTH, Core.HEIGHT);

        // title
        g.setColor(Color.GREEN);
        g.setFont(new Font("Arial", Font.BOLD, 56));
        drawCentered(g, "SNAKE", Core.WIDTH, 140);

        // buttons
        drawButton(g, startBtn, "Start Game");
        drawButton(g, highScoreBtn, "High Scores");
        drawButton(g, quitBtn, "Quit");
    }

    private void drawButton(Graphics2D g, Rectangle rect, String text) {
        g.setColor(Color.DARK_GRAY);
        g.fill(rect);
        g.setColor(Color.WHITE);
        g.draw(rect);

        g.setFont(new Font("Arial", Font.PLAIN, 24));
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

    // --- MouseListener implementation ---

    @Override
    public void mouseClicked(MouseEvent e) {
        System.out.println("CLICK at " + e.getPoint());  // debug line

        Point p = e.getPoint();

        if (startBtn.contains(p)) {
            core.requestFocusInWindow();
            core.toPlaying();  // start the game
        } else if (highScoreBtn.contains(p)) {
            core.toHighScores();  // go to high score screen
        } else if (quitBtn.contains(p)) {
            System.exit(0); // exit game
        }
    }

    @Override public void mousePressed(MouseEvent e) { }
    @Override public void mouseReleased(MouseEvent e) { }
    @Override public void mouseEntered(MouseEvent e) { }
    @Override public void mouseExited(MouseEvent e) { }
}
