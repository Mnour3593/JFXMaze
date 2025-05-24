package com.mnour.jfxmaze;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

/**
 * Handles UI screens and navigation for the JFXMaze application.
 */
public class UIScreens {
    private Stage primaryStage;
    private GameController gameController;
    private String currentLoggedInUsername;
    private Image mainMenuBackgroundImage;
    
    private double fixedStageWidth = 600; // Default, can be overridden
    private double fixedStageHeight = 750; // Default, can be overridden
    
    // Default game settings
    private final int defaultPlayGameMode = MazeModel.SINGLE_PLAYER;
    private final int defaultPlayMazeSize = MazeModel.DEFAULT_SIZE;
    private boolean useCustomSettingsForRestart = false;
    private long seedForRestart = 0;
    private int mazeSizeForRestart = MazeModel.DEFAULT_SIZE;
    private int gameModeForRestart = MazeModel.SINGLE_PLAYER;
    
    public UIScreens(Stage primaryStage, GameController gameController) {
        this.primaryStage = primaryStage;
        this.gameController = gameController;
        loadAssets();
    }
    
    public void setFixedStageSize(double width, double height) {
        this.fixedStageWidth = width;
        this.fixedStageHeight = height;
    }
    
    private void loadAssets() {
        try {
            mainMenuBackgroundImage = loadImage("/images/main_menu_bg.png");
        } catch (Exception e) {
            System.err.println("Error loading main menu background: " + e.getMessage());
        }
    }
    
    private Image loadImage(String path) {
        java.io.InputStream stream = getClass().getResourceAsStream(path);
        if (stream == null) {
            System.err.println("Failed to load image: " + path);
            return null;
        }
        return new Image(stream);
    }
    
    public void setCurrentLoggedInUsername(String username) {
        this.currentLoggedInUsername = username;
    }
    

    
    public void showCustomGameDialog(boolean isFreshPlay, int defaultMode, int defaultSize, String defaultSeedStr) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Game Configuration");
        dialog.setHeaderText("Customize your maze adventure or start with defaults.");
        applyDialogStyles(dialog.getDialogPane());

