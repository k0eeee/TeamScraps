package engine;

/* Chloe
import screen.Screen;
import screen.MenuScreen;
import screen.GameScreen;
import screen.GameOverScreen; */

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferStrategy;

public class Core extends Canvas implements Runnable {

    public static final int WIDTH = 800, HEIGHT = 600;
    private static final double FIXED_DT = 1.0 / 60.0;

    // ---- State & subsystems (owned by other roles, just referenced here) ----
    private final StateMachine states = new StateMachine();
    //private Screen menuScreen, gameScreen, gameOverScreen;   // (chloe)
    //private final InputManager input = new InputManager(this); // (melih)
    //private final SoundManager sound = new SoundManager();     // (ashley)
    //private final FileManager files = new FileManager();       // (dami)

    private JFrame frame;
    private volatile boolean running = false;

    //public static void main(String[] args) { new Core().start(); }

    public Core() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setIgnoreRepaint(true);
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

        initScreens();

        new Thread(this, "GameLoop").start();
    }

    private void initScreens() {
        /* (Chloe) must provide these constructors
        menuScreen     = new MenuScreen(this, states, input, sound, files);
        gameScreen     = new GameScreen(this, states, input, sound, files);
        gameOverScreen = new GameOverScreen(this, states, input, sound, files);

        menuScreen.onEnter();*/
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

            //input.poll(); // (melih)

            while (acc >= FIXED_DT) {
                //currentScreen().update(FIXED_DT);
                acc -= FIXED_DT;
            }

            do {
                do {
                    Graphics2D g = (Graphics2D) bs.getDrawGraphics();
                    g.setColor(Color.BLACK);
                    g.fillRect(0, 0, WIDTH, HEIGHT);

                    //currentScreen().render(g);

                    g.dispose();
                } while (bs.contentsRestored());
                bs.show();
            } while (bs.contentsLost());
        }
    }

    /*private Screen currentScreen() {
        switch (states.get()) {
            case MENU:      return menuScreen;
            case PLAYING:   return gameScreen;
            case PAUSED:    return gameScreen; // game renders under pause overlay
            case GAME_OVER: return gameOverScreen;
            default:        return menuScreen;
        }*/ //
    }

    /*public void toMenu()     { transition(GameStates.MENU); }
    public void toPlaying()  { transition(GameStates.PLAYING); }
    public void toGameOver() { transition(GameStates.GAME_OVER); }
    public void togglePause(){
        if (states.is(GameStates.PLAYING)) transition(GameStates.PAUSED);
        else if (states.is(GameStates.PAUSED)) transition(GameStates.PLAYING);
    }*/

    /*private void transition(GameStates next) {
        currentScreen().onExit();   // (chloe) screen handles cleanup
        states.set(next);
        currentScreen().onEnter();  //  (chloe) screen handles setup
    }*/

