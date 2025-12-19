import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;

/**
 * GameBoard class manages the Tetris game board, including collision detection,
 * line clearing, piece placement, and board state management.
 * 
 * The board is represented as a 2D array where:
 * - Black cells are empty
 * - Colored cells contain placed tetromino blocks
 * - Gray cells represent the border walls and floor
 */
public class GameBoard {
    
    // Board dimensions
    public static final int BOARD_WIDTH = 12;   // Including border walls (1 on each side)
    public static final int BOARD_HEIGHT = 24;  // Including border floor (1 at bottom)
    public static final int PLAYABLE_WIDTH = BOARD_WIDTH - 2;   // 10 columns for gameplay
    public static final int PLAYABLE_HEIGHT = BOARD_HEIGHT - 1;  // 23 rows for gameplay
    
    // Starting position for new pieces
    public static final int SPAWN_X = 5;  // Center of the board
    public static final int SPAWN_Y = 1;  // Near the top, leaving space for rotation
    
    // The game board represented as a 2D color array
    private Color[][] board;
    
    // Tetromino queue management for fair piece distribution
    private ArrayList<Integer> pieceQueue;
    private static final int QUEUE_SIZE = 4; // Number of pieces to show in "next" preview
    
    // Hold system
    private int heldPiece;
    private boolean canHold;
    
    /**
     * Initializes the game board with empty cells and border walls
     */
    public GameBoard() {
        initializeBoard();
        initializePieceQueue();
        heldPiece = -1;  // No piece held initially
        canHold = true;
    }
    
    /**
     * Sets up the board with borders and empty playing field
     */
    private void initializeBoard() {
        board = new Color[BOARD_WIDTH][BOARD_HEIGHT];
        
        for (int x = 0; x < BOARD_WIDTH; x++) {
            for (int y = 0; y < BOARD_HEIGHT; y++) {
                // Create border walls (left, right, bottom)
                if (x == 0 || x == BOARD_WIDTH - 1 || y == BOARD_HEIGHT - 1) {
                    board[x][y] = Color.DARK_GRAY;
                } else {
                    // Empty playing area
                    board[x][y] = Color.BLACK;
                }
            }
        }
    }
    
    /**
     * Initializes the piece queue using the "7-bag" system for fair distribution.
     * This ensures players get all 7 pieces before any piece repeats.
     */
    private void initializePieceQueue() {
        pieceQueue = new ArrayList<>();
        fillPieceQueue();
    }
    
    /**
     * Fills the piece queue with shuffled sets of all 7 tetromino types
     */
    private void fillPieceQueue() {
        while (pieceQueue.size() < QUEUE_SIZE * 2) { // Keep queue well-stocked
            // Create a "bag" containing all 7 piece types
            ArrayList<Integer> bag = new ArrayList<>();
            for (int i = 0; i < Tetromino.TETROMINO_COUNT; i++) {
                bag.add(i);
            }
            
            // Shuffle the bag for random order
            Collections.shuffle(bag);
            
            // Add shuffled pieces to the queue
            pieceQueue.addAll(bag);
        }
    }
    
    /**
     * Gets the next piece from the queue and maintains the queue size
     * 
     * @return The next tetromino type
     */
    public int getNextPiece() {
        if (pieceQueue.isEmpty()) {
            fillPieceQueue();
        }
        
        int nextPiece = pieceQueue.remove(0);
        fillPieceQueue(); // Keep queue filled
        
        return nextPiece;
    }
    
    /**
     * Gets a preview of upcoming pieces without removing them from the queue
     * 
     * @param count Number of pieces to preview (max QUEUE_SIZE)
     * @return Array of upcoming tetromino types
     */
    public int[] peekNextPieces(int count) {
        count = Math.min(count, Math.min(QUEUE_SIZE, pieceQueue.size()));
        int[] preview = new int[count];
        
        for (int i = 0; i < count; i++) {
            preview[i] = pieceQueue.get(i);
        }
        
        return preview;
    }
    
