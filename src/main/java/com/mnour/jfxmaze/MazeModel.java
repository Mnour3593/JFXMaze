package com.mnour.jfxmaze;

import javafx.scene.image.Image;

import java.util.*;

/**
 * Encapsulates the maze data structure, generation algorithm,
 * and logic for player movement and game state.
 */
public class MazeModel {
    // Constants
    public static final int SINGLE_PLAYER = 1;
    public static final int DUAL_PLAYER = 2;

    public static final char WALL_CHAR = '#';
    public static final char PATH_CHAR = ' ';
    public static final char EXIT_CHAR = 'E';
    public static final char BONUS_CHAR = '.';

    public static final int BONUS_POINTS = 10;

    public static final int DEFAULT_SIZE = 21;
    public static final int MIN_SIZE = 5;
    public static final int MAX_SIZE = 51;

    // Game state variables
    private char[][] maze;
    private int mazeSize;
    private long seed;
    private int exitX, exitY;
    private int totalDots = 0;

    private int playerX, playerY;
    private int player1Score = 0;
    private int player1Moves = 0;

    private int player2X, player2Y;
    private int player2Score = 0;
    private int player2Moves = 0;

    private int gameMode;
    private int winner = 0;
    private Random random;
    
    // Added texture customization
    private Image wallImage;
    private Image pathImage;

    /**
     * Creates a new maze model with the specified size, game mode, and seed.
     * 
     * @param mazeSize The size of the maze (must be odd)
     * @param gameMode The game mode (SINGLE_PLAYER or DUAL_PLAYER)
     * @param seed The random seed for maze generation
     */
    public MazeModel(int mazeSize, int gameMode, long seed) {
        this.mazeSize = mazeSize;
        this.gameMode = gameMode;
        this.seed = seed;
        this.random = new Random(seed);
        
        initializeMazeState();
    }

    /**
     * Initializes the maze state.
     */
    private void initializeMazeState() {
        // Initialize the maze with all walls
        maze = new char[mazeSize][mazeSize];
        for (int i = 0; i < mazeSize; i++) {
            for (int j = 0; j < mazeSize; j++) {
                maze[i][j] = WALL_CHAR;
            }
        }

        // Generate the maze using Prim's algorithm
        generatePrimMaze(1, 1);
        
        // Ensure the starting cell itself is a path
        if (maze[1][1] == WALL_CHAR) maze[1][1] = PATH_CHAR;

        // Set the exit position, typically near the bottom-right
        exitX = mazeSize - 2;
        exitY = mazeSize - 2;

        // Make sure exit is accessible and not a wall
        if (maze[exitX][exitY] == WALL_CHAR) {
            boolean foundExitSpot = false;
            
            // Try to find a valid path cell near bottom-right
            int[][] searchDirections = {{0,0}, {-1,0}, {0,-1}, {1,0}, {0,1}, {-1,-1}, {1,-1}, {-1,1}, {1,1}};
            for (int[] offset : searchDirections) {
                int potentialX = mazeSize - 2 + offset[0];
                int potentialY = mazeSize - 2 + offset[1];
                if (potentialX > 0 && potentialX < mazeSize - 1 && 
                    potentialY > 0 && potentialY < mazeSize - 1 && 
                    maze[potentialX][potentialY] == PATH_CHAR) {
                    exitX = potentialX;
                    exitY = potentialY;
                    foundExitSpot = true;
                    break;
                }
            }

            if (!foundExitSpot) {
                // If still not found, iterate from bottom-right inwards to find any path cell
                for (int r = mazeSize - 2; r > 0 && !foundExitSpot; r--) {
                    for (int c = mazeSize - 2; c > 0; c--) {
                        if (maze[r][c] == PATH_CHAR) {
                            exitX = r;
                            exitY = c;
                            foundExitSpot = true;
                            break;
                        }
                    }
                }
            }

            // Last resort: if absolutely no path cell found (highly unlikely)
            if (!foundExitSpot) {
                System.err.println("CRITICAL WARNING: Could not find any valid path cell for the exit! Placing at (1,3) or (3,1) as fallback.");
                exitX = 1; exitY = 3;
                if (exitY >= mazeSize - 1 || maze[exitX][exitY] == WALL_CHAR) {
                    exitX = 3; exitY = 1;
                }
                if (exitX >= mazeSize - 1 || maze[exitX][exitY] == WALL_CHAR) {
                    exitX = 1; exitY = 1; // Player start
                }
            }
        }
        maze[exitX][exitY] = EXIT_CHAR;

        // Place bonus dots
        placeBonusDots();

        // Set player initial positions
        playerX = 1; // Player 1 always starts at (1,1)
        playerY = 1;

        if (gameMode == DUAL_PLAYER) {
            player2Moves = 0;
            player2Score = 0;

            // Try to place Player 2 near Player 1 but not on the same spot or exit
            boolean p2placed = false;
            int[][] p2Offsets = {{0, 2}, {2, 0}, {1, 1}, {0,1}, {1,0}};

            for (int[] offset : p2Offsets) {
                int checkX = playerX + offset[0];
                int checkY = playerY + offset[1];
                if (checkX > 0 && checkX < mazeSize - 1 && checkY > 0 && checkY < mazeSize - 1 &&
                        maze[checkX][checkY] == PATH_CHAR && !(checkX == exitX && checkY == exitY)) {
                    player2X = checkX;
                    player2Y = checkY;
                    p2placed = true;
                    break;
                }
            }

            if (!p2placed) {
                // Search for any valid path spot not occupied by P1 or Exit
                for (int r = 1; r < mazeSize - 1 && !p2placed; r++) {
                    for (int c = 1; c < mazeSize - 1; c++) {
                        if (maze[r][c] == PATH_CHAR && !(r == playerX && c == playerY) && !(r == exitX && c == exitY)) {
                            player2X = r;
                            player2Y = c;
                            p2placed = true;
                            break;
                        }
                    }
                }
            }
            if (!p2placed) {
                player2X = (playerX == 1 && playerY == 3) ? 3 : 1;
                player2Y = (playerX == 1 && playerY == 3) ? 1 : 3;
                if (maze[player2X][player2Y] == WALL_CHAR) {
                    player2X = playerX; player2Y = playerY;
                }
                System.err.println("Warning: Could not find ideal separate spot for Player 2. Placed at fallback: " + player2X + "," + player2Y);
            }
        } else {
            player2X = -1; // Indicates P2 is not active
            player2Y = -1;
        }
        winner = 0; // Reset winner for the new maze
    }

