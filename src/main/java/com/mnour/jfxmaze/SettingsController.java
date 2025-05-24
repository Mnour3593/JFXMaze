package com.mnour.jfxmaze;

import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Controller for the settings screen FXML
 */
public class SettingsController {
    // UI components
    @FXML private Button changePassButton;
    @FXML private Button deleteAccountButton;
    @FXML private Button viewLeaderboardButton;
    @FXML private Button exportScoresButton;
    @FXML private Button clearMyScoresButton;
    @FXML private Button backButton;
    @FXML private Button saveButton;
    @FXML private Label usernameLabel;
    
    // Audio controls
    @FXML private ToggleButton muteToggle;
    @FXML private Slider musicVolumeSlider;
    @FXML private Slider sfxVolumeSlider;
    @FXML private Label musicVolumeLabel;
    @FXML private Label sfxVolumeLabel;
    
    // References to other components
    private GameController gameController;
    private UIScreens uiScreens;
    private Stage primaryStage;
    private String currentLoggedInUsername;
    
    // Score manager
    private ScoreManager scoreManager = new ScoreManager();
    
    // Audio manager reference
    private AudioManager audioManager = AudioManager.getInstance();
    
    // Track whether settings have been changed
    private boolean settingsChanged = false;
    
    /**
     * Initializes the controller
     */
    @FXML
    private void initialize() {
        // Set up audio controls
        setupAudioControls();
        
        // Load scores
        scoreManager.loadScores();
        
        // Disable the save button initially (until changes are made)
        saveButton.setDisable(true);
    }
    
