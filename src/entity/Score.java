package entity;

import java.io.Serializable;

/**
 * Represents a game score with player information and timestamp
 * Handles scoring logic for single player Snake game
 */
public class Score implements Serializable, Comparable<Score> {
    private static final long serialVersionUID = 1L;

    private String playerName;
    private int score;
    private int foodsEaten;
    private long timestamp;

    public Score(String playerName, int score, int foodsEaten) {
        this.playerName = playerName;
        this.score = score;
        this.foodsEaten = foodsEaten;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters
    public String getPlayerName() { return playerName; }
    public int getScore() { return score; }
    public int getFoodsEaten() { return foodsEaten; }
    public long getTimestamp() { return timestamp; }

    /**
     * Format score for display
     */
    public String getFormattedScore() {
        return String.format("%s: %d (%d foods)", playerName, score, foodsEaten);
    }

    /**
     * Compare scores for sorting (highest first)
     */
    @Override
    public int compareTo(Score other) {
        return Integer.compare(other.score, this.score); // Descending order
    }

    @Override
    public String toString() {
        return String.format("Score{player='%s', score=%d, foods=%d}",
                playerName, score, foodsEaten);
    }
}