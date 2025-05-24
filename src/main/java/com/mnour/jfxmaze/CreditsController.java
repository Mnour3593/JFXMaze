package com.mnour.jfxmaze;

import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * Controller for the credits screen FXML
 */
public class CreditsController {
    @FXML private Text creditsTitle;
    @FXML private Button backButton;
    
    private UIScreens uiScreens;
    private Stage primaryStage;
    private HostServices hostServices;
    
    /**
     * Initializes the controller
     */
    @FXML
    private void initialize() {
        // This method is automatically called by the FXMLLoader
    }
    
    /**
     * Sets the UIScreens reference
     */
    public void setUIScreens(UIScreens uiScreens) {
        this.uiScreens = uiScreens;
    }
    
    /**
     * Sets the primary stage
     */
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
    
    /**
     * Sets the host services
     */
    public void setHostServices(HostServices hostServices) {
        this.hostServices = hostServices;
    }

    /**
     * Handles all hyperlink clicks using userData as the URL
     */
    @FXML
    private void handleHyperlinkClick(javafx.event.ActionEvent event) {
        if (hostServices != null && event.getSource() instanceof Hyperlink hyperlink) {
            Object url = hyperlink.getUserData();
            if (url instanceof String) {
                hostServices.showDocument((String) url);
            }
        }
    }

    /**
     * Handles the back button click
     */
    @FXML
    private void handleBack() {
        if (uiScreens != null) {
            uiScreens.showFXMLWelcomeScreen();
        }
    }
} 