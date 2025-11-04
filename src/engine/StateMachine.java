package engine;

public final class StateMachine {
    private GameStates current = GameStates.MENU;

    public GameStates get() { return current; }
    public boolean is(GameStates s) { return current == s; }

    public void set(GameStates next) {
        if (next != current) current = next;
    }
}
