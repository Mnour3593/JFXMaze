package com.mnour.jfxmaze;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

public class UserAuth extends Application {
    
    private static Map<String, String> users = new HashMap<>();
    private static String currentUser = null;
    private static final String USER_FILE = "jfxmaze_users.dat";
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        loadUsers();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("authentication.fxml"));
        primaryStage.setTitle("Authentication");
        primaryStage.setScene(new Scene(loader.load()));
        primaryStage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
    
    // Static methods for external access
    public static boolean isLoggedIn() {
        return currentUser != null;
    }
    
    public static String getCurrentUser() {
        return currentUser;
    }
    
    public static boolean loginUser(String username, String password) {
        if (authenticateUser(username, password)) {
            currentUser = username;
            return true;
        }
        return false;
    }
    
    public static void logoutUser() {
        currentUser = null;
    }
    
    // Controller class
    public static class AuthController {
        @FXML private StackPane rootPane;
        @FXML private VBox loginPanel, registerPanel, mainPanel;
        @FXML private TextField loginUsername, registerUsername;
        @FXML private PasswordField loginPassword, registerPassword, confirmPassword;
        @FXML private Label loginError, registerError, welcomeLabel;
        @FXML private Button loginButton;
        @FXML private Button registerButton;

        private GameController gameController;

        public void setJfxMazeApp(GameController gameController) {
            this.gameController = gameController;
        }

        @FXML
        private void initialize() {
            showLogin();
            setupBackgroundImage();
        }
        
        /**
         * Sets up the background image for the authentication screen
         */
        private void setupBackgroundImage() {
            try {
                // Load background image
                javafx.scene.image.Image backgroundImage = 
                    new javafx.scene.image.Image(getClass().getResource("/images/main_menu_bg.png").toExternalForm());
                
                // Create and configure ImageView
                javafx.scene.image.ImageView backgroundImageView = new javafx.scene.image.ImageView(backgroundImage);
                backgroundImageView.fitWidthProperty().bind(rootPane.widthProperty());
                backgroundImageView.fitHeightProperty().bind(rootPane.heightProperty());
                backgroundImageView.setPreserveRatio(false);
                
                // Add as first child
                rootPane.getChildren().add(0, backgroundImageView);
            } catch (Exception e) {
                System.err.println("Failed to load background image: " + e.getMessage());
                // Fall back to gradient background
                rootPane.setStyle("-fx-background-color: linear-gradient(to bottom, #333333, #1a1a1a);");
            }
        }

        @FXML
        private void handleLogin() {
            String username = loginUsername.getText().trim();
            String password = loginPassword.getText();

            if (username.isEmpty() || password.isEmpty()) {
                loginError.setText("Please enter username and password");
                return;
            }

            if (UserAuth.loginUser(username, password)) {
                loginError.setText("Login successful!");
                if (gameController != null) {
                    gameController.onAuthenticationSuccess(username);
                }
                clearFields();
            } else {
                loginError.setText("Invalid username or password");
            }
        }
        
        @FXML
        private void handleRegister() {
            String username = registerUsername.getText().trim();
            String password = registerPassword.getText();
            String confirm = confirmPassword.getText();
            
            if (username.isEmpty() || password.isEmpty()) {
                registerError.setText("Please enter username and password");
                return;
            }
            
            if (!password.equals(confirm)) {
                registerError.setText("Passwords don't match");
                return;
            }
            
            if (registerUser(username, password)) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText(null);
                alert.setContentText("Registration successful! You can now login.");
                alert.showAndWait();
                clearFields();
                showLogin();
            } else {
                registerError.setText("Username already exists");
            }
        }
        
        @FXML
        private void handleLogout() {
            logoutUser();
            clearFields();
            showLogin();
        }
        
        @FXML private void showLogin() { 
            loginPanel.setVisible(true);
            registerPanel.setVisible(false);
            mainPanel.setVisible(false);
        }
        
        @FXML private void showRegister() { 
            loginPanel.setVisible(false);
            registerPanel.setVisible(true);
            mainPanel.setVisible(false);
        }
        
        private void showMain() {
            loginPanel.setVisible(false);
            registerPanel.setVisible(false);
            mainPanel.setVisible(true);
        }
        
        private void clearFields() {
            loginUsername.clear();
            loginPassword.clear();
            registerUsername.clear();
            registerPassword.clear();
            confirmPassword.clear();
            loginError.setText("");
            registerError.setText("");
        }
    }
    
    // Authentication methods
    public static boolean authenticateUser(String username, String password) {
        String storedHash = users.get(username);
        return storedHash != null && storedHash.equals(hashPassword(password));
    }
    
    private static boolean registerUser(String username, String password) {
        if (users.containsKey(username)) {
            return false;
        }
        users.put(username, hashPassword(password));
        saveUsers();
        return true;
    }
    
    /**
     * Changes the password for a user.
     * 
     * @param username The username
     * @param newPassword The new password
     * @return true if successful, false otherwise
     */
    public static boolean changePassword(String username, String newPassword) {
        if (!users.containsKey(username)) {
            return false;
        }
        
        users.put(username, hashPassword(newPassword));
        saveUsers();
        return true;
    }
    
    /**
     * Deletes a user account.
     * 
     * @param username The username to delete
     * @return true if successful, false otherwise
     */
    public static boolean deleteAccount(String username) {
        if (!users.containsKey(username)) {
            return false;
        }
        
        users.remove(username);
        saveUsers();
        
        // If the deleted account is the current user, log them out
        if (currentUser != null && currentUser.equals(username)) {
            logoutUser();
        }
        
        return true;
    }
    
    private static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return password;
        }
    }

    /**
     * Loads user data from file.
     */
    public static void loadUsers() {
        try {
            if (Files.exists(Paths.get(USER_FILE))) {
                try (BufferedReader reader = new BufferedReader(new FileReader(USER_FILE))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String[] parts = line.split(":", 2);
                        if (parts.length == 2) {
                            users.put(parts[0], parts[1]);
                        }
                    }
                }
                System.out.println("Loaded " + users.size() + " users from " + USER_FILE);
            } else {
                System.out.println("No existing user file found. Creating new user database.");
                // Create default admin user if no file exists
                users.put("admin", hashPassword("admin123"));
                saveUsers(); // Save the default user
            }
        } catch (IOException e) {
            System.err.println("Error loading users: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Saves user data to file.
     */
    public static void saveUsers() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USER_FILE))) {
            for (Map.Entry<String, String> entry : users.entrySet()) {
                writer.write(entry.getKey() + ":" + entry.getValue());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving users: " + e.getMessage());
            e.printStackTrace();
        }
    }
}