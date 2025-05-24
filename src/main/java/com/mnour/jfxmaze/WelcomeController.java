package com.mnour.jfxmaze;

import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * Controller for the welcome screen FXML.
 * <p>
 * Handles user interaction on the main menu, including navigation to game, settings, credits, and audio controls.
 */
public class WelcomeController {
    // FXML-injected UI elements
    @FXML private StackPane rootPane;
    @FXML private VBox contentBox;
    @FXML private Text titleText;
    @FXML private Text subtitleText;
    @FXML private Button playGameButton;
    @FXML private Button settingsButton;
    @FXML private Button logoutButton;
    @FXML private Button exitButton;
    @FXML private Button creditsButton;
    @FXML private Button audioButton;
    
    // References to other controllers and state
    private GameController gameController;
    private UIScreens uiScreens;
    private Stage primaryStage;
    private Image backgroundImage;
    private HostServices hostServices;
    
    /**
     * Initializes the controller (called automatically by FXMLLoader).
     * Can be used for additional setup if needed.
     */
    @FXML
    private void initialize() {
        // This method is automatically called by the FXMLLoader
    }
    
    /**
     * Sets the GameController reference for navigation and state management.
     */
    public void setGameController(GameController gameController) {
        this.gameController = gameController;
    }
    
    /**
     * Sets the UIScreens reference for UI navigation.
     */
    public void setUIScreens(UIScreens uiScreens) {
        this.uiScreens = uiScreens;
    }
    
    /**
     * Sets the primary stage reference for window management.
     */
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
    
    /**
     * Sets the host services for opening web links (e.g., in credits).
     */
    public void setHostServices(HostServices hostServices) {
        this.hostServices = hostServices;
    }
    
    /**
     * Sets the background image for the welcome screen, or a fallback color if not available.
     */
    public void setBackgroundImage(Image backgroundImage) {
        this.backgroundImage = backgroundImage;
        
        if (backgroundImage != null) {
            ImageView backgroundImageView = new ImageView(backgroundImage);
            backgroundImageView.fitWidthProperty().bind(rootPane.widthProperty());
            backgroundImageView.fitHeightProperty().bind(rootPane.heightProperty());
            backgroundImageView.setPreserveRatio(false);
            rootPane.getChildren().add(0, backgroundImageView); // Add as first child
        } else {
            rootPane.setStyle("-fx-background-color: linear-gradient(to bottom, #333333, #1a1a1a);");
        }
    }
    
    /**
     * Handles the Play Game button click. Opens the custom game dialog.
     */
    @FXML
    private void handlePlayGame() {
        // Play button click sound if not muted
        AudioManager.getInstance().playSoundEffect(AudioManager.SFX_BUTTON_CLICK);
        uiScreens.showCustomGameDialog(true, MazeModel.SINGLE_PLAYER, MazeModel.DEFAULT_SIZE, "");
    }
    
    /**
     * Handles the Settings button click. Navigates to the settings screen.
     */
    @FXML
    private void handleSettings() {
        AudioManager.getInstance().playSoundEffect(AudioManager.SFX_BUTTON_CLICK);
        uiScreens.showFXMLSettingsScreen();
    }
    
    /**
     * Handles the Logout button click. Logs out the user and returns to the login screen.
     */
    @FXML
    private void handleLogout() {
        AudioManager.getInstance().playSoundEffect(AudioManager.SFX_BUTTON_CLICK);
        UserAuth.logoutUser();
        if (gameController != null) {
            gameController.start(primaryStage);
        }
    }
    
    /**
     * Handles the Exit button click. Closes the application.
     */
    @FXML
    private void handleExit() {
        AudioManager.getInstance().playSoundEffect(AudioManager.SFX_BUTTON_CLICK);
        primaryStage.close();
    }
    
    /**
     * Handles the Credits button click. Shows the credits screen.
     */
    @FXML
    private void handleShowCredits() {
        AudioManager.getInstance().playSoundEffect(AudioManager.SFX_BUTTON_CLICK);
        uiScreens.showFXMLCreditsScreen();
    }
    
    /**
     * Handles the Audio toggle button click. Mutes/unmutes all audio and updates the button text.
     */
    @FXML
    private void handleToggleAudio() {
        AudioManager audioManager = AudioManager.getInstance();
        boolean newMuteState = !audioManager.isAllMuted();
        audioManager.setAllMuted(newMuteState);
        audioButton.setText(newMuteState ? "Audio: Off" : "Audio: On");
        // Play sound if unmuting
        if (!newMuteState) {
            audioManager.playSoundEffect(AudioManager.SFX_BUTTON_CLICK);
        }
    }
    
    /**
     * Updates the audio button state to reflect whether audio is enabled or muted.
     * @param enabled true if audio is enabled, false if muted
     */
    public void updateAudioButton(boolean enabled) {
        if (audioButton != null) {
            audioButton.setText(enabled ? "Audio: On" : "Audio: Off");
        }
    }
}