    /**
     * Checks if a tetromino collides with the board at the given position and rotation
     * 
     * @param tetromino The tetromino to test
     * @param x X position to test
     * @param y Y position to test  
     * @param rotation Rotation state to test
     * @return true if there is a collision, false otherwise
     */
    public boolean hasCollision(Tetromino tetromino, int x, int y, int rotation) {
        Point[] shape = tetromino.getShapeAtRotation(rotation);
        
        for (Point p : shape) {
            int boardX = x + p.x;
            int boardY = y + p.y;
            
            // Check bounds
            if (boardX < 0 || boardX >= BOARD_WIDTH || 
                boardY < 0 || boardY >= BOARD_HEIGHT) {
                return true;
            }
            
            // Check if cell is occupied (not black = empty)
            if (board[boardX][boardY] != Color.BLACK) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Checks if a tetromino at its current position has a collision
     * 
     * @param tetromino The tetromino to test
     * @return true if there is a collision, false otherwise
     */
    public boolean hasCollision(Tetromino tetromino) {
        return hasCollision(tetromino, tetromino.getX(), tetromino.getY(), tetromino.getRotation());
    }
    
    /**
     * Places a tetromino permanently on the board
     * 
     * @param tetromino The tetromino to place
     */
    public void placeTetromino(Tetromino tetromino) {
        Point[] shape = tetromino.getCurrentShape();
        Color color = tetromino.getColor();
        
        for (Point p : shape) {
            int x = tetromino.getX() + p.x;
            int y = tetromino.getY() + p.y;
            
            // Place the piece on the board
            if (x >= 0 && x < BOARD_WIDTH && y >= 0 && y < BOARD_HEIGHT) {
                board[x][y] = color;
            }
        }
        
        // Reset hold ability after placing a piece
        canHold = true;
    }
    
    /**
     * Calculates the "ghost" position for a tetromino (where it would land if dropped)
     * 
     * @param tetromino The tetromino to calculate ghost position for
     * @return Y coordinate where the piece would land
     */
    public int calculateGhostY(Tetromino tetromino) {
        int ghostY = tetromino.getY();
        
        // Keep moving down until collision is detected
        while (!hasCollision(tetromino, tetromino.getX(), ghostY + 1, tetromino.getRotation())) {
            ghostY++;
        }
        
        return ghostY;
    }
    
    /**
     * Clears completed lines and returns the number of lines cleared
     * 
     * @return Number of lines cleared (0-4)
     */
    public int clearLines() {
        int linesCleared = 0;
        
        // Check each row from bottom to top (excluding border)
        for (int y = BOARD_HEIGHT - 2; y > 0; y--) {
            if (isLineFull(y)) {
                clearLine(y);
                linesCleared++;
                y++; // Check the same row again since everything shifted down
            }
        }
        
        return linesCleared;
    }
    
    /**
     * Checks if a line is completely filled (excluding border cells)
     * 
     * @param y The row to check
     * @return true if the line is full, false otherwise
     */
    private boolean isLineFull(int y) {
        // Check only the playable area (excluding border walls)
        for (int x = 1; x < BOARD_WIDTH - 1; x++) {
            if (board[x][y] == Color.BLACK) {
                return false; // Found an empty cell
            }
        }
        return true;
    }
    
    /**
     * Clears a specific line and moves everything above it down
     * 
     * @param lineY The row to clear
     */
    private void clearLine(int lineY) {
        // Move all lines above the cleared line down by one
        for (int y = lineY; y > 1; y--) {
            for (int x = 1; x < BOARD_WIDTH - 1; x++) {
                board[x][y] = board[x][y - 1];
            }
        }
        
        // Clear the top line
        for (int x = 1; x < BOARD_WIDTH - 1; x++) {
            board[x][1] = Color.BLACK;
        }
    }
    
    /**
     * Checks if the game is over (pieces can't spawn)
     * 
     * @return true if game over, false otherwise
     */
    public boolean isGameOver() {
        // Check if the spawn area is blocked
        for (int x = SPAWN_X - 1; x <= SPAWN_X + 1; x++) {
            for (int y = SPAWN_Y; y <= SPAWN_Y + 1; y++) {
                if (x >= 1 && x < BOARD_WIDTH - 1 && 
                    y >= 1 && y < BOARD_HEIGHT - 1 && 
                    board[x][y] != Color.BLACK) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Hold system: Swaps current piece with held piece
     * 
     * @param currentPieceType The current piece type to hold
     * @return The piece type to spawn next (-1 if no piece was held)
     */
    public int holdPiece(int currentPieceType) {
        if (!canHold) {
            return -1; // Can't hold right now
        }
        
        int previousHeld = heldPiece;
        heldPiece = currentPieceType;
        canHold = false; // Can only hold once per piece
        
        return previousHeld;
    }
    
    /**
     * Resets the board for a new game
     */
    public void reset() {
        initializeBoard();
        pieceQueue.clear();
        fillPieceQueue();
        heldPiece = -1;
        canHold = true;
    }
    
    // Getters for board access and state
    
    /**
     * Gets the color at a specific board position
     * 
     * @param x X coordinate
     * @param y Y coordinate
     * @return Color at that position, or null if out of bounds
     */
    public Color getColorAt(int x, int y) {
        if (x < 0 || x >= BOARD_WIDTH || y < 0 || y >= BOARD_HEIGHT) {
            return null;
        }
        return board[x][y];
    }
    
    /**
     * Gets a copy of the entire board state
     * 
     * @return 2D array copy of the board
     */
    public Color[][] getBoardCopy() {
        Color[][] copy = new Color[BOARD_WIDTH][BOARD_HEIGHT];
        for (int x = 0; x < BOARD_WIDTH; x++) {
            System.arraycopy(board[x], 0, copy[x], 0, BOARD_HEIGHT);
        }
        return copy;
    }
    
    /**
     * Gets the currently held piece type
     * 
     * @return Held piece type, or -1 if no piece is held
     */
    public int getHeldPiece() {
        return heldPiece;
    }
    
    /**
     * Checks if the player can currently use the hold function
     * 
     * @return true if hold is available, false otherwise
     */
    public boolean canUseHold() {
        return canHold;
    }
    
    /**
     * Gets board width including borders
     * 
     * @return Board width
     */
    public int getWidth() {
        return BOARD_WIDTH;
    }
    
    /**
     * Gets board height including borders
     * 
     * @return Board height
     */
    public int getHeight() {
        return BOARD_HEIGHT;
    }
    
    /**
     * Checks if a position is within the playable area (excluding borders)
     * 
     * @param x X coordinate
     * @param y Y coordinate
     * @return true if position is in playable area
     */
    public boolean isInPlayableArea(int x, int y) {
        return x > 0 && x < BOARD_WIDTH - 1 && y > 0 && y < BOARD_HEIGHT - 1;
    }
}