    /**
     * Places bonus dots in the maze.
     */
    private void placeBonusDots() {
        int dotsToPlace = mazeSize / 3;
        if (dotsToPlace < 1 && mazeSize > MIN_SIZE) dotsToPlace = 1;
        else if (mazeSize <= MIN_SIZE) dotsToPlace = 0;

        totalDots = 0;
        int attempts = 0;
        final int maxAttempts = mazeSize * mazeSize * 2;

        while (totalDots < dotsToPlace && attempts < maxAttempts) {
            int randX = 1 + random.nextInt(mazeSize - 2);
            int randY = 1 + random.nextInt(mazeSize - 2);

            if (maze[randX][randY] == PATH_CHAR &&
                    !(randX == playerX && randY == playerY) &&
                    !(gameMode == DUAL_PLAYER && randX == player2X && randY == player2Y) &&
                    !(randX == exitX && randY == exitY)) {
                maze[randX][randY] = BONUS_CHAR;
                totalDots++;
            }
            attempts++;
        }
    }

    /**
     * Generates a maze using Prim's algorithm.
     * 
     * @param startX The starting X coordinate
     * @param startY The starting Y coordinate
     */
    private void generatePrimMaze(int startX, int startY) {
        maze[startX][startY] = PATH_CHAR;

        List<int[]> frontier = new ArrayList<>();
        addFrontierCells(startX, startY, frontier);

        while (!frontier.isEmpty()) {
            int[] currentCell = frontier.remove(random.nextInt(frontier.size()));
            int r = currentCell[0];
            int c = currentCell[1];

            List<int[]> neighbors = new ArrayList<>();
            int[][] directions = {{-2, 0}, {2, 0}, {0, -2}, {0, 2}};

            for (int[] dir : directions) {
                int nr = r + dir[0];
                int nc = c + dir[1];
                if (nr > 0 && nr < mazeSize - 1 && nc > 0 && nc < mazeSize - 1 && maze[nr][nc] == PATH_CHAR) {
                    neighbors.add(new int[]{nr, nc});
                }
            }

            if (!neighbors.isEmpty()) {
                int[] chosenNeighbor = neighbors.get(random.nextInt(neighbors.size()));
                int nr = chosenNeighbor[0];
                int nc = chosenNeighbor[1];

                int wallR = r + (nr - r) / 2;
                int wallC = c + (nc - c) / 2;

                maze[wallR][wallC] = PATH_CHAR;
                maze[r][c] = PATH_CHAR;

                addFrontierCells(r, c, frontier);
            }
        }
        
        // Ensure borders are walls
        for (int i = 0; i < mazeSize; i++) {
            maze[0][i] = WALL_CHAR;
            maze[mazeSize-1][i] = WALL_CHAR;
            maze[i][0] = WALL_CHAR;
            maze[i][mazeSize-1] = WALL_CHAR;
        }
    }

