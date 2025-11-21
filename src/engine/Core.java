package engine;

import screen.Screen;
import screen.MenuScreen;
import screen.GameScreen;
import screen.GameOverScreen;
import screen.HighScoreScreen;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferStrategy;

public class Core extends Canvas implements Runnable {

    public static final int WIDTH = 800, HEIGHT = 600;
    private static final double FIXED_DT = 1.0 / 60.0;

    // ---- State & subsystems (owned by other roles, just referenced here) ----
    private final StateMachine states = new StateMachine();
    private Screen menuScreen, gameScreen, gameOverScreen, highScoreScreen;   // (chloe)

    // the following were enabled so the rest of script would work - chloe
    private final InputManager input = new InputManager(this); // (melih)
    private final SoundManager sound = new SoundManager(); // (ashley)
    private final FileManager files = new FileManager(); // (dami)

    private JFrame frame;
    private volatile boolean running = false;

    public static void main(String[] args) { new Core().start(); }

    public Core() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setIgnoreRepaint(true);
        setFocusable(true);      // required on some systems
    }

    public synchronized void start() {
        if (running) return;
        running = true;

        frame = new JFrame("Snake");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.add(this);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        requestFocus(); // ensure keyboard focus
        requestFocusInWindow();  // helps ensure input grabs focus

        initScreens(); // initialising screens

        new Thread(this, "GameLoop").start();
    }

    // screen constructors to register within the state machine
    private void initScreens() {
        menuScreen = new MenuScreen(this, states, files);
        gameScreen = new GameScreen(this, states, input, files);
        gameOverScreen = new GameOverScreen(this, states);
        highScoreScreen = new HighScoreScreen(this, states, files);

        // state machine initial state
        states.set(GameStates.MENU);
        menuScreen.onEnter();
    }

    @Override
    public void run() {
        createBufferStrategy(2);
        BufferStrategy bs = getBufferStrategy();

        long prev = System.nanoTime();
        double acc = 0;

        while (running) {
            long now = System.nanoTime();
            double dt = (now - prev) / 1_000_000_000.0;
            prev = now;
            acc += dt;

            // run all fixed-step updates
            while (acc >= FIXED_DT) {
                currentScreen().update(FIXED_DT); // drive current screen logic
                acc -= FIXED_DT;
            }

            input.poll(); // (melih) - enabled by chloe so controls work

            do {
                do {
                    Graphics2D g = (Graphics2D) bs.getDrawGraphics();
                    g.setColor(Color.BLACK);
                    g.fillRect(0, 0, WIDTH, HEIGHT);

                    currentScreen().render(g); // draw current screen

                    g.dispose();
                } while (bs.contentsRestored());
                bs.show();
            } while (bs.contentsLost());
        }
    }

    // used for transitions - decide which screen is active (based on GameStates)
    private Screen currentScreen() {
        switch (states.get()) {
            case MENU:
                return menuScreen;
            case PLAYING:
                return gameScreen;
            case PAUSED:
                return gameScreen; // game renders under pause overlay
            case GAME_OVER:
                return gameOverScreen;
            case HIGHSCORES:
                return highScoreScreen;
            default:
                return menuScreen;
        }
    }

    // transition helpers included
    public void toMenu() {
        transition(GameStates.MENU);
    }

    public void toPlaying() {
        transition(GameStates.PLAYING);
    }

    public void toGameOver() {
        transition(GameStates.GAME_OVER);
    }

    public void toHighScores() {
        transition(GameStates.HIGHSCORES);
    }

    public void togglePause() {
        if (states.is(GameStates.PLAYING)) transition(GameStates.PAUSED);
        else if (states.is(GameStates.PAUSED)) transition(GameStates.PLAYING);
    }

    // for menu button - handling state change
    private void transition(GameStates next) {
        System.out.println("Transitioning from " + states.get() + " to " + next); // debug

        currentScreen().onExit();   // screen handles cleanup
        states.set(next);
        currentScreen().onEnter();  // screen handles setup
    }

    /* helper for later when ashley adds sounds - to keep constructors valid
    public SoundManager getSound() {
        return sound;
    } */
}

