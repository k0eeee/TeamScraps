package screen;

import engine.Core;
import engine.GameStates;
import engine.StateMachine;
import engine.InputManager;
import engine.FileManager;
import entity.*;
import engine.SoundManager; // <-- added this for sound

import java.awt.*;



public class GameScreen implements Screen { // implementing screen base
    private final Core core;
    private final StateMachine states;
    private final InputManager input;
    private final FileManager fileManager;

    private final int unit = 20;
    private final int cols = Core.WIDTH / unit;
    private final int rows = Core.HEIGHT / unit;

    private boolean wrap = false; // set false for border walls
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

    public GameScreen(Core core, StateMachine states, InputManager input, FileManager fileManager) {
        this.core = core; // stores reference
        this.states = states;
        this.input = input;
        this.fileManager= fileManager;
        this.soundManager = new SoundManager(); // initialize sound manager

        // ensure effect timers / flags are reset
        this.shakeTime = 0.0;
        this.glitchTime = 0.0;
        this.gameEnding = false;
    }

    private void resetGame() {
        // reset effect timers/flags so no leftover glitch/shake when new game starts
        shakeTime = 0.0;
        glitchTime = 0.0;
        gameEnding = false;

        foodsEaten = 0; // Reset the counter when game starts
        moveAccumulator = 0.0; // reset movement timer
        moveInterval = 0.25;   // reset to base speed
    }
    private void saveScore() {
        String playerName = fileManager.getSetting("player_name", "PLAYER1");
        fileManager.addScore(playerName, foodsEaten);
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
    }

    public void update(double dt) {

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
                soundManager.playEat(); // eating sound
                snake.grow();
                food.respawn(walls, snake, cols, rows);
                updateScore();
                updateSpeedBasedOnLength();   // speed up gradually
                break;

            case HIT_SELF:
            case HIT_WALL:
                // trigger effects
                soundManager.playHit(); // collision sound
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

    public void render(Graphics2D g) {
        if (snake == null || food == null || walls == null) {
            return; // prevent NPE during the first few frames
        }


        // --- Screen Shake ---
        if (shakeTime > 0) {
            int sx = (int)(Math.random() * shakeStrength * 2 - shakeStrength);
            int sy = (int)(Math.random() * shakeStrength * 2 - shakeStrength);
            g.translate(sx, sy);
        }


        // clear background (for now - for visible gameplay)
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, Core.WIDTH, Core.HEIGHT);

        // walls
        if (walls != null) {
            g.setColor(Color.DARK_GRAY);
            for (int x = 0; x < cols; x++)
                for (int y = 0; y < rows; y++)
                    if (walls[x][y]) g.fillRect(x * unit, y * unit, unit, unit);
        }

        // just shows foods eaten - using top border as HUD for now
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 18));

        int hudY = unit - 5; // slightly below top of the first cell (inside the border)
        g.drawString("Foods: " + foodsEaten, 10, hudY);

        // food
        g.setColor(Color.RED);
        g.fillRect(food.getX() * unit, food.getY() * unit, unit, unit);

        // snake
        g.setColor(Color.GREEN);
        snake.rendering(g, unit);

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

}




