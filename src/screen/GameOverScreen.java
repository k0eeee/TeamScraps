package screen;

import engine.Core;
import engine.StateMachine;
import engine.GameStates;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;

public class GameOverScreen implements Screen, MouseListener {

    private final Core core;
    private final StateMachine states;

    private Rectangle retryBtn;
    private Rectangle menuBtn;

    public GameOverScreen(Core core, StateMachine states) { // constructor
        this.core = core;
        this.states = states;
        initButtons();
    }

    private void initButtons() {
        int btnWidth = 220;
        int btnHeight = 50;
        int centerX = Core.WIDTH / 2 - btnWidth / 2;
        int firstY = 280;

        retryBtn = new Rectangle(centerX, firstY, btnWidth, btnHeight);
        menuBtn  = new Rectangle(centerX, firstY + 70, btnWidth, btnHeight);
    }

    @Override
    public void onEnter() {
        core.addMouseListener(this);
        core.requestFocusInWindow();
    }

    @Override
    public void onExit() {
        core.removeMouseListener(this);
    }

    @Override
    public void update(double dt) { // no per-frame logic for now
    }

    @Override
    public void render(Graphics2D g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, Core.WIDTH, Core.HEIGHT);

        g.setColor(Color.RED);
        g.setFont(new Font("Arial", Font.BOLD, 48));
        drawCentered(g, "GAME OVER", Core.WIDTH, 160);

        drawButton(g, retryBtn, "Play Again");
        drawButton(g, menuBtn, "Main Menu");
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

    // MouseListener
    @Override
    public void mouseClicked(MouseEvent e) {
        Point p = e.getPoint();
        if (retryBtn.contains(p)) {
            core.toPlaying();  // restart game (GameScreen.onEnter runs)
        } else if (menuBtn.contains(p)) {
            core.toMenu();     // back to menu
        }
    }

    @Override public void mousePressed(MouseEvent e) { }
    @Override public void mouseReleased(MouseEvent e) { }
    @Override public void mouseEntered(MouseEvent e) { }
    @Override public void mouseExited(MouseEvent e) { }
}