        ButtonType playButtonType = new ButtonType("Start Maze!", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(playButtonType, ButtonType.CANCEL);
        Node playDialogButtonNode = dialog.getDialogPane().lookupButton(playButtonType);
        if (playDialogButtonNode != null) {
            playDialogButtonNode.getStyleClass().add("dialog-play-button");
        }

        RadioButton singlePlayerRadio = new RadioButton("Single Player");
        singlePlayerRadio.getStyleClass().add("custom-radio-button");
        RadioButton dualPlayerRadio = new RadioButton("Dual Player");
        dualPlayerRadio.getStyleClass().add("custom-radio-button");

        ToggleGroup gameModeGroup = new ToggleGroup();
        singlePlayerRadio.setToggleGroup(gameModeGroup);
        dualPlayerRadio.setToggleGroup(gameModeGroup);

        // Create maze size label and value display
        Label mazeSizeLabel = new Label("Maze Size:");
        mazeSizeLabel.getStyleClass().add("custom-label");
        
        // Create a label to display the current slider value
        Label mazeSizeValueLabel = new Label(String.valueOf(defaultSize));
        mazeSizeValueLabel.getStyleClass().add("size-value-label");
        
        // Create slider for maze size selection
        Slider mazeSizeSlider = new Slider(MazeModel.MIN_SIZE, MazeModel.MAX_SIZE, defaultSize);
        mazeSizeSlider.setBlockIncrement(2);
        mazeSizeSlider.setMajorTickUnit(10);
        mazeSizeSlider.setMinorTickCount(4);
        mazeSizeSlider.setShowTickMarks(true);
        mazeSizeSlider.setShowTickLabels(true);
        mazeSizeSlider.setSnapToTicks(true);
        mazeSizeSlider.getStyleClass().add("maze-size-slider");
        
        // Ensure slider value is odd
        mazeSizeSlider.valueProperty().addListener((obs, oldValue, newValue) -> {
            int value = newValue.intValue();
            if (value % 2 == 0) {
                if (value < oldValue.intValue() && value > MazeModel.MIN_SIZE) {
                    mazeSizeSlider.setValue(value - 1);
                } else if (value < MazeModel.MAX_SIZE) {
                    mazeSizeSlider.setValue(value + 1);
                } else {
                    mazeSizeSlider.setValue(MazeModel.MAX_SIZE - 1);
                }
            } else {
                mazeSizeValueLabel.setText(String.valueOf(value));
            }
        });

        // Improved seed input with tooltip and help text
        Label seedLabel = new Label("Maze Seed:");
        seedLabel.getStyleClass().add("custom-label");
        
        TextField seedField = new TextField();
        seedField.getStyleClass().add("custom-textfield");
        seedField.setPromptText("Enter number or text");
        
        // Create tooltip with more information
        Tooltip seedTooltip = new Tooltip(
            "Leave blank for a random maze.\n" +
            "Enter a number or text to generate the same maze again.\n" +
            "Text entries will be converted to a number automatically."
        );
        seedTooltip.setShowDelay(javafx.util.Duration.millis(300));
        seedField.setTooltip(seedTooltip);
        
        // Create info label for seed explanation
        Label seedInfoLabel = new Label("Text seeds will create consistent mazes");
        seedInfoLabel.getStyleClass().add("info-label-small");
        seedInfoLabel.setWrapText(true);

        // Autofill logic
        if (useCustomSettingsForRestart) { // Coming from in-game restart
            if (gameModeForRestart == MazeModel.SINGLE_PLAYER) {
                singlePlayerRadio.setSelected(true);
            } else {
                dualPlayerRadio.setSelected(true);
            }
            mazeSizeSlider.setValue(mazeSizeForRestart);
            mazeSizeValueLabel.setText(String.valueOf(mazeSizeForRestart));
            seedField.setText(String.valueOf(seedForRestart));
            useCustomSettingsForRestart = false; // Reset flag after using
        } else { // Coming from "Play Game" or direct "Custom Game"
            if (defaultMode == MazeModel.SINGLE_PLAYER) {
                singlePlayerRadio.setSelected(true);
            } else {
                dualPlayerRadio.setSelected(true);
            }
            mazeSizeSlider.setValue(defaultSize);
            mazeSizeValueLabel.setText(String.valueOf(defaultSize));
            seedField.setText(defaultSeedStr);
        }

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPadding(new Insets(30, 50, 30, 50)); // More padding
        grid.getStyleClass().add("dialog-grid-pane");

        grid.add(new Label("Mode:"), 0, 0);
        HBox modeBox = new HBox(15, singlePlayerRadio, dualPlayerRadio);
        modeBox.setAlignment(Pos.CENTER_LEFT);
        grid.add(modeBox, 1, 0);

        grid.add(mazeSizeLabel, 0, 1);
        
        // Create HBox to hold slider and its value label
        HBox sliderBox = new HBox(10, mazeSizeSlider, mazeSizeValueLabel);
        sliderBox.setAlignment(Pos.CENTER_LEFT);
        grid.add(sliderBox, 1, 1);
        
        grid.add(seedLabel, 0, 2);
        grid.add(seedField, 1, 2);
        grid.add(seedInfoLabel, 1, 3); // Add the seed info label

        dialog.getDialogPane().setContent(grid);
        mazeSizeSlider.requestFocus();

        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == playButtonType) {
            int gameMode = dualPlayerRadio.isSelected() ? MazeModel.DUAL_PLAYER : MazeModel.SINGLE_PLAYER;

            int tempSize = (int) mazeSizeSlider.getValue();
            if (tempSize % 2 == 0) {
                tempSize = Math.min(MazeModel.MAX_SIZE, Math.max(MazeModel.MIN_SIZE, tempSize + 1));
                if (tempSize > MazeModel.MAX_SIZE) {
                    tempSize = MazeModel.MAX_SIZE - (MazeModel.MAX_SIZE % 2 == 0 ? 1 : 0);
                }
                if (tempSize < MazeModel.MIN_SIZE) {
                    tempSize = MazeModel.MIN_SIZE + (MazeModel.MIN_SIZE % 2 == 0 ? 1 : 0);
                }
            }
            int mazeSize = tempSize;

            long seed;
            String seedText = seedField.getText().trim();
            
            if (seedText.isEmpty()) {
                // Generate a random seed if field is empty
                seed = System.currentTimeMillis();
                if (seed == 0) seed = 1;
            } else {
                try {
                    // Try parsing as a number first
                    seed = Long.parseLong(seedText);
                    if (seed == 0) seed = 1; // Avoid zero seed
                } catch (NumberFormatException e) {
                    // If not a number, convert the text to a consistent hash value
                    seed = convertTextToSeed(seedText);
                }
            }

            // Start game with FXML directly
            gameController.startGameFXML(gameMode, mazeSize, seed);
        } else {
            // User cancelled or closed dialog
            if (isFreshPlay) {
                showFXMLWelcomeScreen();
            }
        }
    }
    
    /**
     * Converts a text string into a consistent long seed value using hash function
     */
    private long convertTextToSeed(String text) {
        // Use Java's string hash code as a base
        long seed = 0;
        
        // Custom hash function that will generate the same value for the same input text
        for (int i = 0; i < text.length(); i++) {
            seed = 31 * seed + text.charAt(i);
        }
        
        // Ensure the seed is positive and non-zero
        seed = Math.abs(seed);
        if (seed == 0) seed = 1;
        
        return seed;
    }
    
    /**
     * Redirects to FXML end game screen for backward compatibility
     */
    public void showEndGameScreen(int winner, int score, int moves, int elapsedTime) {
        // Forward to FXML version
        showEndGameScreenFXML(winner, score, moves, elapsedTime);
    }
    
    public void saveScore(int winningPlayer, int score, int moves, int timeTaken, int gameMode) {
        String playerNameForRecord;

        if (gameMode == MazeModel.SINGLE_PLAYER) {
            playerNameForRecord = (currentLoggedInUsername != null && !currentLoggedInUsername.trim().isEmpty()) ? currentLoggedInUsername.trim() : "Anonymous Hero";
            // Directly proceed to saving for single player
            writeScoreToFile(playerNameForRecord, winningPlayer, score, moves, timeTaken, gameMode);
        } else { // DUAL_PLAYER mode
            Dialog<String> dialog = new Dialog<>();
            dialog.setTitle("Save Your Epic Score!");
            // For dual player, winningPlayer will be 1 or 2.
            dialog.setHeaderText("Player " + winningPlayer + " (" + (winningPlayer == 1 ? "P1" : "P2") + "), enter your name for the Hall of Fame:");
            applyDialogStyles(dialog.getDialogPane());

            ButtonType saveButtonType = new ButtonType("Save Score", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
            Node saveDialogButtonNode = dialog.getDialogPane().lookupButton(saveButtonType);
            if(saveDialogButtonNode != null) saveDialogButtonNode.getStyleClass().add("dialog-play-button");

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 100, 10, 20));

            TextField nameField = new TextField();
            nameField.setPromptText("Champion Name"); // Generic prompt
            // Pre-fill with a default or leave blank for dual player winner to enter their name
            // nameField.setText("Player " + winningPlayer); // Optional: prefill suggestion
            nameField.getStyleClass().add("custom-textfield");

            grid.add(new Label("Name:"), 0, 0);
            grid.add(nameField, 1, 0);
            GridPane.setHgrow(nameField, Priority.ALWAYS);

            dialog.getDialogPane().setContent(grid);
            nameField.requestFocus(); // Focus the name field
            
            dialog.setResultConverter(dialogBtn -> {
                if (dialogBtn == saveButtonType) {
                    return nameField.getText();
                }
                return null;
            });

            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                playerNameForRecord = result.get().trim().isEmpty() ? ("Player " + winningPlayer) : result.get().trim();
                writeScoreToFile(playerNameForRecord, winningPlayer, score, moves, timeTaken, gameMode);
            } else {
                // User cancelled dialog, maybe show main menu or do nothing further for score saving
                // showWelcomeScreen(); // Optionally go to welcome screen if save is cancelled
            }
        }
    }
    
    private void writeScoreToFile(String playerName, int winningPlayer, int score, int moves, int timeTaken, int gameMode) {
        try {
            // Ensure ScoreManager is used if it handles file path/logic, or keep direct file writing here.
            // For now, direct file writing as per original structure in the prompt.
            File scoreFile = new File("jfxmaze_scores.txt"); 
            FileWriter writer = new FileWriter(scoreFile, true); // Append mode
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            writer.write("Player Name: " + playerName + "\n");
            if (gameMode == MazeModel.DUAL_PLAYER) {
                writer.write("Won As: Player " + winningPlayer + "\n");
            }
            writer.write("Score (Dots): " + score + "\n");
            writer.write("Moves: " + moves + "\n");
            writer.write("Time: " + timeTaken + " seconds\n");
            writer.write("Maze Seed: " + gameController.getMazeSeed() + "\n");
            writer.write("Maze Size: " + gameController.getMazeSize() + "x" + gameController.getMazeSize() + "\n");
            writer.write("Mode: " + (gameMode == MazeModel.SINGLE_PLAYER ? "Single Player" : "Dual Player") + "\n");
            writer.write("User Logged In: " + (currentLoggedInUsername != null ? currentLoggedInUsername : "N/A") + "\n"); // Added logged in user context
            writer.write("Date: " + dateFormat.format(new Date()) + "\n");
            writer.write("----------------------------------------\n");
            writer.close();

            Alert infoAlert = new Alert(Alert.AlertType.INFORMATION, "Score saved for " + playerName + "!");
            infoAlert.setHeaderText(null);
            applyDialogStyles(infoAlert.getDialogPane());
            infoAlert.showAndWait();
        } catch (IOException e) {
            Alert errorAlert = new Alert(Alert.AlertType.ERROR, "Could not save score: " + e.getMessage());
            errorAlert.setHeaderText("Save Failed");
            applyDialogStyles(errorAlert.getDialogPane());
            errorAlert.showAndWait();
        }
    }
    
    public void setCustomSettingsForRestart(long seed, int mazeSize, int gameMode) {
        this.useCustomSettingsForRestart = true;
        this.seedForRestart = seed;
        this.mazeSizeForRestart = mazeSize;
        this.gameModeForRestart = gameMode;
    }
    
    private void applyStylesheets(Scene scene) {
        try {
            String cssPath = getClass().getResource("/css/style.css").toExternalForm();
            if (cssPath != null) {
                scene.getStylesheets().add(cssPath);
            } else {
                System.err.println("CSS file not found at /css/style.css");
            }
        } catch (Exception e) {
            System.err.println("Failed to load CSS: " + e.getMessage());
        }
    }
    
    private void applyDialogStyles(DialogPane dialogPane) {
        try {
            String cssPath = getClass().getResource("/css/style.css").toExternalForm();
            if (cssPath != null) {
                dialogPane.getStylesheets().add(cssPath);
                dialogPane.getStyleClass().add("custom-dialog-pane");
            }
        } catch (Exception e) {
            System.err.println("Failed to load CSS for dialog: " + e.getMessage());
        }
    }

    /**
     * Shows the game screen using FXML-based UI.
     * This method will be used for testing the FXML version without disrupting the existing flow.
     */
    public void showGameScreenFXML() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("game.fxml"));
            Scene scene = new Scene(loader.load(), fixedStageWidth, fixedStageHeight);
            applyStylesheets(scene);
            
            GameScreenController controller = loader.getController();
            if (controller != null) {
                controller.setupGame(
                    gameController.getMazeModel(),
                    primaryStage,
                    gameController,
                    this
                );
                
                // Set up key press handling
                scene.setOnKeyPressed(event -> controller.handleKeyPress(event.getCode()));
                
                // Apply scene and show
                primaryStage.setTitle("JFXMaze - Level: Seed " + gameController.getMazeSeed());
                primaryStage.setScene(scene);
                primaryStage.sizeToScene();
                primaryStage.setWidth(fixedStageWidth);
                primaryStage.setHeight(fixedStageHeight);
                primaryStage.centerOnScreen();
                primaryStage.show();
            }
        } catch (IOException e) {
            System.err.println("Error loading game FXML: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Shows the end game screen using FXML-based UI.
     * This method will be used for testing the FXML version without disrupting the existing flow.
     */
    public void showEndGameScreenFXML(int winner, int score, int moves, int elapsedTime) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("endgame.fxml"));
            Scene scene = new Scene(loader.load(), fixedStageWidth, fixedStageHeight);
            applyStylesheets(scene);
            
            EndGameController controller = loader.getController();
            if (controller != null) {
                controller.setupEndGame(
                    winner,
                    score,
                    moves,
                    elapsedTime,
                    gameController.getGameMode(),
                    gameController,
                    this,
                    primaryStage,
                    currentLoggedInUsername
                );
                
                // Apply scene and show
                primaryStage.setTitle("JFXMaze - Game Over");
                primaryStage.setScene(scene);
                primaryStage.sizeToScene();
                primaryStage.setWidth(fixedStageWidth);
                primaryStage.setHeight(fixedStageHeight);
                primaryStage.centerOnScreen();
                primaryStage.show();
            }
        } catch (IOException e) {
            System.err.println("Error loading end game FXML: " + e.getMessage());
            e.printStackTrace();
            
            // Fall back to original end game screen
            showEndGameScreen(winner, score, moves, elapsedTime);
        }
    }

    /**
     * Directly starts the FXML version of the game without showing a selection dialog.
     */


    /**
     * Shows the FXML welcome screen.
     */
    public void showFXMLWelcomeScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("welcome.fxml"));
            Scene scene = new Scene(loader.load());
            
            // Apply stylesheet
            applyStylesheets(scene);
            
            // Get controller and set references
            WelcomeController controller = loader.getController();
            controller.setGameController(gameController);
            controller.setUIScreens(this);
            controller.setPrimaryStage(primaryStage);
            
            // Set background image
            try {
                // Use mainMenuBackgroundImage which is loaded during initialization
                // instead of trying to load maze_bg.jpg which doesn't exist
                if (mainMenuBackgroundImage != null) {
                    controller.setBackgroundImage(mainMenuBackgroundImage);
                } else {
                    // Try to load it directly if it wasn't loaded before
                    Image backgroundImage = loadImage("/images/2main_menu_bg.png");
                    controller.setBackgroundImage(backgroundImage);
                }
            } catch (Exception e) {
                System.err.println("Failed to load welcome background image: " + e.getMessage());
            }
            
            // Set host services for hyperlinks
            if (primaryStage.getProperties().containsKey("hostServices")) {
                Object hostServices = primaryStage.getProperties().get("hostServices");
                if (hostServices != null) {
                    controller.setHostServices((javafx.application.HostServices)hostServices);
                }
            }
            
            // Update audio state from AudioManager
            boolean isMuted = AudioManager.getInstance().isAllMuted();
            controller.updateAudioButton(!isMuted);
            
            // Only play background music if it's not already playing
            if (!AudioManager.getInstance().isBackgroundMusicPlaying()) {
                AudioManager.getInstance().playMainMusic();
            }
            
            primaryStage.setTitle("JFXMaze - Welcome!");
            primaryStage.setScene(scene);
            primaryStage.sizeToScene();
            primaryStage.setWidth(fixedStageWidth);
            primaryStage.setHeight(fixedStageHeight);
            primaryStage.centerOnScreen();
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
            // Show error alert instead of falling back to non-FXML version
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Error Loading Welcome Screen");
            errorAlert.setHeaderText("Failed to load welcome screen");
            errorAlert.setContentText("Error details: " + e.getMessage());
            applyDialogStyles(errorAlert.getDialogPane());
            errorAlert.showAndWait();
        }
    }

    /**
     * Shows the settings screen using FXML-based UI.
     */
    public void showFXMLSettingsScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("settings.fxml"));
            Scene scene = new Scene(loader.load(), fixedStageWidth, fixedStageHeight);
            applyStylesheets(scene);
            
            SettingsController controller = loader.getController();
            if (controller != null) {
                controller.setGameController(gameController);
                controller.setUIScreens(this);
                controller.setPrimaryStage(primaryStage);
                controller.setCurrentLoggedInUsername(currentLoggedInUsername);
                
                // Apply scene and show
                primaryStage.setTitle("JFXMaze - Settings");
                primaryStage.setScene(scene);
                primaryStage.sizeToScene();
                primaryStage.setWidth(fixedStageWidth);
                primaryStage.setHeight(fixedStageHeight);
                primaryStage.centerOnScreen();
                primaryStage.show();
            }
        } catch (IOException e) {
            System.err.println("Error loading settings screen FXML: " + e.getMessage());
            e.printStackTrace();
            
            // Show error alert instead of falling back to non-FXML version
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Error Loading Settings");
            errorAlert.setHeaderText("Failed to load settings screen");
            errorAlert.setContentText("Error details: " + e.getMessage());
            applyDialogStyles(errorAlert.getDialogPane());
            errorAlert.showAndWait();
        }
    }


    /**
     * Shows the credits screen using FXML-based UI.
     */
    public void showFXMLCreditsScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("credits.fxml"));
            Scene scene = new Scene(loader.load(), fixedStageWidth, fixedStageHeight);
            applyStylesheets(scene);
            
            CreditsController controller = loader.getController();
            if (controller != null) {
                controller.setUIScreens(this);
                controller.setPrimaryStage(primaryStage);
                // Pass HostServices to the CreditsController
                if (primaryStage.getProperties().containsKey("hostServices")) {
                    controller.setHostServices((javafx.application.HostServices)primaryStage.getProperties().get("hostServices"));
                }
                
                primaryStage.setTitle("JFXMaze - Credits");
                primaryStage.setScene(scene);
                primaryStage.sizeToScene(); // Adjust size to content
                primaryStage.setWidth(fixedStageWidth);
                primaryStage.setHeight(fixedStageHeight);
                primaryStage.centerOnScreen();
                primaryStage.show();
            }
        } catch (IOException e) {
            System.err.println("Error loading credits.fxml: " + e.getMessage());
            e.printStackTrace();
            
            // Show error alert
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Error Loading Credits");
            errorAlert.setHeaderText("Failed to load credits screen");
            errorAlert.setContentText("Error details: " + e.getMessage());
            applyDialogStyles(errorAlert.getDialogPane());
            errorAlert.showAndWait();
        }
    }
} 