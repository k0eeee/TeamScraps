package engine;

import entity.Score;

import java.io.*;
import java.util.*;
import javax.swing.JOptionPane;

/**
 * Handles all file operations including saving/loading high scores and game settings
 */
public class FileManager {
    private static final String HIGH_SCORES_FILE = "highscores.dat";
    private static final String SETTINGS_FILE = "game_settings.dat";
    private static final int MAX_HIGH_SCORES = 10;


    private List<Score> highScores;
    private Properties settings;

    public FileManager() {
        this.highScores = new ArrayList<>();
        this.settings = new Properties();
        loadHighScores();
        loadSettings();
    }


    /**
     * Load high scores from file
     */
    @SuppressWarnings("unchecked")
    public void loadHighScores() {
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(HIGH_SCORES_FILE))) {

            // changed to wrap in new ArrayList so we never keep a SubList
            highScores = new ArrayList<>((List<Score>) ois.readObject());
            Collections.sort(highScores);

        } catch (FileNotFoundException e) {
            System.out.println("No high scores file found. Starting fresh.");
            initializeDefaultHighScores();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading high scores: " + e.getMessage());
            initializeDefaultHighScores();
        }
    }

    /**
     * Save high scores to file
     */
    public void saveHighScores() {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(HIGH_SCORES_FILE))) {

            // copy into a real ArrayList instead of keeping SubList
            if (highScores.size() > MAX_HIGH_SCORES) {
                highScores = new ArrayList<>(highScores.subList(0, MAX_HIGH_SCORES));
            }

            // always write a fresh ArrayList copy (defensive)
            oos.writeObject(new ArrayList<>(highScores));

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    "Error saving high scores: " + e.getMessage(),
                    "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Add a new score and update high scores list
     */
    public boolean addScore(String playerName, int foodsEaten) {
        Score newScore = new Score(playerName, foodsEaten);

        highScores.add(newScore);
        Collections.sort(highScores);

        // remove excess scores - wrap subList in new ArrayList
        if (highScores.size() > MAX_HIGH_SCORES) {
            highScores = new ArrayList<>(highScores.subList(0, MAX_HIGH_SCORES));
        }

        saveHighScores();
        return isHighScore(foodsEaten);
    }

    /**
     * Check if a score qualifies as high score
     */
    public boolean isHighScore(int score) {
        if (highScores.size() < MAX_HIGH_SCORES) {
            return true;
        }
        return score > highScores.get(highScores.size() - 1).getFoodsEaten();
    }

    /**
     * Get top high scores
     */
    public List<Score> getHighScores() {
        return new ArrayList<>(highScores);
    }

    /**
     * Load game settings
     */
    public void loadSettings() {
        try (FileInputStream fis = new FileInputStream(SETTINGS_FILE)) {
            settings.load(fis);
        } catch (FileNotFoundException e) {
            System.out.println("No settings file found. Using defaults.");
            setDefaultSettings();
        } catch (IOException e) {
            System.err.println("Error loading settings: " + e.getMessage());
            setDefaultSettings();
        }
    }

    /**
     * Save game settings
     */
    public void saveSettings() {
        try (FileOutputStream fos = new FileOutputStream(SETTINGS_FILE)) {
            settings.store(fos, "Snake Game Settings");
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                    "Error saving settings: " + e.getMessage(),
                    "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Get setting with default value
     */
    public String getSetting(String key, String defaultValue) {
        return settings.getProperty(key, defaultValue);
    }

    /**
     * Set game setting
     */
    public void setSetting(String key, String value) {
        settings.setProperty(key, value);
        saveSettings();
    }

    /**
     * Initialize default high scores for demonstration
     */
    private void initializeDefaultHighScores() {
        highScores.add(new Score("CHAMP", 15));
        highScores.add(new Score("ACE", 12));
        highScores.add(new Score("PRO", 9));
        highScores.add(new Score("ROOKIE", 6));
        highScores.add(new Score("NEWBIE", 3));
        Collections.sort(highScores);
    }

    /**
     * Set default game settings
     */
    private void setDefaultSettings() {
        settings.setProperty("player_name", "PLAYER1");
        settings.setProperty("sound_enabled", "true");
        settings.setProperty("music_volume", "80");
    }


    /**
     * Clear all high scores (for testing/reset)
     */
    public void clearHighScores() {
        highScores.clear();
        saveHighScores();
    }
}