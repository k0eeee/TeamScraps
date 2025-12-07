package screen;

import engine.Core;
import engine.GameStates;
import engine.StateMachine;
import engine.InputManager;
import engine.FileManager;
import entity.*;
import engine.SoundManager; // <-- added this for sound
import javax.swing.SwingUtilities;
import javax.swing.JOptionPane;
import engine.SpriteLoader;
import java.awt.image.BufferedImage;
import java.awt.RenderingHints;
import java.util.List;

import java.awt.Font;
import java.io.FileInputStream;
import java.io.InputStream;

import java.awt.*;



public class GameScreen implements Screen { // implementing screen base
    private final Core core;
    private final StateMachine states;
    private final InputManager input;
    private final FileManager fileManager;

    // sprite graphics
    private BufferedImage headUp;
    private BufferedImage headDown;
    private BufferedImage headLeft;
    private BufferedImage headRight;
    private BufferedImage bodyH;    // horizontal neck
    private BufferedImage bodyV;    // vertical neck
    private BufferedImage foodSprite;
    private BufferedImage backgroundSprite;
    private BufferedImage wallSprite;
    private Font hudFont;

    private final int unit = 48;
    private final int cols = Core.WIDTH / unit;
    private final int rows = Core.HEIGHT / unit;

    private final  boolean wrap = false; // set false for border walls
    private boolean[][] walls;             // environment grid
    private final CollisionHandler collider = new CollisionHandler();

    private Snake snake;
    private Food food;

    // --- Screen Shake ---
    private double shakeTime = 0.0;
    private final double shakeDuration = 0.2;  // 0.2s of shake
    private final int shakeStrength = 5;       // shake strength in pixels

    // --- CRT Glitch ---
    private double glitchTime = 0.0;
    private final double glitchDuration = 0.25;  // glitch lasts 0.25s

    // --- surprise head-flip message ---
    private double flipMessageTime = 0.0;   // seconds to show "SNAKE REVERSED!"

    // --- random micro shake/glitch event ---
    private double randomEventTimer = 0.0;  // counts down to next random event

    // --- random blackout glitch ---
    private double blackoutTime = 0.0;        // how long screen stays black (seconds)
    private double nextBlackoutTimer = 0.0;   // countdown until next possible blackout

    // Prevent immediate multiple game-over transitions
    private boolean gameEnding = false;
    private final int GAME_OVER_DELAY_MS = 300; // delay for effects before changing screen (300 ms)

    // sound manager instance
    private SoundManager soundManager;

    // SCORE VARIABLES
    private int foodsEaten;

    // movement timing – controls snake speed
    private double moveAccumulator = 0.0;
    private double moveInterval = 0.25; // seconds between moves at start (slower)

    private void updateScore() {
        foodsEaten++; // counting the score
    }

    public GameScreen(Core core, StateMachine states, InputManager input, FileManager fileManager,SoundManager soundManager) {
        this.core = core; // stores reference
        this.states = states;
        this.input = input;
        this.fileManager = fileManager;
        this.soundManager = soundManager; // initialize sound manager

        // ensure effect timers / flags are reset
        this.shakeTime = 0.0;
        this.glitchTime = 0.0;
        this.gameEnding = false;

        // load graphics
        loadFonts();
        loadSprites();
    }

    private void resetGame() {
        // reset effect timers/flags so no leftover glitch/shake when new game starts
        shakeTime = 0.0;
        glitchTime = 0.0;
        gameEnding = false;

        flipMessageTime = 0.0; // clear “snake reversed” message
        randomEventTimer = 4.0 + Math.random() * 5.0; // first random event 3–8s from now

        // schedule first blackout 6–12 seconds from now
        blackoutTime = 0.0;
        nextBlackoutTimer = 6.0 + Math.random() * 6.0;

        foodsEaten = 0; // Reset the counter when game starts
        moveAccumulator = 0.0; // reset movement timer
        moveInterval = 0.25;   // reset to base speed
    }
    private void saveScore() {
        final int finalScore = foodsEaten;

        SwingUtilities.invokeLater(() -> {
            // This version handles the Object return properly
            Object response = JOptionPane.showInputDialog(
                    "Enter your name for score: " + finalScore
            );

            String playerName = "Player"; // Default

            if (response != null) {
                String name = response.toString().trim();
                if (!name.isEmpty()) {
                    playerName = name;
                }
            }

            fileManager.addScore(playerName, finalScore);
        });
    }