    /**
     * Sets up the audio control listeners
     */
    private void setupAudioControls() {
        // Set initial state based on AudioManager
        boolean allMuted = audioManager.isAllMuted();
        muteToggle.setSelected(allMuted);
        muteToggle.setText(allMuted ? "Muted" : "Enabled");
        
        // Set initial slider values
        double musicVolume = audioManager.getMusicVolume() * 100;
        double sfxVolume = audioManager.getSfxVolume() * 100;
        
        musicVolumeSlider.setValue(musicVolume);
        sfxVolumeSlider.setValue(sfxVolume);
        musicVolumeLabel.setText(String.format("%.0f%%", musicVolume));
        sfxVolumeLabel.setText(String.format("%.0f%%", sfxVolume));
        
        // Update slider sensitivity when mute is toggled
        updateSlidersEnabled(!allMuted);
        
        // Add listeners for sliders
        musicVolumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double value = newVal.doubleValue();
            musicVolumeLabel.setText(String.format("%.0f%%", value));
            // Apply volume change immediately
            audioManager.setMusicVolume(value / 100.0);
            settingsChanged = true;
            saveButton.setDisable(false);
        });
        
        sfxVolumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double value = newVal.doubleValue();
            sfxVolumeLabel.setText(String.format("%.0f%%", value));
            // Apply volume change immediately
            audioManager.setSfxVolume(value / 100.0);
            settingsChanged = true;
            saveButton.setDisable(false);
        });
        
        // Add mouse event handlers for SFX slider to play test sound
        sfxVolumeSlider.setOnMousePressed(event -> {
            if (!muteToggle.isSelected()) {
                audioManager.playSoundEffect(AudioManager.SFX_BUTTON_CLICK);
            }
        });
        
        sfxVolumeSlider.setOnMouseReleased(event -> {
            if (!muteToggle.isSelected()) {
                audioManager.playSoundEffect(AudioManager.SFX_BUTTON_CLICK);
            }
        });
        
        // Disable sliders if audio is muted
        muteToggle.selectedProperty().addListener((obs, oldVal, newVal) -> {
            updateSlidersEnabled(!newVal);
            settingsChanged = true;
            saveButton.setDisable(false);
        });
    }
    
    /**
     * Enable/disable sliders based on mute state
     */
    private void updateSlidersEnabled(boolean enabled) {
        musicVolumeSlider.setDisable(!enabled);
        sfxVolumeSlider.setDisable(!enabled);
        musicVolumeLabel.setOpacity(enabled ? 1.0 : 0.5);
        sfxVolumeLabel.setOpacity(enabled ? 1.0 : 0.5);
    }
    
    /**
     * Handle mute toggle button click
     */
    @FXML
    private void handleMuteToggle() {
        boolean muted = muteToggle.isSelected();
        muteToggle.setText(muted ? "Muted" : "Enabled");
        
        // Apply mute setting immediately
        audioManager.setAllMuted(muted);
        
        // Update UI but don't save changes until Save button is pressed
        updateSlidersEnabled(!muted);
        
        // Play click sound if unmuting
        if (!muted) {
            audioManager.playSoundEffect(AudioManager.SFX_BUTTON_CLICK);
        }
        
        // Mark settings as changed
        settingsChanged = true;
        saveButton.setDisable(false);
    }
    
    /**
     * Handle change password button
     */
    @FXML
    private void handleChangePassword() {
        // Skip if not logged in
        if (currentLoggedInUsername == null || currentLoggedInUsername.trim().isEmpty() || currentLoggedInUsername.equals("Guest")) {
            showAlert(Alert.AlertType.WARNING, "Not Logged In", "You need to be logged in to change your password.");
            return;
        }
        
        // Create dialog for password change
        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Change Password");
        dialog.setHeaderText("Enter your current password and new password");
        
        // Set the button types
        ButtonType changeButtonType = new ButtonType("Change Password", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(changeButtonType, ButtonType.CANCEL);
        
        // Create the form grid
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        PasswordField oldPasswordField = new PasswordField();
        oldPasswordField.setPromptText("Current Password");
        PasswordField newPasswordField = new PasswordField();
        newPasswordField.setPromptText("New Password");
        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("Confirm New Password");
        
        grid.add(new Label("Current Password:"), 0, 0);
        grid.add(oldPasswordField, 1, 0);
        grid.add(new Label("New Password:"), 0, 1);
        grid.add(newPasswordField, 1, 1);
        grid.add(new Label("Confirm New Password:"), 0, 2);
        grid.add(confirmPasswordField, 1, 2);
        
        dialog.getDialogPane().setContent(grid);
        
        // Enable/Disable change button depending on whether all fields are filled
        Node changeButton = dialog.getDialogPane().lookupButton(changeButtonType);
        changeButton.setDisable(true);
        
        // Validator for all fields
        ChangeListener<String> validator = (observable, oldValue, newValue) -> {
            boolean isValid = !oldPasswordField.getText().trim().isEmpty() && 
                              !newPasswordField.getText().trim().isEmpty() && 
                              !confirmPasswordField.getText().trim().isEmpty();
            changeButton.setDisable(!isValid);
        };
        
        oldPasswordField.textProperty().addListener(validator);
        newPasswordField.textProperty().addListener(validator);
        confirmPasswordField.textProperty().addListener(validator);
        
        // Convert the result to Boolean
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == changeButtonType) {
                return Boolean.TRUE;
            }
            return Boolean.FALSE;
        });
        
        Optional<Boolean> result = dialog.showAndWait();
        
        result.ifPresent(confirm -> {
            if (confirm) {
                // First check if new password matches confirmation
                if (!newPasswordField.getText().equals(confirmPasswordField.getText())) {
                    showAlert(Alert.AlertType.ERROR, "Password Mismatch", "New passwords do not match. Please try again.");
                    return;
                }
                
                // Verify current password and change it if correct
                if (!UserAuth.authenticateUser(currentLoggedInUsername, oldPasswordField.getText())) {
                    showAlert(Alert.AlertType.ERROR, "Change Failed", "Current password is incorrect. Please try again.");
                    return;
                }
                
                // Try to change password
                boolean success = UserAuth.changePassword(currentLoggedInUsername, newPasswordField.getText());
                
                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Password Changed", "Your password has been successfully changed.");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Change Failed", "Failed to change password. Please try again later.");
                }
            }
        });
    }
    
    /**
     * Handle delete account button
     */
    @FXML
    private void handleDeleteAccount() {
        // Skip if not logged in
        if (currentLoggedInUsername == null || currentLoggedInUsername.trim().isEmpty() || currentLoggedInUsername.equals("Guest")) {
            showAlert(Alert.AlertType.WARNING, "Not Logged In", "You need to be logged in to delete your account.");
            return;
        }
        
        // Create confirmation dialog
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Delete Account");
        dialog.setHeaderText("Are you sure you want to delete your account?\nThis action cannot be undone, and all your scores will be lost.");
        
        // Set the button types
        ButtonType deleteButtonType = new ButtonType("Delete", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(deleteButtonType, ButtonType.CANCEL);
        
        // Create the password field for confirmation
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password to confirm");
        
        grid.add(new Label("Password:"), 0, 0);
        grid.add(passwordField, 1, 0);
        
        dialog.getDialogPane().setContent(grid);
        
        // Enable/Disable delete button depending on whether password is entered
        Node deleteButton = dialog.getDialogPane().lookupButton(deleteButtonType);
        deleteButton.setDisable(true);
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            deleteButton.setDisable(newValue.trim().isEmpty());
        });
        
        // Convert the result to the password
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == deleteButtonType) {
                return passwordField.getText();
            }
            return null;
        });
        
        Optional<String> result = dialog.showAndWait();
        
        result.ifPresent(password -> {
            if (password != null && !password.isEmpty()) {
                // Verify password
                if (!UserAuth.authenticateUser(currentLoggedInUsername, password)) {
                    showAlert(Alert.AlertType.ERROR, "Deletion Failed", "Incorrect password. Please try again.");
                    return;
                }
                
                // Try to delete the account
                boolean success = UserAuth.deleteAccount(currentLoggedInUsername);
                
                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Account Deleted", "Your account has been successfully deleted. The application will now return to the welcome screen.");
                    uiScreens.showFXMLWelcomeScreen();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Deletion Failed", "Failed to delete account. Please try again later.");
                }
            }
        });
    }


    /**
     * Handle view leaderboard button
     */
    @FXML
    private void handleViewLeaderboard() {
        File scoreFile = new File("jfxmaze_scores.txt");
        if (!scoreFile.exists() || scoreFile.length() == 0) {
            showAlert(Alert.AlertType.INFORMATION, "No Scores", "No scores found to display.");
            return;
        }

        try {
            // Read scores in reverse order (newest first)
            List<String> allLines = Files.readAllLines(scoreFile.toPath());
            List<String> reversedLines = new ArrayList<>(allLines);
            Collections.reverse(reversedLines);

            // Create dialog for scores display
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("JFXMaze Leaderboard");
            dialog.setHeaderText("High Scores (Newest First)");
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

            // Create text area for scores
            TextArea textArea = new TextArea();
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setPrefWidth(550);
            textArea.setPrefHeight(400);

            // Add scores to text area
            StringBuilder scoreText = new StringBuilder();
            for (String line : reversedLines) {
                scoreText.append(line).append("\n");
            }
            textArea.setText(scoreText.toString());

            // Add text area to dialog
            VBox content = new VBox(textArea);
            content.setSpacing(10);
            VBox.setVgrow(textArea, Priority.ALWAYS);
            dialog.getDialogPane().setContent(content);

            // Show dialog
            dialog.showAndWait();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Could not read scores: " + e.getMessage());
        }
    }

    /**
     * Handle export scores button
     */
    @FXML
    private void handleExportScores() {
        File scoreFile = new File("jfxmaze_scores.txt");
        if (!scoreFile.exists() || scoreFile.length() == 0) {
            showAlert(Alert.AlertType.INFORMATION, "No Scores", "No scores found to export.");
            return;
        }

        // Get user's download directory
        String userHome = System.getProperty("user.home");
        File downloadsDir = new File(userHome, "Downloads");

        // Create file chooser
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("Select Export Directory");
        dirChooser.setInitialDirectory(downloadsDir.exists() ? downloadsDir : new File(userHome));

        // Show dialog
        File selectedDir = dirChooser.showDialog(primaryStage);
        if (selectedDir != null) {
            // Generate unique filename
            String timestamp = String.valueOf(System.currentTimeMillis());
            File exportFile = new File(selectedDir, "jfxmaze_scores_" + timestamp + ".txt");
            int counter = 1;
            while (exportFile.exists()) {
                exportFile = new File(selectedDir, "jfxmaze_scores_" + timestamp + "_" + counter + ".txt");
                counter++;
            }

            try {
                // Copy scores file to export location
                Files.copy(scoreFile.toPath(), exportFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Scores exported to:\n" + exportFile.getAbsolutePath());
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Export failed: " + e.getMessage());
            }
        }
    }

    /**
     * Handle clear scores button
     */
    @FXML
    private void handleClearScores() {
        File scoreFile = new File("jfxmaze_scores.txt");
        if (!scoreFile.exists() || scoreFile.length() == 0) {
            showAlert(Alert.AlertType.INFORMATION, "No Scores", "No scores to clear.");
            return;
        }

        // Confirm score clearing with export option
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Clear Scores");
        confirmDialog.setHeaderText("Clear All Score Records");
        confirmDialog.setContentText("Are you sure you want to clear all scores? This action cannot be undone!");

        // Create custom buttons for the dialog
        ButtonType clearButtonType = new ButtonType("Clear Scores", ButtonBar.ButtonData.NO);
        ButtonType exportClearButtonType = new ButtonType("Export and Clear", ButtonBar.ButtonData.YES);
        ButtonType cancelButtonType = ButtonType.CANCEL;

        confirmDialog.getButtonTypes().setAll(clearButtonType, exportClearButtonType, cancelButtonType);

        Optional<ButtonType> result = confirmDialog.showAndWait();

        if (result.isPresent()) {
            if (result.get() == exportClearButtonType) {
                // Export first, then clear if export is successful
                if (exportScoresFile()) {
                    clearScoresFile(scoreFile);
                }
            } else if (result.get() == clearButtonType) {
                // Just clear scores
                clearScoresFile(scoreFile);
            }
            // If cancel, do nothing
        }
    }

    /**
     * Clears the scores file
     *
     * @param scoreFile The scores file to clear
     */
    private void clearScoresFile(File scoreFile) {
        try {
            // Now clear the file by truncating it
            new FileWriter(scoreFile, false).close();
            showAlert(Alert.AlertType.INFORMATION, "Success", "All scores have been cleared.");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to clear scores: " + e.getMessage());
        }
    }

    /**
     * Exports scores to user selected location
     *
     * @return true if export was successful
     */
    private boolean exportScoresFile() {
        File scoreFile = new File("jfxmaze_scores.txt");
        if (!scoreFile.exists() || scoreFile.length() == 0) {
            showAlert(Alert.AlertType.INFORMATION, "No Scores", "No scores found to export.");
            return false;
        }

        // Get user's download directory
        String userHome = System.getProperty("user.home");
        File downloadsDir = new File(userHome, "Downloads");

        // Create file chooser
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("Select Export Directory");
        dirChooser.setInitialDirectory(downloadsDir.exists() ? downloadsDir : new File(userHome));

        // Show dialog
        File selectedDir = dirChooser.showDialog(primaryStage);
        if (selectedDir != null) {
            // Generate unique filename
            String timestamp = String.valueOf(System.currentTimeMillis());
            File exportFile = new File(selectedDir, "jfxmaze_scores_" + timestamp + ".txt");
            int counter = 1;
            while (exportFile.exists()) {
                exportFile = new File(selectedDir, "jfxmaze_scores_" + timestamp + "_" + counter + ".txt");
                counter++;
            }

            try {
                // Copy scores file to export location
                Files.copy(scoreFile.toPath(), exportFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Scores exported to:\n" + exportFile.getAbsolutePath());
                return true;
            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Error", "Export failed: " + e.getMessage());
                return false;
            }
        }
        return false;
    }

    /**
     * Handle save settings button
     */
    @FXML
    private void handleSaveSettings() {
        // Save audio settings
        boolean muted = muteToggle.isSelected();
        double musicVolume = musicVolumeSlider.getValue() / 100.0;
        double sfxVolume = sfxVolumeSlider.getValue() / 100.0;
        
        audioManager.setAllMuted(muted);
        audioManager.setMusicVolume(musicVolume);
        audioManager.setSfxVolume(sfxVolume);
        
        // Play a sound only if not muted
        if (!muted) {
            audioManager.playSoundEffect(AudioManager.SFX_BUTTON_CLICK);
        }
        
        // Reset change flag and disable save button
        settingsChanged = false;
        saveButton.setDisable(true);
        
        showAlert(Alert.AlertType.INFORMATION, "Settings Saved", "Your settings have been saved successfully.");
    }
    
    /**
     * Sets the game controller reference
     */
    public void setGameController(GameController gameController) {
        this.gameController = gameController;
    }
    
    /**
     * Sets the UI screens reference
     */
    public void setUIScreens(UIScreens uiScreens) {
        this.uiScreens = uiScreens;
    }
    
    /**
     * Sets the primary stage reference
     */
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
    
    /**
     * Sets the current logged in username
     */
    public void setCurrentLoggedInUsername(String username) {
        this.currentLoggedInUsername = username;
        if (username != null && !username.isEmpty()) {
            usernameLabel.setText("Current User: " + username);
        } else {
            usernameLabel.setText("Current User: Guest");
        }
    }
    
    /**
     * Shows an alert with the specified parameters
     */
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Handle back button
     */
    @FXML
    private void handleBackToMenu() {
        // Play button click sound
        if (!audioManager.isAllMuted()) {
            audioManager.playSoundEffect(AudioManager.SFX_BUTTON_CLICK);
        }
        
        // Check for unsaved changes
        if (settingsChanged) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Unsaved Changes");
            alert.setHeaderText("You have unsaved changes.");
            alert.setContentText("Do you want to save your changes before going back?");
            
            ButtonType buttonSave = new ButtonType("Save");
            ButtonType buttonDiscard = new ButtonType("Discard");
            ButtonType buttonCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            
            alert.getButtonTypes().setAll(buttonSave, buttonDiscard, buttonCancel);
            
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == buttonSave) {
                handleSaveSettings();
            } else if (result.get() == buttonCancel) {
                return; // Stay on settings screen
            }
            // If 'Discard', we just continue to go back
        }
        
        // Go back to the main menu
        uiScreens.showFXMLWelcomeScreen();
    }
} 