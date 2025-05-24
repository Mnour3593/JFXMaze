package com.mnour.jfxmaze;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Controller for the game screen FXML
 */
public class GameScreenController {
    @FXML private BorderPane gameRootPane;
    @FXML private StackPane canvasContainer;
    @FXML private Canvas mazeCanvas;
    @FXML private VBox statusPanel;
    @FXML private HBox playerInfoBox;
    @FXML private Label timeLabel;
    @FXML private Label player1Label;
    @FXML private Button restartButton;
    @FXML private Button quitButton;
    @FXML private Button audioToggleButton;
    @FXML private Label keyboardInfoLabel;
    
    // References to other components
    private GameController gameController;
    private MazeRenderer mazeRenderer;
    private MazeModel mazeModel;
    private UIScreens uiScreens;
    private Stage primaryStage;
    
    // Timer for continuous UI updates
    private Timeline updateTimeline;
    
    // Audio manager reference
    private AudioManager audioManager = AudioManager.getInstance();
    
    /**
     * Initializes the controller
     */
    @FXML
    private void initialize() {
        // Set up button actions
        restartButton.setOnAction(e -> handleRestart());
        quitButton.setOnAction(e -> handleQuit());
        audioToggleButton.setOnAction(e -> handleToggleAudio());
    }
    
    /**
     * Called after FXML initialization to set up the game
     */
    public void setupGame(MazeModel mazeModel, Stage primaryStage, GameController gameController, UIScreens uiScreens) {
        this.mazeModel = mazeModel;
        this.primaryStage = primaryStage;
        this.gameController = gameController;
        this.uiScreens = uiScreens;
        
        // Set audio button state
        updateAudioButtonState();
        
        // Add window close handler
        primaryStage.setOnCloseRequest(event -> {
            // Cancel game timer
            if (gameController.getGameTimer() != null) {
                gameController.getGameTimer().cancel();
            }
            
            // Stop continuous updates
            stopContinuousUpdates();
            
            // Exit the application
            javafx.application.Platform.exit();
        });
        
        // Calculate dynamic cell size based on maze size
        double TARGET_MAZE_PANE_WIDTH = 525.0;
        double TARGET_MAZE_PANE_HEIGHT = 525.0;
        int calculatedCellSize = (int) Math.max(1.0, Math.min(
            TARGET_MAZE_PANE_WIDTH / mazeModel.getMazeSize(), 
            TARGET_MAZE_PANE_HEIGHT / mazeModel.getMazeSize()));
        
        // Set canvas size
        double canvasWidth = mazeModel.getMazeSize() * calculatedCellSize;
        double canvasHeight = mazeModel.getMazeSize() * calculatedCellSize;
        mazeCanvas.setWidth(canvasWidth);
        mazeCanvas.setHeight(canvasHeight);
        
        // Create maze renderer
        mazeRenderer = new MazeRenderer(mazeCanvas, mazeModel, calculatedCellSize);
        
        // Let the GameController know about the renderer (this fixes the texture updating)
        gameController.setMazeRenderer(mazeRenderer);
        
        // Set up dual player UI if needed
        if (mazeModel.getGameMode() == MazeModel.DUAL_PLAYER) {
            Label player2Label = new Label();
            player2Label.getStyleClass().add("info-label");
            playerInfoBox.getChildren().add(player2Label);
        }
        
        // Set keyboard controls info
        String p1Controls = "P1: WASD";
        String p2Controls = mazeModel.getGameMode() == MazeModel.DUAL_PLAYER ? " | P2: Arrows" : " or Arrows";
        keyboardInfoLabel.setText("Controls: " + p1Controls + p2Controls);
        
        // Update player labels
        updatePlayerLabels();
        
        // Render the maze initially
        mazeRenderer.renderMaze();
        
        // Start continuous UI updates
        startContinuousUpdates();
    }
    
    /**
     * Starts continuous UI updates independent of player movement
     */
    private void startContinuousUpdates() {
        // Cancel any existing timeline
        if (updateTimeline != null && updateTimeline.getStatus() == Animation.Status.RUNNING) {
            updateTimeline.stop();
        }
        
        // Create a new timeline that updates UI every 500ms
        updateTimeline = new Timeline(
            new KeyFrame(Duration.millis(500), e -> {
                updatePlayerLabels();
                mazeRenderer.renderMaze();
            })
        );
        updateTimeline.setCycleCount(Animation.INDEFINITE);
        updateTimeline.play();
    }
    
    /**
     * Stops the continuous UI updates
     */
    private void stopContinuousUpdates() {
        if (updateTimeline != null) {
            updateTimeline.stop();
            updateTimeline = null;
        }
    }
    
