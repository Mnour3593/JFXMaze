package com.mnour.jfxmaze;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Handles rendering the maze and players on a JavaFX Canvas.
 * <p>
 * Responsible for drawing the maze grid, walls, paths, bonuses, exit, and player sprites.
 * Supports both single and dual player modes, and uses textures or fallback colors as needed.
 */
public class MazeRenderer {
    // Canvas and graphics context for drawing
    private Canvas mazeCanvas;
    private GraphicsContext gc;
    // Reference to the maze model (game state)
    private MazeModel model;
    // Size of each cell in pixels
    private int currentCellSize;

    // Images for rendering maze elements
    private Image wallImage;
    private Image pathImage;
    private Image bonusImage;
    private Image exitImage;
    
    // Player images for each direction (for both players)
    private Image player1ImageUp, player1ImageDown, player1ImageLeft, player1ImageRight;
    private Image player2ImageUp, player2ImageDown, player2ImageLeft, player2ImageRight;
    
    // Currently displayed player images (based on direction)
    private Image player1CurrentImage;
    private Image player2CurrentImage;

    /**
     * Creates a new maze renderer.
     *
     * @param mazeCanvas The canvas to render on
     * @param model The maze model to render
     * @param fixedCellSize The fixed size for each cell
     */
    public MazeRenderer(Canvas mazeCanvas, MazeModel model, int fixedCellSize) {
        this.mazeCanvas = mazeCanvas;
        this.gc = mazeCanvas.getGraphicsContext2D();
        this.model = model;
        this.currentCellSize = fixedCellSize;
        
        loadImages();
        
        // Set default player images (fallback if not loaded)
        player1CurrentImage = player1ImageDown != null ? player1ImageDown : 
                              createPlaceholderImage(Color.BLUE, currentCellSize, currentCellSize, "P1");
        player2CurrentImage = player2ImageDown != null ? player2ImageDown : 
                              createPlaceholderImage(Color.RED, currentCellSize, currentCellSize, "P2");
    }

    /**
     * Loads all images/textures for maze elements and players.
     * Falls back to null if not found (handled in rendering).
     */
    private void loadImages() {
        try {
            wallImage = loadImage("/images/wall.png");
            pathImage = loadImage("/images/path.png");
            bonusImage = loadImage("/images/bonus.png");
            exitImage = loadImage("/images/exit.png");
            
            player1ImageUp = loadImage("/images/player_up.png");
            player1ImageDown = loadImage("/images/player_down.png");
            player1ImageLeft = loadImage("/images/player_left.png");
            player1ImageRight = loadImage("/images/player_right.png");
            
            player2ImageUp = loadImage("/images/player2_up.png");
            player2ImageDown = loadImage("/images/player2_down.png");
            player2ImageLeft = loadImage("/images/player2_left.png");
            player2ImageRight = loadImage("/images/player2_right.png");
        } catch (Exception e) {
            System.err.println("Error loading images: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Loads an image from the classpath.
     *
     * @param path The path to the image
     * @return The loaded image, or null if loading failed
     */
    private Image loadImage(String path) {
        java.io.InputStream stream = getClass().getResourceAsStream(path);
        if (stream == null) {
            System.err.println("Failed to load image: " + path);
            return null;
        }
        return new Image(stream);
    }

    /**
     * Creates a placeholder image (colored rectangle with text) if an image fails to load.
     *
     * @param color The color of the placeholder
     * @param width The width of the placeholder
     * @param height The height of the placeholder
     * @param text The text to display on the placeholder
     * @return The created placeholder image
     */
    private Image createPlaceholderImage(Color color, int width, int height, String text) {
        Canvas canvas = new Canvas(width, height);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(color);
        gc.fillRect(0, 0, width, height);
        if (text != null && !text.isEmpty()) {
            gc.setStroke(Color.BLACK);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, Math.min(width, height) * 0.6));
            gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);
            gc.strokeText(text, width / 2.0, height / 2.0 + Math.min(width, height) * 0.2);
        }
        return canvas.snapshot(null, null);
    }

