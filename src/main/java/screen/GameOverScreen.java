package screen;

import engine.Core;
import engine.StateMachine;
import engine.GameStates;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import engine.SpriteLoader;
import java.awt.image.BufferedImage;
import java.awt.RenderingHints;

import java.awt.Font;
import java.io.FileInputStream;
import java.io.InputStream;
import java.awt.geom.Rectangle2D;



public class GameOverScreen implements Screen, MouseListener {

    private final Core core;
    private final StateMachine states;

    private Rectangle retryBtn;
    private Rectangle menuBtn;
    private BufferedImage gameOverBackground;

    private Font titleFont;
    private Font buttonFont;
    private BufferedImage stoneTile;



    public GameOverScreen(Core core, StateMachine states) { // constructor
        this.core = core;
        this.states = states;
        loadFonts();
        initButtons();

        stoneTile = SpriteLoader.load("resources/sprites/button.png");
        gameOverBackground = SpriteLoader.load("resources/backgrounds/end.jpg");
    }

    private void loadFonts() {
        try (InputStream in = new FileInputStream("resources/fonts/alagard.ttf")) {
            Font base = Font.createFont(Font.TRUETYPE_FONT, in);
            titleFont  = base.deriveFont(Font.BOLD, 80f);  // "GAME OVER"
            buttonFont = base.deriveFont(Font.PLAIN, 26f); // buttons
        } catch (Exception e) {
            titleFont  = new Font("Arial", Font.BOLD, 80);
            buttonFont = new Font("Arial", Font.PLAIN, 26);
        }
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

        // make absolutely sure BGM is stopped when we arrive here
        core.getSound().stopBGM();
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
        // keep pixel art crisp
        g.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
        );

        // background image
        if (gameOverBackground != null) {
            g.drawImage(gameOverBackground, 0, 0, Core.WIDTH, Core.HEIGHT, null);
        } else {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, Core.WIDTH, Core.HEIGHT);
        }

        // ----- GAME OVER title -----
        String text = "GAME OVER";
        g.setFont(titleFont);
        FontMetrics fm = g.getFontMetrics();
        int x = (Core.WIDTH - fm.stringWidth(text)) / 2;
        int y = 150;

        // glow shadow
        g.setColor(new Color(0, 0, 0, 200));
        g.drawString(text, x - 3, y + 3);

        // main red text
        g.setColor(new Color(230, 40, 40));
        g.drawString(text, x, y);


        drawButton(g, retryBtn, "Play Again");
        drawButton(g, menuBtn, "Main Menu");
    }

    private void drawButton(Graphics2D g, Rectangle rect, String text) {

        // draw stone tile background
        if (stoneTile != null) {
            g.drawImage(stoneTile,
                    rect.x, rect.y,
                    rect.x + rect.width, rect.y + rect.height,
                    0, 0, stoneTile.getWidth(), stoneTile.getHeight(),
                    null);
        } else {
            g.setColor(new Color(40, 40, 40, 220));
            g.fill(rect);
        }

        // dark outer border
        g.setColor(new Color(20, 20, 20));
        g.drawRect(rect.x, rect.y, rect.width, rect.height);

        // light inner highlight border
        g.setColor(new Color(130, 130, 130));
        g.drawRect(rect.x + 2, rect.y + 2, rect.width - 4, rect.height - 4);

        // button text
        g.setFont(buttonFont);
        g.setColor(new Color(235, 235, 235)); // off-white
        FontMetrics fm = g.getFontMetrics();
        int tx = rect.x + (rect.width - fm.stringWidth(text)) / 2;
        int ty = rect.y + (rect.height + fm.getAscent()) / 2 - 3;

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
