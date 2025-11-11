package engine;

import entity.Snake;
import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;
import java.util.Set;

/**
 * Handles keyboard input:
 *  - Arrow keys for movement
 *  - P / ESC to pause or resume
 *  - R to restart
 */
public class InputManager implements KeyListener {

    private final Set<Integer> pressed = new HashSet<>();

    private boolean pauseToggled = false;
    private boolean restartRequested = false;

    private Snake.Direction currentDir = Snake.Direction.RIGHT;
    private Snake.Direction queuedDir = Snake.Direction.RIGHT;

    public InputManager(Component attachTo) {
        attachTo.addKeyListener(this);
        attachTo.setFocusable(true);
        attachTo.requestFocus();
    }

    /** Called once per frame to update input state. */
    public void poll() {
        currentDir = queuedDir;
    }

    public boolean consumePauseToggled() {
        boolean x = pauseToggled;
        pauseToggled = false;
        return x;
    }

    public boolean consumeRestartRequested() {
        boolean x = restartRequested;
        restartRequested = false;
        return x;
    }

    /** Returns the newly requested direction for this frame, or null if none. */
    public Snake.Direction consumeDirectionChange() {
        if (queuedDir != currentDir) {
            return queuedDir;
        }
        return null;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (!pressed.add(code)) return;

        switch (code) {
            // ✅ Only arrow keys for movement
            case KeyEvent.VK_UP:
                requestDirection(Snake.Direction.UP);
                break;
            case KeyEvent.VK_DOWN:
                requestDirection(Snake.Direction.DOWN);
                break;
            case KeyEvent.VK_LEFT:
                requestDirection(Snake.Direction.LEFT);
                break;
            case KeyEvent.VK_RIGHT:
                requestDirection(Snake.Direction.RIGHT);
                break;

            // Pause / Resume
            case KeyEvent.VK_P:
            case KeyEvent.VK_ESCAPE:
                pauseToggled = true;
                break;

            // Restart
            case KeyEvent.VK_R:
                restartRequested = true;
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        pressed.remove(e.getKeyCode());
    }

    @Override
    public void keyTyped(KeyEvent e) { /* not used */ }

    private void requestDirection(Snake.Direction next) {
        if (!isOpposite(currentDir, next)) {
            queuedDir = next;
        }
    }

    private boolean isOpposite(Snake.Direction a, Snake.Direction b) {
        return (a == Snake.Direction.UP && b == Snake.Direction.DOWN) ||
                (a == Snake.Direction.DOWN && b == Snake.Direction.UP) ||
                (a == Snake.Direction.LEFT && b == Snake.Direction.RIGHT) ||
                (a == Snake.Direction.RIGHT && b == Snake.Direction.LEFT);
    }
}
