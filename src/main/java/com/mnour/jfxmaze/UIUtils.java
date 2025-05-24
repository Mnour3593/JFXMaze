package com.mnour.jfxmaze;

import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Utility class for UI operations, such as applying the application icon to windows.
 */
public class UIUtils {
    
    // Stores the application icon for reuse across windows (singleton pattern for efficiency)
    private static Image appIcon = null;
    
    /**
     * Applies the application icon to a given JavaFX window (if it is a Stage).
     * Loads the icon only once and reuses it for all windows.
     *
     * @param window The window to apply the icon to (should be a Stage)
     */
    public static void applyAppIcon(Window window) {
        if (window instanceof Stage) {
            Stage stage = (Stage) window;
            
            // Load icon if not already loaded
            if (appIcon == null) {
                try {
                    appIcon = new Image(UIUtils.class.getResourceAsStream("/images/icon.png"));
                } catch (Exception e) {
                    System.err.println("Failed to load application icon: " + e.getMessage());
                    return;
                }
            }
            
            // Apply the icon to the stage
            if (appIcon != null) {
                stage.getIcons().setAll(appIcon);
            }
        }
    }

}