    /**
     * Updates the player information labels with the current state
     */
    public void updatePlayerLabels() {
        if (mazeModel == null) return;
        
        // Update time display
        if (gameController != null) {
            timeLabel.setText("Time: " + gameController.getElapsedSeconds() + "s");
        }
        
        // Update player 1 stats
        player1Label.setText(String.format("P1 Score: %d Moves: %d", 
            mazeModel.getPlayer1Score(), mazeModel.getPlayer1Moves()));
            
        // Update player 2 stats if in dual player mode
        if (mazeModel.getGameMode() == MazeModel.DUAL_PLAYER && playerInfoBox.getChildren().size() > 2) {
            Label player2Label = (Label) playerInfoBox.getChildren().get(2);
            player2Label.setText(String.format("P2 Score: %d Moves: %d", 
                mazeModel.getPlayer2Score(), mazeModel.getPlayer2Moves()));
        }
    }
    
    /**
     * Handles key press events for player movement
     */
    public void handleKeyPress(KeyCode code) {
        if (gameController != null) {
            // Forward the key press to the game controller
            gameController.onKeyPress(code, this);
        }
    }
    
    /**
     * Handles the restart button click
     */
    private void handleRestart() {
        // Play button sound
        audioManager.playSoundEffect(AudioManager.SFX_BUTTON_CLICK);
        
        if (gameController != null) {
            // Prompt for confirmation before restarting
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.CONFIRMATION
            );
            alert.setTitle("Start New Game");
            alert.setHeaderText("Start a New Maze?");
            alert.setContentText("Are you sure you want to start a new game? Your current progress will be lost.");
            
            // Add custom button types
            javafx.scene.control.ButtonType restartButtonType = new javafx.scene.control.ButtonType("New Game");
            javafx.scene.control.ButtonType cancelButtonType = new javafx.scene.control.ButtonType("Cancel", javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(restartButtonType, cancelButtonType);
            
            // Show the dialog and wait for response
            java.util.Optional<javafx.scene.control.ButtonType> result = alert.showAndWait();
            
            if (result.isPresent() && result.get() == restartButtonType) {
                // User confirmed restart
                if (gameController.getGameTimer() != null) {
                    gameController.getGameTimer().cancel();
                }
                
                // Stop our continuous updates
                stopContinuousUpdates();
                
                uiScreens.setCustomSettingsForRestart(
                    mazeModel.getSeed(), 
                    mazeModel.getMazeSize(), 
                    mazeModel.getGameMode());
                    
                uiScreens.showCustomGameDialog(
                    false, 
                    mazeModel.getGameMode(), 
                    mazeModel.getMazeSize(), 
                    String.valueOf(mazeModel.getSeed()));
            }
        }
    }
    
    /**
     * Handles the quit button click
     */
    private void handleQuit() {
        // Play button sound
        audioManager.playSoundEffect(AudioManager.SFX_BUTTON_CLICK);
        
        if (gameController != null) {
            // Prompt for confirmation before abandoning the game
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert.AlertType.CONFIRMATION
            );
            alert.setTitle("Return to Main Menu");
            alert.setHeaderText("Abandon Current Maze?");
            alert.setContentText("Are you sure you want to return to the main menu? Your current game progress will be lost.");
            
            // Add custom button types
            javafx.scene.control.ButtonType quitButtonType = new javafx.scene.control.ButtonType("Return to Menu");
            javafx.scene.control.ButtonType cancelButtonType = new javafx.scene.control.ButtonType("Cancel", javafx.scene.control.ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(quitButtonType, cancelButtonType);
            
            // Show the dialog and wait for response
            java.util.Optional<javafx.scene.control.ButtonType> result = alert.showAndWait();
            
            if (result.isPresent() && result.get() == quitButtonType) {
                // User confirmed quit
                if (gameController.getGameTimer() != null) {
                    gameController.getGameTimer().cancel();
                }
                
                // Stop our continuous updates
                stopContinuousUpdates();
                
                // Return to main menu
                uiScreens.showFXMLWelcomeScreen();
            }
        }
    }
    
    /**
     * Handles the audio toggle button click
     */
    private void handleToggleAudio() {
        boolean newMuteState = !audioManager.isAllMuted();
        audioManager.setAllMuted(newMuteState);
        updateAudioButtonState();
        
        // Play sound if unmuting
        if (!newMuteState) {
            audioManager.playSoundEffect(AudioManager.SFX_BUTTON_CLICK);
        }
    }
    
    /**
     * Updates the audio button text based on current state
     */
    private void updateAudioButtonState() {
        if (audioToggleButton != null) {
            audioToggleButton.setText(audioManager.isAllMuted() ? "Audio: Off" : "Audio: On");
        }
    }

    /**
     * Gets the maze renderer
     */
    public MazeRenderer getMazeRenderer() {
        return mazeRenderer;
    }
    
    /**
     * Play bonus collect sound
     */
    public void playBonusCollectSound() {
        audioManager.playSoundEffect(AudioManager.SFX_BONUS_COLLECT);
    }
    
    /**
     * Play win sound
     */
    public void playWinSound() {
        audioManager.playSoundEffect(AudioManager.SFX_WIN);
    }
} 