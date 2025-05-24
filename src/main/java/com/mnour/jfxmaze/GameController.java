package com.mnour.jfxmaze;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Main controller for the JFXMaze game.
 * <p>
 * Coordinates between UI, model, and renderer components. Handles authentication, game state, and navigation.
 */
public class GameController {

    // Define fixed stage dimensions for consistent UI
    private static final double FIXED_STAGE_WIDTH = 600.0;
    private static final double FIXED_STAGE_HEIGHT = 750.0;

    // Core components
    private Stage primaryStage;
    private MazeModel mazeModel;
    private MazeRenderer mazeRenderer;
    private UIScreens uiScreens;
    private ScoreManager scoreManager;
    
    // Game state
    private Timer gameTimer;
    private long startTime;
    private int elapsedTime;
    private String currentLoggedInUsername;
    private Stage authStage;
    private BorderPane gameRootPane;
    
    /**
     * Initializes the game controller and loads scores.
     */
    public GameController() {
        scoreManager = new ScoreManager();
        scoreManager.loadScores();
    }
    
    /**
     * Starts the game application and shows the authentication screen.
     *
     * @param primaryStage The primary stage for the application
     */
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("JFXMaze");
        
        // Initialize UI screens and set fixed size
        uiScreens = new UIScreens(primaryStage, this);
        uiScreens.setFixedStageSize(FIXED_STAGE_WIDTH, FIXED_STAGE_HEIGHT);
        
        // Load user data
        UserAuth.loadUsers();
        
