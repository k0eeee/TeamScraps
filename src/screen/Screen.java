package screen;

import java.awt.Graphics2D;

// common interface made as base for other screen classes
public interface Screen {
    void onEnter();
    void onExit();
    void update(double dt);
    void render(Graphics2D g);
}
