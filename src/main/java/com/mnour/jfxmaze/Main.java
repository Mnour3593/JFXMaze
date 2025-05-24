package com.mnour.jfxmaze;

import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Main entry point for the JFXMaze application.
 * <p>
 * This class launches the JavaFX application and delegates initialization to the GameController.
 * It also sets the application icon and ensures the main window is not resizable.
 */
public class Main extends Application {
    
    /**
     * Called by the JavaFX runtime to start the application.
     * Sets up the primary stage and delegates to GameController.
     * @param primaryStage the main window for the application
     */
    @Override
    public void start(Stage primaryStage) {
        // Set application icon (if available)
        try {
            primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/icon.png")));
        } catch (Exception e) {
            System.err.println("Error loading application icon: /images/icon.png - " + e.getMessage());
        }

        // Store host services for later use (e.g., opening web links)
        primaryStage.getProperties().put("hostServices", getHostServices());
        
        // Prevent resizing for consistent UI
        primaryStage.setResizable(false);

        // Start the main game controller
        GameController gameController = new GameController();
        gameController.start(primaryStage);
    }
    
    /**
     * Main method, launches the JavaFX application.
     * @param args command-line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
} 