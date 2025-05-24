package com.mnour.jfxmaze;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

/**
 * Manages scores for the JFXMaze game.
 */
public class ScoreManager {
    private static final String SCORE_FILE = "jfxmaze_scores.txt";
    private List<Score> scores = new ArrayList<>();
    
    /**
     * Loads all scores from the score file.
     */
    public void loadScores() {
        scores.clear();
        try {
            File file = new File(SCORE_FILE);
            if (!file.exists()) {
                return;
            }
            
            Scanner scanner = new Scanner(file);
            Score currentScore = null;
            
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.startsWith("Player: ")) {
                    // Start of a new score record
                    if (currentScore != null) {
                        scores.add(currentScore); // Add the previous score
                    }
                    currentScore = new Score();
                    currentScore.playerName = line.substring("Player: ".length());
                } else if (currentScore != null) {
                    if (line.startsWith("Won As: Player ")) {
                        currentScore.playerNumber = Integer.parseInt(line.substring("Won As: Player ".length()));
                    } else if (line.startsWith("Score (Dots): ")) {
                        currentScore.score = Integer.parseInt(line.substring("Score (Dots): ".length()));
                    } else if (line.startsWith("Moves: ")) {
                        currentScore.moves = Integer.parseInt(line.substring("Moves: ".length()));
                    } else if (line.startsWith("Time: ")) {
                        String timeStr = line.substring("Time: ".length());
                        currentScore.timeTaken = Integer.parseInt(timeStr.split(" ")[0]); // Extract seconds
                    } else if (line.startsWith("Maze Seed: ")) {
                        currentScore.seed = Long.parseLong(line.substring("Maze Seed: ".length()));
                    } else if (line.startsWith("Maze Size: ")) {
                        String sizeStr = line.substring("Maze Size: ".length());
                        currentScore.mazeSize = Integer.parseInt(sizeStr.split("x")[0]); // Extract first number
                    } else if (line.startsWith("Mode: ")) {
                        currentScore.isSinglePlayer = line.contains("Single Player");
                    } else if (line.startsWith("Date: ")) {
                        currentScore.date = line.substring("Date: ".length());
                    }
                }
                
                // When we hit the separator line and have a current score, add it
                if (line.startsWith("-------------------") && currentScore != null) {
                    scores.add(currentScore);
                    currentScore = null;
                }
            }
            
            // Add the last score if not already added
            if (currentScore != null) {
                scores.add(currentScore);
            }
            
            scanner.close();
        } catch (FileNotFoundException e) {
            System.err.println("Score file not found: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error loading scores: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Saves a new score to the score file.
     * 
     * @param playerName The name of the player
     * @param playerNumber The player number (1 or 2)
     * @param score The score achieved
     * @param moves The number of moves made
     * @param timeTaken The time taken in seconds
     * @param seed The maze seed
     * @param mazeSize The maze size
     * @param gameMode The game mode (SINGLE_PLAYER or DUAL_PLAYER)
     * @return true if the score was saved successfully, false otherwise
     */
    public boolean saveScore(String playerName, int playerNumber, int score, int moves, 
                          int timeTaken, long seed, int mazeSize, int gameMode) {
        try {
            File scoreFile = new File(SCORE_FILE);
            FileWriter writer = new FileWriter(scoreFile, true); // Append mode
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String dateStr = dateFormat.format(new Date());

            writer.write("Player: " + playerName + "\n");
            writer.write("Won As: Player " + playerNumber + "\n");
            writer.write("Score (Dots): " + score + "\n");
            writer.write("Moves: " + moves + "\n");
            writer.write("Time: " + timeTaken + " seconds\n");
            writer.write("Maze Seed: " + seed + "\n");
            writer.write("Maze Size: " + mazeSize + "x" + mazeSize + "\n");
            writer.write("Mode: " + (gameMode == MazeModel.SINGLE_PLAYER ? "Single Player" : "Dual Player") + "\n");
            writer.write("Date: " + dateStr + "\n");
            writer.write("----------------------------------------\n");
            writer.close();

            // Add to in-memory scores
            Score newScore = new Score();
            newScore.playerName = playerName;
            newScore.playerNumber = playerNumber;
            newScore.score = score;
            newScore.moves = moves;
            newScore.timeTaken = timeTaken;
            newScore.seed = seed;
            newScore.mazeSize = mazeSize;
            newScore.isSinglePlayer = (gameMode == MazeModel.SINGLE_PLAYER);
            newScore.date = dateStr;
            scores.add(newScore);
            
            return true;
        } catch (IOException e) {
            System.err.println("Error saving score: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Gets all scores for a specific player.
     * 
     * @param playerName The name of the player
     * @return A list of scores for the player
     */
    public List<Score> getPlayerScores(String playerName) {
        List<Score> playerScores = new ArrayList<>();
        for (Score score : scores) {
            if (score.playerName.equals(playerName)) {
                playerScores.add(score);
            }
        }
        return playerScores;
    }
    
    /**
     * Gets all scores.
     * 
     * @return A list of all scores
     */
    public List<Score> getAllScores() {
        return new ArrayList<>(scores); // Return a copy
    }
    
    /**
     * Clears all scores for a specific player.
     * 
     * @param playerName The name of the player
     * @return true if scores were cleared, false otherwise
     */
    public boolean clearPlayerScores(String playerName) {
        try {
            // First load all scores
            loadScores();
            
            // Filter out scores for the specified player
            List<Score> remainingScores = new ArrayList<>();
            for (Score score : scores) {
                if (!score.playerName.equals(playerName)) {
                    remainingScores.add(score);
                }
            }
            
            // Rewrite the file with remaining scores
            File scoreFile = new File(SCORE_FILE);
            if (scoreFile.exists()) {
                scoreFile.delete();
            }
            
            for (Score score : remainingScores) {
                saveScore(score.playerName, score.playerNumber, score.score, score.moves,
                       score.timeTaken, score.seed, score.mazeSize,
                       score.isSinglePlayer ? MazeModel.SINGLE_PLAYER : MazeModel.DUAL_PLAYER);
            }
            
            // Update the in-memory scores
            scores = remainingScores;
            return true;
        } catch (Exception e) {
            System.err.println("Error clearing player scores: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Represents a score record.
     */
    public static class Score {
        public String playerName;
        public int playerNumber;
        public int score;
        public int moves;
        public int timeTaken;
        public long seed;
        public int mazeSize;
        public boolean isSinglePlayer;
        public String date;
        
        @Override
        public String toString() {
            return String.format("%s - Score: %d, Moves: %d, Time: %ds, %s",
                    playerName, score, moves, timeTaken,
                    isSinglePlayer ? "Single Player" : "Dual Player");
        }
    }
} 