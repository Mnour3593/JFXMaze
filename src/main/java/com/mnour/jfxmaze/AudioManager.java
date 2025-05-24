package com.mnour.jfxmaze;

import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages audio playback for the game, including background music and sound effects.
 */
public class AudioManager {
    // Singleton instance
    private static AudioManager instance;
    
    // Background music player
    private MediaPlayer musicPlayer;
    
    // Sound effects cache
    private Map<String, AudioClip> soundEffects = new HashMap<>();
    
    // Audio settings
    private boolean musicEnabled = true;
    private boolean sfxEnabled = true;
    private double musicVolume = 0.5;  // 50%
    private double sfxVolume = 0.75;    // 70%
    private boolean isPaused = false;
    private String currentMusicResource = null;
    
    // Sound effect keys
    public static final String SFX_BUTTON_CLICK = "button_click";
    public static final String SFX_BONUS_COLLECT = "bonus_collect";
    public static final String SFX_WIN = "win";
    
    /**
     * Private constructor for singleton
     */
    private AudioManager() {
        // Load sound effects
        loadSoundEffects();
    }
    
    /**
     * Get the singleton instance
     */
    public static AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }
    
    /**
     * Load all sound effects into the cache
     */
    private void loadSoundEffects() {
        try {
            // Load button click sound
            loadSoundEffect(SFX_BUTTON_CLICK, "/audio/button_click.wav");
            
            // Load bonus collect sound
            loadSoundEffect(SFX_BONUS_COLLECT, "/audio/bonus_collect.wav");
            
            // Load win sound
            loadSoundEffect(SFX_WIN, "/audio/win.wav");
        } catch (Exception e) {
            System.err.println("Error loading sound effects: " + e.getMessage());
        }
    }
    
    /**
     * Load a single sound effect
     */
    private void loadSoundEffect(String key, String resourcePath) {
        try {
            URL soundUrl = getClass().getResource(resourcePath);
            if (soundUrl != null) {
                AudioClip clip = new AudioClip(soundUrl.toExternalForm());
                clip.setVolume(sfxVolume);
                soundEffects.put(key, clip);
            } else {
                System.err.println("Sound file not found: " + resourcePath);
            }
        } catch (Exception e) {
            System.err.println("Error loading sound effect: " + resourcePath + " - " + e.getMessage());
        }
    }
    
    /**
     * Play background music
     * @param resourcePath path to music file
     */
    public void playMusic(String resourcePath) {
        // Store current music path for potential resume later
        currentMusicResource = resourcePath;
        
        if (!musicEnabled || isPaused) return;
        
        try {
            // Stop any current music but don't dispose
            if (musicPlayer != null) {
                musicPlayer.stop();
            }
            
            // Load and start new music
            URL musicUrl = getClass().getResource(resourcePath);
            if (musicUrl != null) {
                Media media = new Media(musicUrl.toExternalForm());
                musicPlayer = new MediaPlayer(media);
                musicPlayer.setVolume(musicVolume);
                musicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
                musicPlayer.play();
            } else {
                System.err.println("Music file not found: " + resourcePath);
            }
        } catch (Exception e) {
            System.err.println("Error playing music: " + e.getMessage());
        }
    }
    
    /**
     * Start the main game music
     */
    public void playMainMusic() {
        playMusic("/audio/background_music.mp3");
    }
    
    /**
     * Pause background music
     */
    public void pauseMusic() {
        isPaused = true;
        if (musicPlayer != null) {
            musicPlayer.pause();
        }
    }
    
    /**
     * Resume background music
     */
    public void resumeMusic() {
        isPaused = false;
        if (musicEnabled) {
            if (musicPlayer != null) {
                musicPlayer.play();
            } else if (currentMusicResource != null) {
                playMusic(currentMusicResource);
            }
        }
    }
    
    /**
     * Stop background music (really stops it, not just pause)
     */
    public void stopMusic() {
        isPaused = false;
        if (musicPlayer != null) {
            musicPlayer.stop();
            musicPlayer.dispose();
            musicPlayer = null;
        }
        currentMusicResource = null;
    }
    
    /**
     * Play a sound effect
     * @param soundKey the key for the sound effect
     */
    public void playSoundEffect(String soundKey) {
        if (!sfxEnabled) return;
        
        AudioClip clip = soundEffects.get(soundKey);
        if (clip != null) {
            clip.play();
        }
    }
    
    /**
     * Set music enabled/disabled
     */
    public void setMusicEnabled(boolean enabled) {
        this.musicEnabled = enabled;
        if (enabled) {
            resumeMusic();
        } else {
            pauseMusic();
        }
    }
    
    /**
     * Set sound effects enabled/disabled
     */
    public void setSfxEnabled(boolean enabled) {
        this.sfxEnabled = enabled;
    }
    
    /**
     * Set music volume
     * @param volume 0.0 to 1.0
     */
    public void setMusicVolume(double volume) {
        this.musicVolume = Math.max(0.0, Math.min(1.0, volume));
        if (musicPlayer != null) {
            musicPlayer.setVolume(musicVolume);
        }
    }
    
    /**
     * Set sound effects volume
     * @param volume 0.0 to 1.0
     */
    public void setSfxVolume(double volume) {
        this.sfxVolume = Math.max(0.0, Math.min(1.0, volume));
        for (AudioClip clip : soundEffects.values()) {
            clip.setVolume(sfxVolume);
        }
    }
    
    /**
     * Get the current music volume
     */
    public double getMusicVolume() {
        return musicVolume;
    }
    
    /**
     * Get the current sound effects volume
     */
    public double getSfxVolume() {
        return sfxVolume;
    }
    
    /**
     * Check if music is enabled
     */
    public boolean isMusicEnabled() {
        return musicEnabled;
    }
    
    /**
     * Check if sound effects are enabled
     */
    public boolean isSfxEnabled() {
        return sfxEnabled;
    }
    
    /**
     * Mute/unmute all audio
     */
    public void setAllMuted(boolean muted) {
        setMusicEnabled(!muted);
        setSfxEnabled(!muted);
    }
    
    /**
     * Is all audio muted?
     */
    public boolean isAllMuted() {
        return !musicEnabled && !sfxEnabled;
    }
    
    /**
     * Check if background music is currently playing
     * @return true if music is playing, false otherwise
     */
    public boolean isBackgroundMusicPlaying() {
        return musicPlayer != null && 
               musicEnabled && 
               !isPaused && 
               musicPlayer.getStatus() == MediaPlayer.Status.PLAYING;
    }
} 