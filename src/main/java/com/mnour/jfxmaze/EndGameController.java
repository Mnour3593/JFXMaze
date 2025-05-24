package com.mnour.jfxmaze;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * Controller for the end game screen FXML
 */
public class EndGameController {
    @FXML private StackPane endSceneRoot;
    @FXML private Text headerText;
    @FXML private Text statsText;
    @FXML private Button saveScoreButton;
    @FXML private Button newMazeButton;
    @FXML private Button menuButton;
    @FXML private Button logoutButton;
    @FXML private Button exitAppButton;
    
    private GameController gameController;
    private UIScreens uiScreens;
    private Stage primaryStage;
    private int winner;
    private int score;
    private int moves;
    private int elapsedTime;
    private int gameMode;
    private String username;
    private Image backgroundImage;
    
    /**
     * Initializes the controller
     */
    @FXML
    private void initialize() {
        // Set up button actions
        saveScoreButton.setOnAction(e -> handleSaveScore());
        newMazeButton.setOnAction(e -> handleNewMaze());
        menuButton.setOnAction(e -> handleMainMenu());
        logoutButton.setOnAction(e -> handleLogout());
        exitAppButton.setOnAction(e -> handleExitApp());
    }
    
    /**
     * Sets the background image
     */
    public void setBackgroundImage(Image backgroundImage) {
        this.backgroundImage = backgroundImage;
        
        if (backgroundImage != null) {
            ImageView backgroundImageView = new ImageView(backgroundImage);
            backgroundImageView.fitWidthProperty().bind(endSceneRoot.widthProperty());
            backgroundImageView.fitHeightProperty().bind(endSceneRoot.heightProperty());
            backgroundImageView.setPreserveRatio(false);
            endSceneRoot.getChildren().add(0, backgroundImageView); // Add as first child
        } else {
            endSceneRoot.setStyle("-fx-background-color: linear-gradient(to bottom, #333333, #1a1a1a);");
        }
    }
    
    /**
     * Sets up the end game screen with game results
     * 
     * @param winner The winner (1=player1, 2=player2, 0=none, -1=game exited)
     * @param score The score of the winner
     * @param moves The number of moves made by the winner
     * @param elapsedTime The elapsed time in seconds
     * @param gameController Reference to game controller
     * @param uiScreens Reference to UI screens
     * @param primaryStage Reference to primary stage
     */
    public void setupEndGame(int winner, int score, int moves, int elapsedTime, int gameMode,
                             GameController gameController, UIScreens uiScreens, Stage primaryStage, String username) {
        this.winner = winner;
        this.score = score;
        this.moves = moves;
        this.elapsedTime = elapsedTime;
        this.gameMode = gameMode;
        this.gameController = gameController;
        this.uiScreens = uiScreens;
        this.primaryStage = primaryStage;
        this.username = username;
        
        // Update header text based on winner
        String outcomeMessage;
        if (winner == 1) {
            if (gameMode == MazeModel.DUAL_PLAYER) {
                outcomeMessage = "Player 1 Wins!";
                statsText.setText("P1 Time: " + elapsedTime + "s | Score: " + score + " | Moves: " + moves);
            } else {
                // For single player, only show username in the header, not in stats
                outcomeMessage = (username != null && !username.isEmpty() ? username : "Anonymous Hero") + " Wins!";
                statsText.setText("Time: " + elapsedTime + "s | Score: " + score + " | Moves: " + moves);
            }
        } else if (winner == 2) {
            outcomeMessage = "Player 2 Wins!";
            statsText.setText("P2 Time: " + elapsedTime + "s | Score: " + score + " | Moves: " + moves);
        } else if (winner == -1) { // Game quit early
            outcomeMessage = "Game Exited";
            statsText.setText("Time Played: " + elapsedTime + "s");
        } else {
            outcomeMessage = "Game Over"; // Should not happen if winner is set properly
            statsText.setText(""); // Empty stats for this case
        }
        
        headerText.setText(outcomeMessage);
        
        // Try to load background image from UIScreens
        try {
            Image bgImage = new Image(getClass().getResource("/images/main_menu_bg.png").toExternalForm());
            setBackgroundImage(bgImage);
        } catch (Exception e) {
            System.err.println("Failed to load background image for end game screen: " + e.getMessage());
            endSceneRoot.setStyle("-fx-background-color: linear-gradient(to bottom, #333333, #1a1a1a);");
        }
    }
    
    /**
     * Handles the save score button click
     */
    private void handleSaveScore() {
        // Play button sound
        AudioManager.getInstance().playSoundEffect(AudioManager.SFX_BUTTON_CLICK);
        
        if (uiScreens != null) {
            // Call appropriate score saving mechanism through the UIScreens
            uiScreens.saveScore(winner, score, moves, elapsedTime, gameMode);
        }
    }
    
    /**
     * Handles the new maze button click
     */
    private void handleNewMaze() {
        // Play button sound
        AudioManager.getInstance().playSoundEffect(AudioManager.SFX_BUTTON_CLICK);
        
        if (uiScreens != null) {
            // Use appropriate method to set up a new maze
            if (gameController != null) {
                uiScreens.setCustomSettingsForRestart(
                    gameController.getMazeSeed(),
                    gameController.getMazeSize(),
                    gameController.getGameMode()
                );
                uiScreens.showCustomGameDialog(
                    true, 
                    gameController.getGameMode(), 
                    gameController.getMazeSize(), 
                    "");
            }
        }
    }
    
    /**
     * Handles the main menu button click
     */
    private void handleMainMenu() {
        // Play button sound
        AudioManager.getInstance().playSoundEffect(AudioManager.SFX_BUTTON_CLICK);
        
        if (uiScreens != null) {
            uiScreens.showFXMLWelcomeScreen();
        }
    }
    
    /**
     * Handles the logout button click
     */
    private void handleLogout() {
        // Play button sound
        AudioManager.getInstance().playSoundEffect(AudioManager.SFX_BUTTON_CLICK);
        
        UserAuth.logoutUser();
        
        if (gameController != null) {
            gameController.start(primaryStage);
        }
    }
    
    /**
     * Handles the exit app button click
     */
    private void handleExitApp() {
        // Play button sound
        AudioManager.getInstance().playSoundEffect(AudioManager.SFX_BUTTON_CLICK);
        
        if (primaryStage != null) {
            primaryStage.close();
        }
    }
} 