    @Override
    public void onEnter() {
        input.reset();
        snake = new Snake();
        soundManager.playBGM(); // start background music
        buildMapEasy();
        ensureSafeStartDirection(); // fix snake direction once spawned
        food = new Food();
        food.respawn(walls, snake, cols, rows);
        resetGame();//reset score when game starts
    }

    public void onExit() {
        soundManager.stopBGM(); // stop background music when leaving screen

        // ensure no lingering visual effects
        shakeTime = 0.0;
        glitchTime = 0.0;
        gameEnding = false;

        flipMessageTime = 0.0;   // ensure cleared when leaving the screen
        blackoutTime = 0.0;
    }

    public void update(double dt) {
        // flip warning fading out
        if (flipMessageTime > 0) {
            flipMessageTime -= dt;
            if (flipMessageTime < 0) flipMessageTime = 0;
        }

        // update blackout countdown and duration (only while actually playing)
        if (!gameEnding && !states.is(GameStates.PAUSED)) {

            // countdown to next possible blackout
            nextBlackoutTimer -= dt;

            if (nextBlackoutTimer <= 0) {
                // chance to actually trigger blackout when timer hits zero
                double blackoutChance = 0.35;    // 35% chance, adjust as you like
                if (Math.random() < blackoutChance) {
                    blackoutTime = 0.4;         // blackout lasts ~0.4 seconds
                }
                // schedule next window 6–12 seconds from now
                nextBlackoutTimer = 6.0 + Math.random() * 6.0;
            }
        }

        // countdown active blackout (separate from scheduling)
        if (blackoutTime > 0) {
            blackoutTime -= dt;
            if (blackoutTime < 0) blackoutTime = 0;
        }

        // random small shake/glitch events (only while actually playing)
        if (!gameEnding && !states.is(GameStates.PAUSED)) {
            randomEventTimer -= dt;
            if (randomEventTimer <= 0) {
                // 35% chance to actually fire when the timer expires
                if (Math.random() < 0.35) {
                    // short, small effect – use Math.max so we don't override a bigger collision shake
                    shakeTime  = Math.max(shakeTime,  0.15);
                    glitchTime = Math.max(glitchTime, 0.15);
                }
                // schedule next window 3–8 seconds from now
                randomEventTimer = 4.0 + Math.random() * 5.0;
            }
        }

        if (gameEnding) {
            // allow only effect timers to tick; skip movement/input handling
            // still subtract dt from shakeTime/glitchTime below as you already do
            // optionally return early so snake doesn't move during end effect
            return;
        }

        if (snake == null || food == null || walls == null) return;
        if (states.is(GameStates.PAUSED)) return;

        Snake.Direction nd = input.consumeDirectionChange();
        if (nd != null) { snake.setDirection(nd); }

        // accumulate time and move only when enough time has passed
        moveAccumulator += dt;
        if (moveAccumulator < moveInterval) {
            return; // not time to move yet
        }
        moveAccumulator -= moveInterval;

        // actually move the snake one cell
        snake.move();

        soundManager.playMove(); // play movement sound

        CollisionHandler.Result r =
                collider.check(snake, food, walls, wrap, cols, rows);

        switch (r) {
            case ATE_FOOD:
                soundManager.playEat();
                snake.grow();
                food.respawn(walls, snake, cols, rows);
                updateScore();
                updateSpeedBasedOnLength();

                maybeFlipSnake();    // <--- surprise event
                break;


            case HIT_SELF:
            case HIT_WALL:
                // trigger effects
                soundManager.playHit(); // collision sound
                soundManager.stopBGM();    // stop music immediately on death
                shakeTime = shakeDuration;   // start shake
                glitchTime = glitchDuration; // start glitch
                saveScore();

                // prevent multiple transitions
                if (!gameEnding) {
                    gameEnding = true;

                    // Delay the transition so the shake/glitch effect can be shown for a short moment
                    new Thread(() -> {
                        try {
                            Thread.sleep(GAME_OVER_DELAY_MS); // milliseconds
                        } catch (InterruptedException e) {
                            // ignore
                        }
                        core.toGameOver();
                    }).start();
                }
                break;
            default: /* no-op */ }

        // --- decrease shake + glitch timer ---
        if (shakeTime > 0) {
            shakeTime -= dt;
            if (shakeTime < 0) shakeTime = 0;
        }

        if (glitchTime > 0) {
            glitchTime -= dt;
            if (glitchTime < 0) glitchTime = 0;
        }
    }

