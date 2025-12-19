import java.awt.Color;
import java.awt.Point;

/**
 * Tetromino class represents the tetris pieces (tetrominoes) with their shapes, rotations, and colors.
 * Each tetromino has 4 rotation states and a distinct color.
 * 
 * The seven standard tetrominoes are:
 * - I-piece (Cyan): The straight line piece
 * - J-piece (Blue): The reverse L-shaped piece  
 * - L-piece (Orange): The L-shaped piece
 * - O-piece (Yellow): The square piece
 * - S-piece (Green): The S-shaped piece
 * - T-piece (Purple): The T-shaped piece
 * - Z-piece (Red): The Z-shaped piece
 */
public class Tetromino {
    
    // Tetromino type constants for reference
    public static final int I_PIECE = 0;
    public static final int J_PIECE = 1;
    public static final int L_PIECE = 2;
    public static final int O_PIECE = 3;
    public static final int S_PIECE = 4;
    public static final int T_PIECE = 5;
    public static final int Z_PIECE = 6;
    
    // Total number of different tetromino types
    public static final int TETROMINO_COUNT = 7;
    
    /**
     * 3D array containing all tetromino shapes and their rotations
     * Format: [piece_type][rotation][point_index]
     * Each piece has 4 rotations (0-3) and 4 points defining its shape
     */
    private static final Point[][][] TETROMINO_SHAPES = {
        // I-Piece (Cyan) - The straight line piece
        {
            { new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(3, 1) }, // Horizontal
            { new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(1, 3) }, // Vertical
            { new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(3, 1) }, // Horizontal
            { new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(1, 3) }  // Vertical
        },
        
        // J-Piece (Blue) - The reverse L-shaped piece
        {
            { new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(2, 0) }, // Base horizontal, hook up-right
            { new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(2, 2) }, // Base vertical, hook down-right
            { new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(0, 2) }, // Base horizontal, hook down-left
            { new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(0, 0) }  // Base vertical, hook up-left
        },
        
        // L-Piece (Orange) - The L-shaped piece
        {
            { new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(2, 2) }, // Base horizontal, hook down-right
            { new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(0, 2) }, // Base vertical, hook down-left
            { new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(0, 0) }, // Base horizontal, hook up-left
            { new Point(1, 0), new Point(1, 1), new Point(1, 2), new Point(2, 0) }  // Base vertical, hook up-right
        },
        
        // O-Piece (Yellow) - The square piece (same in all rotations)
        {
            { new Point(0, 0), new Point(0, 1), new Point(1, 0), new Point(1, 1) },
            { new Point(0, 0), new Point(0, 1), new Point(1, 0), new Point(1, 1) },
            { new Point(0, 0), new Point(0, 1), new Point(1, 0), new Point(1, 1) },
            { new Point(0, 0), new Point(0, 1), new Point(1, 0), new Point(1, 1) }
        },
        
        // S-Piece (Green) - The S-shaped piece
        {
            { new Point(1, 0), new Point(2, 0), new Point(0, 1), new Point(1, 1) }, // Horizontal S
            { new Point(0, 0), new Point(0, 1), new Point(1, 1), new Point(1, 2) }, // Vertical S
            { new Point(1, 0), new Point(2, 0), new Point(0, 1), new Point(1, 1) }, // Horizontal S
            { new Point(0, 0), new Point(0, 1), new Point(1, 1), new Point(1, 2) }  // Vertical S
        },
        
        // T-Piece (Purple) - The T-shaped piece
        {
            { new Point(1, 0), new Point(0, 1), new Point(1, 1), new Point(2, 1) }, // T pointing up
            { new Point(1, 0), new Point(0, 1), new Point(1, 1), new Point(1, 2) }, // T pointing right
            { new Point(0, 1), new Point(1, 1), new Point(2, 1), new Point(1, 2) }, // T pointing down
            { new Point(1, 0), new Point(1, 1), new Point(2, 1), new Point(1, 2) }  // T pointing left
        },
        
        // Z-Piece (Red) - The Z-shaped piece
        {
            { new Point(0, 0), new Point(1, 0), new Point(1, 1), new Point(2, 1) }, // Horizontal Z
            { new Point(1, 0), new Point(0, 1), new Point(1, 1), new Point(0, 2) }, // Vertical Z
            { new Point(0, 0), new Point(1, 0), new Point(1, 1), new Point(2, 1) }, // Horizontal Z
            { new Point(1, 0), new Point(0, 1), new Point(1, 1), new Point(0, 2) }  // Vertical Z
        }
    };
    
