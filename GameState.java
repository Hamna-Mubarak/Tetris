/**
 * GameState class manages the overall state of the Tetris game including
 * score tracking, level progression, game phases, and statistics.
 * 
 * The game follows standard Tetris scoring and leveling mechanics:
 * - Lines cleared increase level every 10 lines
 * - Higher levels mean faster piece falling
 * - Different line clear types give different scores
 * - Game includes countdown, playing, paused, and game over states
 */
public class GameState {
    
    /**
     * Enumeration of possible game states
     */
    public enum State {
        START_COUNTDOWN,  // Initial countdown before game starts (3, 2, 1)
        PLAYING,         // Active gameplay
        PAUSED,          // Game paused by player
        GAME_OVER        // Game ended, awaiting restart
    }
    
    // Scoring constants (based on standard Tetris scoring)
    private static final int SINGLE_LINE_BASE_SCORE = 100;   // 1 line cleared
    private static final int DOUBLE_LINE_BASE_SCORE = 300;   // 2 lines cleared  
    private static final int TRIPLE_LINE_BASE_SCORE = 500;   // 3 lines cleared
    private static final int TETRIS_BASE_SCORE = 800;       // 4 lines cleared (Tetris!)
    private static final int SOFT_DROP_POINTS = 1;          // Points per cell soft dropped
    private static final int HARD_DROP_POINTS = 2;          // Points per cell hard dropped
    
    // Level progression
    private static final int LINES_PER_LEVEL = 10;          // Lines needed to advance level
    private static final int INITIAL_DROP_DELAY = 1000;     // Starting drop delay in milliseconds
    private static final int MIN_DROP_DELAY = 100;          // Minimum drop delay (max speed)
    private static final int LEVEL_SPEED_REDUCTION = 100;   // Delay reduction per level
    
    // Game state variables
    private State currentState;
    private int countdownValue;
    
    // Scoring and progression
    private long score;
    private int level;
    private int totalLinesCleared;
    private int linesInCurrentLevel;
    
    // Statistics tracking
    private int[] lineClearStats;  // Index 0=singles, 1=doubles, 2=triples, 3=tetrises
    private long totalPiecesDropped;
    private long gameStartTime;
    private long gameDuration;
    
    // Performance tracking
    private int currentDropDelay;
    
    /**
     * Initialize a new game state
     */
    public GameState() {
        reset();
    }
    
    /**
     * Resets all game state variables for a new game
     */
    public void reset() {
        currentState = State.START_COUNTDOWN;
        countdownValue = 3;
        
        score = 0;
        level = 1;
        totalLinesCleared = 0;
        linesInCurrentLevel = 0;
        
        lineClearStats = new int[4]; // All zeros
        totalPiecesDropped = 0;
        gameStartTime = System.currentTimeMillis();
        gameDuration = 0;
        
        calculateDropDelay();
    }
    
    /**
     * Updates the countdown timer and transitions to playing state when ready
     * 
     * @return true if countdown finished and game should start
     */
    public boolean updateCountdown() {
        if (currentState != State.START_COUNTDOWN) {
            return false;
        }
        
        countdownValue--;
        if (countdownValue <= 0) {
            currentState = State.PLAYING;
            gameStartTime = System.currentTimeMillis(); // Reset start time for accurate duration
            return true;
        }
        
        return false;
    }
    
    /**
     * Adds score for line clears based on the number of lines and current level
     * 
     * @param linesCleared Number of lines cleared (1-4)
     */
    public void addLineClearScore(int linesCleared) {
        if (linesCleared < 1 || linesCleared > 4) {
            return; // Invalid line clear count
        }
        
        // Calculate base score based on lines cleared
        int baseScore;
        switch (linesCleared) {
            case 1:
                baseScore = SINGLE_LINE_BASE_SCORE;
                lineClearStats[0]++;
                break;
            case 2:
                baseScore = DOUBLE_LINE_BASE_SCORE;
                lineClearStats[1]++;
                break;
            case 3:
                baseScore = TRIPLE_LINE_BASE_SCORE;
                lineClearStats[2]++;
                break;
            case 4:
                baseScore = TETRIS_BASE_SCORE;
                lineClearStats[3]++;
                break;
            default:
                return;
        }
        
        // Apply level multiplier
        score += baseScore * level;
        
        // Update line counts and check for level progression
        totalLinesCleared += linesCleared;
        linesInCurrentLevel += linesCleared;
        
        // Check for level advancement
        if (linesInCurrentLevel >= LINES_PER_LEVEL) {
            level++;
            linesInCurrentLevel -= LINES_PER_LEVEL;
            calculateDropDelay();
        }
    }
    
    /**
     * Adds score for soft dropping (down arrow key)
     * 
     * @param cellsDropped Number of cells the piece was soft dropped
     */
    public void addSoftDropScore(int cellsDropped) {
        score += cellsDropped * SOFT_DROP_POINTS;
    }
    
    /**
     * Adds score for hard dropping (space bar)
     * 
     * @param cellsDropped Number of cells the piece was hard dropped
     */
    public void addHardDropScore(int cellsDropped) {
        score += cellsDropped * HARD_DROP_POINTS;
    }
    