        // Show authentication screen
        showAuthenticationScreen();
    }
    
    /**
     * Shows the authentication screen for user login/registration.
     */
    private void showAuthenticationScreen() {
        try {
            authStage = new Stage();
            authStage.setTitle("JFXMaze - Login Required");
            authStage.setResizable(false);
            
            // Apply icon to authentication window
            if (primaryStage != null && !primaryStage.getIcons().isEmpty()) {
                authStage.getIcons().addAll(primaryStage.getIcons());
            }
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("authentication.fxml"));
            Scene scene = new Scene(loader.load());
            
            // Apply the same CSS stylesheet
            applyStylesheetsToScene(scene);
            
            authStage.setScene(scene);
            authStage.sizeToScene();
            authStage.setWidth(480);
            authStage.setHeight(350);
            authStage.centerOnScreen();
            
            UserAuth.AuthController authController = loader.getController();
            if (authController != null) {
                authController.setJfxMazeApp(this);
            } else {
                System.err.println("Auth Controller is null. Cannot proceed.");
                Platform.exit();
                return;
            }
            
            // Handle window close event
            authStage.setOnCloseRequest(event -> {
                if (currentLoggedInUsername == null) {
                    System.out.println("Authentication window closed without login. Exiting game.");
                    Platform.exit();
                }
            });
            
            primaryStage.hide();
            authStage.showAndWait();
            
            if (currentLoggedInUsername == null){
                System.out.println("Auth flow completed without login. Exiting.");
                Platform.exit();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Could not load authentication screen: " + e.getMessage());
            // Apply icon to alert dialog
            if (primaryStage != null && !primaryStage.getIcons().isEmpty()) {
                ((Stage)alert.getDialogPane().getScene().getWindow()).getIcons().addAll(primaryStage.getIcons());
            }
            alert.showAndWait();
            Platform.exit();
        }
    }
    
    /**
     * Called when authentication is successful. Proceeds to welcome screen.
     *
     * @param username The authenticated username
     */
    public void onAuthenticationSuccess(String username) {
        currentLoggedInUsername = username;
        System.out.println("Login successful for: " + currentLoggedInUsername);
        
        if (authStage != null && authStage.isShowing()) {
            authStage.close();
        }
        
        Platform.runLater(() -> {
            uiScreens.setCurrentLoggedInUsername(currentLoggedInUsername);
            uiScreens.showFXMLWelcomeScreen();
            primaryStage.setTitle("JFXMaze - Welcome " + currentLoggedInUsername);
            primaryStage.setWidth(FIXED_STAGE_WIDTH);
            primaryStage.setHeight(FIXED_STAGE_HEIGHT);
            primaryStage.centerOnScreen();
        });
    }

    /**
     * Applies the main CSS stylesheet to a scene.
     */
    private void applyStylesheetsToScene(Scene scene) {
        try {
            String cssPath = getClass().getResource("/css/style.css").toExternalForm();
            if (cssPath != null) {
                scene.getStylesheets().add(cssPath);
            }
        } catch (Exception e) {
            System.err.println("Failed to load CSS: " + e.getMessage());
        }
    }
    
    /**
     * Starts the game timer and updates elapsed time every second.
     */
    private void startGameTimer() {
        startTime = System.currentTimeMillis();
        elapsedTime = 0;
        
        if (gameTimer != null) {
            gameTimer.cancel();
        }
        
        gameTimer = new Timer();
        gameTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    if (mazeModel.getWinner() == 0) {
                        long currentTime = System.currentTimeMillis();
                        elapsedTime = (int) ((currentTime - startTime) / 1000);
                        updateGameUI();
                    }
                });
            }
        }, 0, 1000); // Update every second
    }
    
    /**
     * Updates the game UI with current game state (time, scores, moves).
     */
    private void updateGameUI() {
        // Update time label, player scores, etc.
        Scene scene = primaryStage.getScene();
        if (scene != null && scene.getRoot() == gameRootPane) {
            VBox statusPanel = (VBox) gameRootPane.getBottom();
            HBox playerInfoBox = (HBox) statusPanel.getChildren().get(0); // First child is playerInfoBox
            Label timeLabel = (Label) playerInfoBox.getChildren().get(0);
            timeLabel.setText("Time: " + elapsedTime + "s");

            Label p1Label = (Label) playerInfoBox.getChildren().get(1);
            p1Label.setText(String.format("P1 Score: %d Moves: %d", mazeModel.getPlayer1Score(), mazeModel.getPlayer1Moves()));

            if (mazeModel.getGameMode() == MazeModel.DUAL_PLAYER && playerInfoBox.getChildren().size() > 2) {
                Label p2Label = (Label) playerInfoBox.getChildren().get(2);
                p2Label.setText(String.format("P2 Score: %d Moves: %d", mazeModel.getPlayer2Score(), mazeModel.getPlayer2Moves()));
            }
        }
    }

    // Getters for game state
    public int getGameMode() { return mazeModel != null ? mazeModel.getGameMode() : MazeModel.SINGLE_PLAYER; }
    public int getMazeSize() { return mazeModel != null ? mazeModel.getMazeSize() : MazeModel.DEFAULT_SIZE; }
    public long getMazeSeed() { return mazeModel != null ? mazeModel.getSeed() : 0; }
    
    /**
     * Gets the maze model (game state).
     */
    public MazeModel getMazeModel() {
        return mazeModel;
    }
    
    /**
     * Gets the game timer.
     */
    public Timer getGameTimer() {
        return gameTimer;
    }
    
    /**
     * Gets the elapsed seconds since game start.
     */
    public int getElapsedSeconds() {
        return elapsedTime;
    }
    
    /**
     * Handles key press events for player movement from the FXML UI
     * 
     * @param code The key code that was pressed
     * @param controller The FXML controller
     */
    public void onKeyPress(KeyCode code, GameScreenController controller) {
        if (mazeModel == null) return;

        if (mazeModel.getWinner() != 0) {
            return;
        }

        int oldP1Score = mazeModel.getPlayer1Score();
        int oldP2Score = mazeModel.getPlayer2Score();
        
        boolean moved = false;
        int dx = 0, dy = 0;

        // Determine direction based on key
        if (code == KeyCode.W || code == KeyCode.UP) {
            dx = -1; dy = 0;
        } else if (code == KeyCode.A || code == KeyCode.LEFT) {
            dx = 0; dy = -1;
        } else if (code == KeyCode.S || code == KeyCode.DOWN) {
            dx = 1; dy = 0;
        } else if (code == KeyCode.D || code == KeyCode.RIGHT) {
            dx = 0; dy = 1;
        }
        
        // Process movement if a direction key was pressed
        if (dx != 0 || dy != 0) {
            int playerNum = 1; // Default to player 1
            
            // Determine which player to move
            if ((code == KeyCode.W || code == KeyCode.A || code == KeyCode.S || code == KeyCode.D) || 
                mazeModel.getGameMode() != MazeModel.DUAL_PLAYER) {
                // Player 1 movement
                moved = mazeModel.movePlayer(1, dx, dy);
                playerNum = 1;
            } else if (mazeModel.getGameMode() == MazeModel.DUAL_PLAYER) {
                // Player 2 movement (arrow keys in dual player mode)
                moved = mazeModel.movePlayer(2, dx, dy);
                playerNum = 2;
            }
            
            // Update player direction if moved
            if (moved && controller != null && controller.getMazeRenderer() != null) {
                controller.getMazeRenderer().updatePlayerDirection(playerNum, dx, dy);
            }
        }

        // If the player moved, update the UI
        if (moved) {
            // Check for bonus collection
            if (mazeModel.getPlayer1Score() > oldP1Score && controller != null) {
                controller.playBonusCollectSound();
            }
            
            if (mazeModel.getPlayer2Score() > oldP2Score && controller != null) {
                controller.playBonusCollectSound();
            }
            
            // Check for win condition
            int winner = mazeModel.getWinner();
            if (winner > 0) {
                // Play win sound
                if (controller != null) {
                    controller.playWinSound();
                }
                
                // Handle win
                if (gameTimer != null) {
                    gameTimer.cancel();
                    gameTimer = null;
                }

                int finalScore = (winner == 1) ? mazeModel.getPlayer1Score() : mazeModel.getPlayer2Score();
                int finalMoves = (winner == 1) ? mazeModel.getPlayer1Moves() : mazeModel.getPlayer2Moves();
                
                // Give a slight delay before showing end game screen
                Timer delayTimer = new Timer();
                delayTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Platform.runLater(() -> {
                            if (uiScreens != null) {
                                uiScreens.showEndGameScreenFXML(winner, finalScore, finalMoves, elapsedTime);
                            }
                        });
                    }
                }, 500);
            }
            
            if (controller != null) {
                controller.updatePlayerLabels();
                controller.getMazeRenderer().renderMaze();
            }
        }
    }

    /**
     * Starts a new game with the specified settings using FXML UI.
     * @param gameMode The game mode (SINGLE_PLAYER or DUAL_PLAYER)
     * @param mazeSize The maze size
     * @param seed The random seed for maze generation
     */
    public void startGameFXML(int gameMode, int mazeSize, long seed) {
        // Cancel any existing timer
        if (gameTimer != null) {
            gameTimer.cancel();
            gameTimer = null;
        }
        
        // Create a new model with the specified settings
        mazeModel = new MazeModel(mazeSize, gameMode, seed);
        
        // Try to generate a solvable maze (reusing existing code)
        boolean mazeGeneratedSuccessfully = false;
        int mazeRegenCount = 0;
        
        do {
            if (mazeModel.isExitReachable()) {
                mazeGeneratedSuccessfully = true;
                break;
            } else {
                mazeRegenCount++;
                System.err.println("Maze generation attempt " + mazeRegenCount + " with seed " + seed + " failed: Exit unreachable.");
                
                if (mazeRegenCount >= 5) {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Maze Generation Failed");
                    alert.setHeaderText("Failed 5 times to create a solvable maze.");
                    alert.setContentText("Try with a new random seed?");
                    
                    if (alert.showAndWait().get().getButtonData().isDefaultButton()) {
                        seed = System.currentTimeMillis();
                        if (seed == 0) seed = 1;
                        mazeModel = new MazeModel(mazeSize, gameMode, seed);
                        mazeRegenCount = 0;
                    } else {
                        uiScreens.showFXMLWelcomeScreen();
                        return;
                    }
                } else {
                    seed++;
                    if (seed == 0) seed = 1;
                    mazeModel = new MazeModel(mazeSize, gameMode, seed);
                }
            }
        } while (mazeRegenCount < 5 || mazeGeneratedSuccessfully);
        
        if (!mazeGeneratedSuccessfully) {
            System.err.println("Could not generate a solvable maze after all attempts.");
            Alert errorAlert = new Alert(Alert.AlertType.ERROR, "Failed to generate a solvable maze. Returning to main menu.");
            errorAlert.showAndWait();
            uiScreens.showFXMLWelcomeScreen();
            return;
        }
        
        // Use the FXML-based game UI instead of setting up our own
        uiScreens.showGameScreenFXML();
        
        // Start the game timer
        startGameTimer();
    }

    /**
     * Sets the maze renderer.
     * @param renderer The maze renderer instance.
     */
    public void setMazeRenderer(MazeRenderer renderer) {
        this.mazeRenderer = renderer;
    }

} 