    /**
     * Colors for each tetromino type with enhanced brightness and saturation
     * Colors are chosen to be visually distinct and pleasing
     */
    private static final Color[] TETROMINO_COLORS = {
        new Color(0, 255, 255),   // I-piece: Bright Cyan
        new Color(0, 0, 255),     // J-piece: Bright Blue
        new Color(255, 165, 0),   // L-piece: Orange
        new Color(255, 255, 0),   // O-piece: Bright Yellow
        new Color(0, 255, 0),     // S-piece: Bright Green
        new Color(160, 32, 240),  // T-piece: Purple
        new Color(255, 0, 0)      // Z-piece: Bright Red
    };
    
    // Instance variables
    private int pieceType;          // The type of tetromino (0-6)
    private int rotation;           // Current rotation state (0-3)
    private Point origin;           // Position on the game board
    
    /**
     * Creates a new tetromino with the specified type at the given position
     * 
     * @param pieceType The type of tetromino (0-6, use constants like I_PIECE)
     * @param x The initial x-coordinate on the game board
     * @param y The initial y-coordinate on the game board
     */
    public Tetromino(int pieceType, int x, int y) {
        if (pieceType < 0 || pieceType >= TETROMINO_COUNT) {
            throw new IllegalArgumentException("Invalid piece type: " + pieceType);
        }
        
        this.pieceType = pieceType;
        this.rotation = 0;  // Start with no rotation
        this.origin = new Point(x, y);
    }
    
    /**
     * Gets the current shape points for this tetromino
     * 
     * @return Array of 4 points representing the tetromino's current shape and rotation
     */
    public Point[] getCurrentShape() {
        return TETROMINO_SHAPES[pieceType][rotation].clone();
    }
    
    /**
     * Gets the shape points for a specific rotation without changing the current rotation
     * 
     * @param rotationState The rotation state to get (0-3)
     * @return Array of 4 points for the specified rotation
     */
    public Point[] getShapeAtRotation(int rotationState) {
        if (rotationState < 0 || rotationState > 3) {
            throw new IllegalArgumentException("Invalid rotation state: " + rotationState);
        }
        return TETROMINO_SHAPES[pieceType][rotationState].clone();
    }
    
    /**
     * Gets the color associated with this tetromino type
     * 
     * @return The color for this tetromino
     */
    public Color getColor() {
        return TETROMINO_COLORS[pieceType];
    }
    
    /**
     * Gets the color for a specific tetromino type (static method)
     * 
     * @param pieceType The tetromino type (0-6)
     * @return The color for the specified tetromino type
     */
    public static Color getColor(int pieceType) {
        if (pieceType < 0 || pieceType >= TETROMINO_COUNT) {
            return Color.WHITE; // Default color for invalid types
        }
        return TETROMINO_COLORS[pieceType];
    }
    
    /**
     * Rotates the tetromino clockwise
     */
    public void rotateClockwise() {
        rotation = (rotation + 1) % 4;
    }
    
    /**
     * Rotates the tetromino counterclockwise
     */
    public void rotateCounterclockwise() {
        rotation = (rotation + 3) % 4; // Same as -1 mod 4
    }
    
    /**
     * Sets the rotation to a specific state
     * 
     * @param newRotation The new rotation state (0-3)
     */
    public void setRotation(int newRotation) {
        if (newRotation < 0 || newRotation > 3) {
            throw new IllegalArgumentException("Invalid rotation state: " + newRotation);
        }
        this.rotation = newRotation;
    }
    
    /**
     * Moves the tetromino by the specified offset
     * 
     * @param dx Change in x-coordinate
     * @param dy Change in y-coordinate
     */
    public void move(int dx, int dy) {
        origin.x += dx;
        origin.y += dy;
    }
    
    /**
     * Sets the position of the tetromino
     * 
     * @param x New x-coordinate
     * @param y New y-coordinate
     */
    public void setPosition(int x, int y) {
        origin.x = x;
        origin.y = y;
    }
    
    // Getters
    public int getPieceType() { return pieceType; }
    public int getRotation() { return rotation; }
    public int getX() { return origin.x; }
    public int getY() { return origin.y; }
    public Point getOrigin() { return new Point(origin); } // Return a copy for safety
    
    /**
     * Creates a copy of this tetromino
     * 
     * @return A new Tetromino with the same properties
     */
    public Tetromino copy() {
        Tetromino copy = new Tetromino(this.pieceType, this.origin.x, this.origin.y);
        copy.rotation = this.rotation;
        return copy;
    }
    
    /**
     * Gets a random tetromino type (0-6)
     * 
     * @return Random tetromino type
     */
    public static int getRandomType() {
        return (int) (Math.random() * TETROMINO_COUNT);
    }
    
    @Override
    public String toString() {
        return String.format("Tetromino[type=%d, rotation=%d, pos=(%d,%d)]", 
                           pieceType, rotation, origin.x, origin.y);
    }
}