    private void loadFonts() {
        try (InputStream in = new FileInputStream("resources/fonts/alagard.ttf")) {
            Font base = Font.createFont(Font.TRUETYPE_FONT, in);
            hudFont = base.deriveFont(Font.PLAIN, 24f);   // tweak size if needed
        } catch (Exception e) {
            hudFont = new Font("Arial", Font.BOLD, 18);
        }
    }

    public void render(Graphics2D g) {
        if (snake == null || food == null || walls == null) {
            return; // prevent NPE during the first few frames
        }

        // keep pixel art crisp
        g.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
        );

        // --- Screen Shake ---
        if (shakeTime > 0) {
            int sx = (int)(Math.random() * shakeStrength * 2 - shakeStrength);
            int sy = (int)(Math.random() * shakeStrength * 2 - shakeStrength);
            g.translate(sx, sy);
        }

        // background
        // base clear behind everything
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, Core.WIDTH, Core.HEIGHT);

        // tile backgroundSprite inside the wall border
        if (backgroundSprite != null) {
            for (int x = 1; x < cols - 1; x++) {          // skipping left/right border
                for (int y = 1; y < rows - 1; y++) {      // skipping top/bottom border
                    g.drawImage(
                            backgroundSprite, x * unit, y * unit, unit, unit, null);
                }
            }
        }

        // walls
        if (walls != null) {
            for (int x = 0; x < cols; x++) {
                for (int y = 0; y < rows; y++) {
                    if (walls[x][y]) {
                        if (wallSprite != null) {
                            g.drawImage(wallSprite, x * unit, y * unit, unit, unit, null);
                        } else {
                            g.setColor(new Color(40, 40, 40));
                            g.fillRect(x * unit, y * unit, unit, unit);
                        }
                    }
                }
            }
        }

        // --- HUD bar for Foods counter ---
        int hudHeight = unit;   // one tile tall

        // dark translucent strip across the top
        g.setColor(new Color(0, 0, 0, 190));
        g.fillRect(0, 0, Core.WIDTH, hudHeight);

        // prepare text
        String hudText = "Foods: " + foodsEaten;
        g.setFont(hudFont);
        FontMetrics hfm = g.getFontMetrics();

        // position (left side, vertically centred in the bar)
        int textX = 16;
        int textY = (hudHeight + hfm.getAscent()) / 2 - 4;

        // outline so it pops on any background
        g.setColor(Color.BLACK);
        g.drawString(hudText, textX - 1, textY);
        g.drawString(hudText, textX + 1, textY);
        g.drawString(hudText, textX,     textY - 1);
        g.drawString(hudText, textX,     textY + 1);

        // main color (matching your green titles)
        g.setColor(new Color(120, 255, 120));
        g.drawString(hudText, textX, textY);


        // food
        int fx = food.getX();
        int fy = food.getY();
        g.drawImage(foodSprite, fx * unit, fy * unit, unit, unit, null);

        // snake as ghost + neck
        List<Segment> body = snake.getBody();  // all segments except head
        Segment head = snake.getHead();

        // draw head
        g.drawImage(getHeadSprite(), head.getX() * unit, head.getY() * unit, unit, unit, null);

        // draw body segments (neck)
        Segment prev = head;
        for (Segment curr : body) {
            BufferedImage bodySprite = getBodySpriteForSegment(prev, curr);
            g.drawImage(bodySprite, curr.getX() * unit, curr.getY() * unit, unit, unit, null);
            prev = curr;
        }

        // --- CRT Glitch ---
        if (glitchTime > 0) {
            Composite old = g.getComposite();

            for (int i = 0; i < 10; i++) {
                int y = (int)(Math.random() * Core.HEIGHT);
                g.setColor(new Color(255, 255, 255, 50));
                g.fillRect(0, y, Core.WIDTH, 2);
            }

            g.setComposite(old);
        }

        if (flipMessageTime > 0) {
            // semi-transparent dark overlay
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect(0, 0, Core.WIDTH, Core.HEIGHT);

            // warning text
            g.setColor(Color.RED);
            g.setFont(hudFont.deriveFont(36f));  // or a fixed Font if you prefer

            String msg = "THE SNAKE REVERSED!";
            FontMetrics fm = g.getFontMetrics();
            int x = (Core.WIDTH  - fm.stringWidth(msg)) / 2;
            int y = (Core.HEIGHT - fm.getHeight()) / 2 + fm.getAscent();

            g.drawString(msg, x, y);

            // --- full-screen blackout glitch ---
            if (blackoutTime > 0) {
                g.setColor(Color.BLACK);
                g.fillRect(0, 0, Core.WIDTH, Core.HEIGHT);
                // no text during blackout – if you want text, draw it here before return
                return;
            }
        }


    }

    // --- simple environment presets (easy). Others can be added later.
    private void buildMapEasy() {
        walls = new boolean[cols][rows];
        if (!wrap) {
            for (int x = 0; x < cols; x++) { walls[x][0] = true; walls[x][rows - 1] = true; }
            for (int y = 0; y < rows; y++) { walls[0][y] = true; walls[cols - 1][y] = true; }
        }
    }

    // making sure the starting direction does NOT point into a wall
    private void ensureSafeStartDirection() {
        if (snake == null || walls == null) return;

        // current head position in grid coordinates
        Segment head = snake.getHead();
        int x = head.getX();
        int y = head.getY();

        // try directions in a reasonable order: first one inside the board and not a wall will be used
        if (isFreeCell(x + 1, y)) {
            snake.setDirection(Snake.Direction.RIGHT);
            return;
        }
        if (isFreeCell(x - 1, y)) {
            snake.setDirection(Snake.Direction.LEFT);
            return;
        }
        if (isFreeCell(x, y - 1)) {
            snake.setDirection(Snake.Direction.UP);
            return;
        }
        if (isFreeCell(x, y + 1)) {
            snake.setDirection(Snake.Direction.DOWN);
        }
    }

    // helper: is a given cell inside the board and not a wall?
    private boolean isFreeCell(int x, int y) {
        if (x < 0 || x >= cols || y < 0 || y >= rows) return false;
        return !walls[x][y];
    }

    // adjusting speed of snake for every 5 foods eaten
    private void updateSpeedBasedOnLength() {
        // each 5 foods -> 1 level faster
        int levels = foodsEaten / 5;      // 0,1,2,3,...

        double baseInterval = 0.25;       // starting speed
        double perLevelDecrease = 0.03;   // how much faster each level
        double minInterval = 0.10;        // fastest allowed

        double newInterval = baseInterval - levels * perLevelDecrease;
        if (newInterval < minInterval) {
            newInterval = minInterval;
        }
        moveInterval = newInterval;
    }

    private void loadSprites() {
        headUp    = SpriteLoader.load("resources/sprites/upH.png");
        headDown  = SpriteLoader.load("resources/sprites/downH.png");
        headLeft  = SpriteLoader.load("resources/sprites/leftH.png");
        headRight = SpriteLoader.load("resources/sprites/rightH.png");

        bodyH     = SpriteLoader.load("resources/sprites/bodyH.png");
        bodyV     = SpriteLoader.load("resources/sprites/bodyV.png");

        foodSprite = SpriteLoader.load("resources/sprites/eye.png");
        backgroundSprite = SpriteLoader.load("resources/sprites/wall2.png");
        wallSprite = SpriteLoader.load("resources/sprites/wall.png");
    }

    private BufferedImage getHeadSprite() {
        Snake.Direction dir = snake.getDirection();
        switch (dir) {
            case UP:    return headUp;
            case DOWN:  return headDown;
            case LEFT:  return headLeft;
            case RIGHT:
            default:    return headRight;
        }
    }

    private BufferedImage getBodySpriteForSegment(Segment prev, Segment curr) {
        int dx = curr.getX() - prev.getX();
        int dy = curr.getY() - prev.getY();

        if (dx != 0) {
            // moved left/right
            return bodyH;
        } else {
            // moved up/down
            return bodyV;
        }
    }

    private void maybeFlipSnake() {
        // not to flip for the first 2 foods
        if (foodsEaten < 3) {
            return;
        }

        // example: 20% chance each time you eat food
        double flipChance = 0.20;

        if (Math.random() < flipChance) {
            snake.reverse();                         // flip head and tail
            shakeTime = shakeDuration * 2.0;         // extra shake
            glitchTime = glitchDuration * 2.0;       // extra glitch
            flipMessageTime = 1.0;                   // show warning for 1 second
        }
    }


}



