package engine;

import javax.sound.sampled.*;
import java.net.URL;

/**
 * Manages all sound effects for the game:
 * - Background music (looping)
 * - Snake movement sound
 * - Eating sound
 * - Wall collision sound
 *
 * Put .wav files into the resources folder and match the file names below.
 */
public class SoundManager {

    // Looping background music clip
    private Clip bgmClip;

    // ------ File names ------
    private final String BGM_FILE = "bgm.wav";
    private final String MOVE_FILE = "move.wav";
    private final String EAT_FILE = "eat.wav";
    private final String HIT_FILE = "hit.wav";


    /** Play background BGM */
    public void playBGM() {
        try {
            // Prevent duplicate playback
            if (bgmClip != null && bgmClip.isRunning())
                return;

            bgmClip = loadClip(BGM_FILE);

            if (bgmClip != null) {

                // Reduce BGM volume to 50%
                FloatControl gain = (FloatControl) bgmClip.getControl(FloatControl.Type.MASTER_GAIN);
                float volume = -15.0f; // about 50% volume reduction
                gain.setValue(volume);

                bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /** Stop background music */
    public void stopBGM() {
        if (bgmClip != null) {
            bgmClip.stop();
        }
    }


    /** Play movement sound (boosted volume) */
    public void playMove() {
        try {
            Clip clip = loadClip(MOVE_FILE);

            if (clip != null) {

                // Increase volume for movement sound
                try {
                    FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                    gain.setValue(+6.0f); // make movement sound louder
                } catch (Exception ex) {
                    // Some audio files may not support volume control — safe to ignore
                }

                clip.start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Play eating sound (0.5 sec only) */
    public void playEat() {
        try {
            Clip clip = loadClip(EAT_FILE);

            if (clip != null) {
                clip.start();

                // Stop after 0.5 seconds
                new Thread(() -> {
                    try {
                        Thread.sleep(500);
                        clip.stop();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }).start();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /** Play wall collision sound */
    public void playHit() {
        playEffect(HIT_FILE);
    }



    // ---------------- internal methods ----------------


    /** Play one-shot short effect */
    private void playEffect(String fileName) {
        try {
            Clip clip = loadClip(fileName);
            if (clip != null)
                clip.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /** Load .wav file as Clip */
    private Clip loadClip(String name) {
        try {
            URL url = getClass().getClassLoader().getResource(name);
            if (url == null) {
                System.out.println("[SoundManager] File not found: " + name);
                return null;
            }

            AudioInputStream ais = AudioSystem.getAudioInputStream(url);
            Clip clip = AudioSystem.getClip();
            clip.open(ais);

            return clip;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