    /**
     * Adds frontier cells to the list.
     * 
     * @param r The row
     * @param c The column
     * @param frontier The list of frontier cells
     */
    private void addFrontierCells(int r, int c, List<int[]> frontier) {
        int[][] directions = {{-2, 0}, {2, 0}, {0, -2}, {0, 2}};
        for (int[] dir : directions) {
            int nr = r + dir[0];
            int nc = c + dir[1];

            if (nr > 0 && nr < mazeSize - 1 && nc > 0 && nc < mazeSize - 1 && maze[nr][nc] == WALL_CHAR) {
                boolean exists = false;
                for (int[] cell : frontier) {
                    if (cell[0] == nr && cell[1] == nc) {
                        exists = true;
                        break;
                    }
                }
                if (!exists) {
                    frontier.add(new int[]{nr, nc});
                }
            }
        }
    }

    /**
     * Checks if the exit is reachable from the player's starting position.
     * 
     * @return true if the exit is reachable, false otherwise
     */
    public boolean isExitReachable() {
        if (maze == null || playerX < 0 || exitX < 0) return false;
        if (playerX == exitX && playerY == exitY) return true;

        boolean[][] visited = new boolean[mazeSize][mazeSize];
        Queue<int[]> queue = new LinkedList<>();

        if (maze[1][1] != WALL_CHAR) {
            queue.offer(new int[]{1, 1});
            visited[1][1] = true;
        } else {
            System.err.println("Error in isExitReachable: Player 1 start (1,1) is a wall. Maze is likely invalid.");
            return false;
        }

        int[] dRow = {-1, 1, 0, 0};
        int[] dCol = {0, 0, -1, 1};

        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            int r = current[0];
            int c = current[1];

            if (r == exitX && c == exitY) {
                return true;
            }

            for (int i = 0; i < 4; i++) {
                int newR = r + dRow[i];
                int newC = c + dCol[i];

                if (newR >= 0 && newR < mazeSize && newC >= 0 && newC < mazeSize &&
                        !visited[newR][newC] && maze[newR][newC] != WALL_CHAR) {
                    visited[newR][newC] = true;
                    queue.offer(new int[]{newR, newC});
                }
            }
        }
        return false;
    }

    /**
     * Moves a player in the specified direction.
     * 
     * @param playerNum The player number (1 or 2)
     * @param dx The change in X coordinate
     * @param dy The change in Y coordinate
     * @return true if the move was successful, false otherwise
     */
    public boolean movePlayer(int playerNum, int dx, int dy) {
        int newX, newY;
        
        if (playerNum == 1) {
            newX = playerX + dx;
            newY = playerY + dy;
            
            if (isValidMove(newX, newY)) {
                playerX = newX;
                playerY = newY;
                player1Moves++;
                
                if (maze[playerX][playerY] == BONUS_CHAR) {
                    player1Score += BONUS_POINTS;
                    maze[playerX][playerY] = PATH_CHAR;
                }
                
                if (playerX == exitX && playerY == exitY) {
                    winner = 1;
                }
                
                return true;
            }
        } else if (playerNum == 2 && gameMode == DUAL_PLAYER) {
            newX = player2X + dx;
            newY = player2Y + dy;
            
            if (isValidMove(newX, newY)) {
                player2X = newX;
                player2Y = newY;
                player2Moves++;
                
                if (maze[player2X][player2Y] == BONUS_CHAR) {
                    player2Score += BONUS_POINTS;
                    maze[player2X][player2Y] = PATH_CHAR;
                }
                
                if (player2X == exitX && player2Y == exitY) {
                    winner = 2;
                }
                
                return true;
            }
        }
        
        return false;
    }

    /**
     * Checks if a move to the specified coordinates is valid.
     * 
     * @param x The X coordinate
     * @param y The Y coordinate
     * @return true if the move is valid, false otherwise
     */
    private boolean isValidMove(int x, int y) {
        return x >= 0 && x < mazeSize && y >= 0 && y < mazeSize && maze[x][y] != WALL_CHAR;
    }

    // Getters
    public char[][] getMaze() { return maze; }
    public int getMazeSize() { return mazeSize; }
    public long getSeed() { return seed; }
    public int getPlayerX() { return playerX; }
    public int getPlayerY() { return playerY; }
    public int getPlayer2X() { return player2X; }
    public int getPlayer2Y() { return player2Y; }
    public int getPlayer1Score() { return player1Score; }
    public int getPlayer1Moves() { return player1Moves; }
    public int getPlayer2Score() { return player2Score; }
    public int getPlayer2Moves() { return player2Moves; }
    public int getGameMode() { return gameMode; }
    public int getWinner() { return winner; }
    
    /**
     * Gets the wall texture image
     * @return The wall texture image
     */
    public Image getWallImage() {
        return wallImage;
    }
    
    /**
     * Gets the path texture image
     * @return The path texture image
     */
    public Image getPathImage() {
        return pathImage;
    }

}