    /**
     * Renders the entire maze, including paths, walls, bonuses, exit, and players.
     * Uses textures if available, otherwise falls back to colors/shapes.
     */
    public void renderMaze() {
        if (gc == null || model == null || mazeCanvas.getWidth() <= 0 || mazeCanvas.getHeight() <= 0) {
            return;
        }

        gc.clearRect(0, 0, mazeCanvas.getWidth(), mazeCanvas.getHeight());

        char[][] maze = model.getMaze();
        int mazeSize = model.getMazeSize();

        // Draw path/floor texture everywhere first
        if (pathImage != null) {
            for (int i = 0; i < mazeSize; i++) {
                for (int j = 0; j < mazeSize; j++) {
                    gc.drawImage(pathImage, j * currentCellSize, i * currentCellSize, currentCellSize, currentCellSize);
                }
            }
        } else {
            gc.setFill(Color.LIGHTSLATEGRAY); // Fallback path color
            gc.fillRect(0, 0, mazeCanvas.getWidth(), mazeCanvas.getHeight());
        }

        // Draw walls, bonuses, exit
        for (int i = 0; i < mazeSize; i++) {
            for (int j = 0; j < mazeSize; j++) {
                double drawX = (double) j * currentCellSize;
                double drawY = (double) i * currentCellSize;

                if (maze[i][j] == MazeModel.WALL_CHAR) {
                    if (wallImage != null) {
                        gc.drawImage(wallImage, drawX, drawY, currentCellSize, currentCellSize);
                    } else {
                        gc.setFill(Color.DARKGRAY); // Fallback wall color
                        gc.fillRect(drawX, drawY, currentCellSize, currentCellSize);
                    }
                } else if (maze[i][j] == MazeModel.BONUS_CHAR) {
                    if (bonusImage != null) {
                        gc.drawImage(bonusImage, drawX, drawY, currentCellSize, currentCellSize);
                    } else {
                        gc.setFill(Color.GOLD); // Fallback bonus
                        gc.fillOval(drawX + currentCellSize * 0.2, drawY + currentCellSize * 0.2, 
                                   currentCellSize * 0.6, currentCellSize * 0.6);
                    }
                } else if (maze[i][j] == MazeModel.EXIT_CHAR) {
                    if (exitImage != null) {
                        gc.drawImage(exitImage, drawX, drawY, currentCellSize, currentCellSize);
                    } else {
                        gc.setFill(Color.LIMEGREEN); // Fallback exit
                        gc.fillRect(drawX + currentCellSize * 0.1, drawY + currentCellSize * 0.1, 
                                   currentCellSize * 0.8, currentCellSize * 0.8);
                        gc.setStroke(Color.DARKGREEN);
                        gc.setLineWidth(2);
                        gc.strokeRect(drawX + currentCellSize * 0.1, drawY + currentCellSize * 0.1, 
                                     currentCellSize * 0.8, currentCellSize * 0.8);
                    }
                }
            }
        }

        // Draw player 1
        int playerX = model.getPlayerX();
        int playerY = model.getPlayerY();
        Image p1ToDraw = (player1CurrentImage != null) ? player1CurrentImage : 
                        createPlaceholderImage(Color.BLUE, currentCellSize, currentCellSize, "P1");
        gc.drawImage(p1ToDraw, playerY * currentCellSize, playerX * currentCellSize, currentCellSize, currentCellSize);

        // Draw player 2 (if dual mode)
        if (model.getGameMode() == MazeModel.DUAL_PLAYER) {
            int player2X = model.getPlayer2X();
            int player2Y = model.getPlayer2Y();
            
            if (player2X != -1 && player2Y != -1) {
                Image p2ToDraw = (player2CurrentImage != null) ? player2CurrentImage : 
                                createPlaceholderImage(Color.RED, currentCellSize, currentCellSize, "P2");

                // Handle overlap: If P1 and P2 are on the same spot, offset P2
                if (playerX == player2X && playerY == player2Y) {
                    double offsetX = currentCellSize * 0.3; // Offset by 30% of cell width
                    gc.drawImage(p2ToDraw, 
                                 (player2Y * currentCellSize) + offsetX, 
                                 player2X * currentCellSize, 
                                 currentCellSize, 
                                 currentCellSize);
                } else {
                    gc.drawImage(p2ToDraw, player2Y * currentCellSize, player2X * currentCellSize, 
                                currentCellSize, currentCellSize);
                }
            }
        }
    }

    /**
     * Updates the player sprite direction based on movement.
     *
     * @param playerNum The player number (1 or 2)
     * @param dx The change in X coordinate
     * @param dy The change in Y coordinate
     */
    public void updatePlayerDirection(int playerNum, int dx, int dy) {
        Image newImg = null;
        if (playerNum == 1) {
            if (dx < 0) newImg = player1ImageUp;
            else if (dx > 0) newImg = player1ImageDown;
            else if (dy < 0) newImg = player1ImageLeft;
            else if (dy > 0) newImg = player1ImageRight;
            player1CurrentImage = (newImg != null) ? newImg : player1ImageDown; // Default to down if specific direction image is null
        } else if (playerNum == 2) {
            if (dx < 0) newImg = player2ImageUp;
            else if (dx > 0) newImg = player2ImageDown;
            else if (dy < 0) newImg = player2ImageLeft;
            else if (dy > 0) newImg = player2ImageRight;
            player2CurrentImage = (newImg != null) ? newImg : player2ImageDown; // Default to down
        }
    }

    /**
     * Updates the textures used by the renderer from the model and redraws the maze.
     * Should be called after texture settings are changed.
     */
    public void updateTextures() {
        if (model != null) {
            Image newWallImage = model.getWallImage();
            Image newPathImage = model.getPathImage();

            if (newWallImage != null) {
                this.wallImage = newWallImage;
            } else {
                // Fallback or reload default if model's image is null
                this.wallImage = loadImage("/images/wall.png");
                 if (this.wallImage == null) {
                    this.wallImage = createPlaceholderImage(Color.DARKGRAY, currentCellSize, currentCellSize, "W");
                }
            }

            if (newPathImage != null) {
                this.pathImage = newPathImage;
            } else {
                // Fallback or reload default if model's image is null
                this.pathImage = loadImage("/images/path.png");
                if (this.pathImage == null) {
                    this.pathImage = createPlaceholderImage(Color.LIGHTSLATEGRAY, currentCellSize, currentCellSize, "P");
                }
            }
        } else {
            // Model is null, attempt to load default images directly
            this.wallImage = loadImage("/images/wall.png");
            this.pathImage = loadImage("/images/path.png");
             if (this.wallImage == null) {
                this.wallImage = createPlaceholderImage(Color.DARKGRAY, currentCellSize, currentCellSize, "W");
            }
            if (this.pathImage == null) {
                this.pathImage = createPlaceholderImage(Color.LIGHTSLATEGRAY, currentCellSize, currentCellSize, "P");
            }
        }
        
        // After updating textures, re-render the maze
        renderMaze();
    }

} 