    /**
     * Increments the counter for pieces dropped
     */
    public void incrementPiecesDropped() {
        totalPiecesDropped++;
    }
    
    /**
     * Calculates the current drop delay based on level
     */
    private void calculateDropDelay() {
        currentDropDelay = Math.max(MIN_DROP_DELAY, 
                                  INITIAL_DROP_DELAY - (level - 1) * LEVEL_SPEED_REDUCTION);
    }
    
    /**
     * Updates the game duration (call regularly during gameplay)
     */
    public void updateGameTime() {
        if (currentState == State.PLAYING) {
            gameDuration = System.currentTimeMillis() - gameStartTime;
        }
    }
    
    /**
     * Toggles between paused and playing states
     */
    public void togglePause() {
        if (currentState == State.PLAYING) {
            currentState = State.PAUSED;
        } else if (currentState == State.PAUSED) {
            currentState = State.PLAYING;
            // Adjust start time to account for pause duration
            gameStartTime = System.currentTimeMillis() - gameDuration;
        }
    }
    
    /**
     * Sets the game state to game over
     */
    public void setGameOver() {
        currentState = State.GAME_OVER;
        updateGameTime(); // Final time update
    }
    
    /**
     * Calculates statistics for display
     * 
     * @return Formatted statistics string
     */
    public String getStatisticsString() {
        StringBuilder stats = new StringBuilder();
        stats.append("Time: ").append(formatTime(gameDuration)).append("\n");
        stats.append("Pieces: ").append(totalPiecesDropped).append("\n");
        stats.append("Lines: ").append(totalLinesCleared).append("\n");
        
        if (totalLinesCleared > 0) {
            stats.append("Singles: ").append(lineClearStats[0]).append("\n");
            stats.append("Doubles: ").append(lineClearStats[1]).append("\n");
            stats.append("Triples: ").append(lineClearStats[2]).append("\n");
            stats.append("Tetrises: ").append(lineClearStats[3]).append("\n");
            
            // Calculate efficiency (percentage of 4-line clears)
            int tetrisPercent = (lineClearStats[3] * 100) / getTotalLineClearEvents();
            stats.append("Tetris%: ").append(tetrisPercent).append("%");
        }
        
        return stats.toString();
    }
    
    /**
     * Gets the total number of line clear events (not total lines)
     * 
     * @return Total line clear events
     */
    private int getTotalLineClearEvents() {
        return lineClearStats[0] + lineClearStats[1] + lineClearStats[2] + lineClearStats[3];
    }
    
    /**
     * Formats milliseconds into MM:SS format
     * 
     * @param milliseconds Time in milliseconds
     * @return Formatted time string
     */
    private String formatTime(long milliseconds) {
        int totalSeconds = (int) (milliseconds / 1000);
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
    
    /**
     * Calculates lines remaining until next level
     * 
     * @return Lines needed for next level
     */
    public int getLinesUntilNextLevel() {
        return LINES_PER_LEVEL - linesInCurrentLevel;
    }
    
    /**
     * Gets the current lines per second rate
     * 
     * @return Lines per second, or 0 if no time has passed
     */
    public double getLinesPerSecond() {
        if (gameDuration <= 0) return 0.0;
        return totalLinesCleared / (gameDuration / 1000.0);
    }
    
    /**
     * Gets the current pieces per second rate
     * 
     * @return Pieces per second, or 0 if no time has passed
     */
    public double getPiecesPerSecond() {
        if (gameDuration <= 0) return 0.0;
        return totalPiecesDropped / (gameDuration / 1000.0);
    }
    
    // Getters for accessing game state
    
    public State getCurrentState() { return currentState; }
    public void setState(State state) { this.currentState = state; }
    
    public int getCountdownValue() { return countdownValue; }
    public void setCountdownValue(int value) { this.countdownValue = value; }
    
    public long getScore() { return score; }
    public void addScore(long points) { this.score += points; }
    
    public int getLevel() { return level; }
    public void setLevel(int level) { 
        this.level = level; 
        calculateDropDelay();
    }
    
    public int getTotalLinesCleared() { return totalLinesCleared; }
    public int getLinesInCurrentLevel() { return linesInCurrentLevel; }
    
    public int getCurrentDropDelay() { return currentDropDelay; }
    
    public long getTotalPiecesDropped() { return totalPiecesDropped; }
    
    public long getGameDuration() { return gameDuration; }
    
    public int[] getLineClearStats() { return lineClearStats.clone(); }
    
    /**
     * Checks if the game is currently active (not paused or in countdown)
     * 
     * @return true if game is in active playing state
     */
    public boolean isActive() {
        return currentState == State.PLAYING;
    }
    
    /**
     * Checks if the game is paused
     * 
     * @return true if game is paused
     */
    public boolean isPaused() {
        return currentState == State.PAUSED;
    }
    
    /**
     * Checks if the game is over
     * 
     * @return true if game is over
     */
    public boolean isGameOver() {
        return currentState == State.GAME_OVER;
    }
    
    /**
     * Checks if the game is in countdown state
     * 
     * @return true if game is counting down
     */
    public boolean isCountingDown() {
        return currentState == State.START_COUNTDOWN;
    }
}