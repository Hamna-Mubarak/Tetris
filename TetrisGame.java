import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * TetrisGame - Main game class that have all components of the Tetris game.
 * 
 */
public class TetrisGame extends JPanel implements InputHandler.InputEventListener {
    
    // Game components
    private GameBoard gameBoard;
    private GameState gameState;
    private GameRenderer renderer;
    private InputHandler inputHandler;
    
    // Current game piece
    private Tetromino currentPiece;
    
    // Game timing
    private Timer gameTimer;
    private Timer renderTimer;
    
    // Constants
    private static final String GAME_TITLE = "Tetris Game";
    private static final int TARGET_FPS = 60;
    private static final int RENDER_DELAY = 1000 / TARGET_FPS;
    
    /**
     * Initialize the Tetris game with all components
     */
    public TetrisGame() {
        initializeGame();
        setupUI();
        startGameLoop();
    }
    
    /**
     * Initialize all game components and systems
     */
    private void initializeGame() {
        // Initialize game components
        gameBoard = new GameBoard();
        gameState = new GameState();
        renderer = new GameRenderer();
        inputHandler = new InputHandler(this);
        
        // Initialize game state
        currentPiece = null;
        
        // Setup panel properties
        setFocusable(true);
        addKeyListener(inputHandler);
        setPreferredSize(new Dimension(renderer.getTotalWidth(), renderer.getTotalHeight()));
        
        // Set background color
        setBackground(new Color(25, 25, 35));
    }
    
    /**
     * Setup UI properties and window settings
     */
    private void setupUI() {
        // Request focus for keyboard input
        requestFocusInWindow();
        
        // Enable double buffering for smooth rendering
        setDoubleBuffered(true);
    }
    
    /**
     * Start the main game loops (logic and rendering)
     */
    private void startGameLoop() {
        // Game logic timer - handles piece dropping and game progression
        gameTimer = new Timer(gameState.getCurrentDropDelay(), new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateGameLogic();
                
                // Update timer delay based on current level
                gameTimer.setDelay(gameState.getCurrentDropDelay());
            }
        });
        
        // Rendering timer - handles smooth 60fps rendering
        renderTimer = new Timer(RENDER_DELAY, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Update input handler for key repeat
                inputHandler.update();
                
                // Update game state timing
                gameState.updateGameTime();
                
                // Repaint the game
                repaint();
            }
        });
        
        // Start both timers
        gameTimer.start();
        renderTimer.start();
    }
    
    /**
     * Main game logic update - handles automatic piece dropping and state transitions
     */
    private void updateGameLogic() {
        if (gameState.isCountingDown()) {
            // Handle countdown phase
            if (gameState.updateCountdown()) {
                // Countdown finished, spawn first piece
                spawnNewPiece();
            }
        } else if (gameState.isActive()) {
            // Handle active gameplay - automatic piece dropping
            if (currentPiece != null) {
                onSoftDrop(); // This handles the automatic drop
            }
        }
        // Paused and game over states don't need automatic updates
    }
    
    /**
     * Spawns a new tetromino piece at the top of the board
     */
    private void spawnNewPiece() {
        int pieceType = gameBoard.getNextPiece();
        currentPiece = new Tetromino(pieceType, GameBoard.SPAWN_X, GameBoard.SPAWN_Y);
        
        // Check for game over condition
        if (gameBoard.hasCollision(currentPiece)) {
            gameState.setGameOver();
            currentPiece = null;
        }
        
        // Track pieces spawned
        gameState.incrementPiecesDropped();
    }
    
    /**
     * Locks the current piece to the board and handles line clearing
     */
    private void lockPiece() {
        if (currentPiece == null) return;
        
        // Place piece on board
        gameBoard.placeTetromino(currentPiece);
        
        // Check for line clears
        int linesCleared = gameBoard.clearLines();
        if (linesCleared > 0) {
            gameState.addLineClearScore(linesCleared);
        }
        
        // Check for game over before spawning new piece
        if (gameBoard.isGameOver()) {
            gameState.setGameOver();
            currentPiece = null;
        } else {
            // Spawn next piece
            spawnNewPiece();
        }
    }
    
    // InputHandler.InputEventListener implementation
    
    @Override
    public void onMoveLeft() {
        if (!gameState.isActive() || currentPiece == null) return;
        
        if (!gameBoard.hasCollision(currentPiece, currentPiece.getX() - 1, currentPiece.getY(), currentPiece.getRotation())) {
            currentPiece.move(-1, 0);
        }
    }
    
    @Override
    public void onMoveRight() {
        if (!gameState.isActive() || currentPiece == null) return;
        
        if (!gameBoard.hasCollision(currentPiece, currentPiece.getX() + 1, currentPiece.getY(), currentPiece.getRotation())) {
            currentPiece.move(1, 0);
        }
    }
    
    @Override
    public void onRotateClockwise() {
        if (!gameState.isActive() || currentPiece == null) return;
        
        int newRotation = (currentPiece.getRotation() + 1) % 4;
        if (!gameBoard.hasCollision(currentPiece, currentPiece.getX(), currentPiece.getY(), newRotation)) {
            currentPiece.setRotation(newRotation);
        }
    }
    
    @Override
    public void onRotateCounterclockwise() {
        if (!gameState.isActive() || currentPiece == null) return;
        
        int newRotation = (currentPiece.getRotation() + 3) % 4; // Same as -1 mod 4
        if (!gameBoard.hasCollision(currentPiece, currentPiece.getX(), currentPiece.getY(), newRotation)) {
            currentPiece.setRotation(newRotation);
        }
    }
    
    @Override
    public void onSoftDrop() {
        if (!gameState.isActive() || currentPiece == null) return;
        
        if (!gameBoard.hasCollision(currentPiece, currentPiece.getX(), currentPiece.getY() + 1, currentPiece.getRotation())) {
            currentPiece.move(0, 1);
            // Award soft drop points only for manual drops (not automatic)
            if (inputHandler.isKeyPressed(java.awt.event.KeyEvent.VK_DOWN) || 
                inputHandler.isKeyPressed(java.awt.event.KeyEvent.VK_S)) {
                gameState.addSoftDropScore(1);
            }
        } else {
            // Piece can't move down - lock it
            lockPiece();
        }
    }
    
    @Override
    public void onHardDrop() {
        if (!gameState.isActive() || currentPiece == null) return;
        
        int dropDistance = 0;
        
        // Drop piece as far as possible
        while (!gameBoard.hasCollision(currentPiece, currentPiece.getX(), currentPiece.getY() + 1, currentPiece.getRotation())) {
            currentPiece.move(0, 1);
            dropDistance++;
        }
        
        // Award hard drop bonus points
        gameState.addHardDropScore(dropDistance);
        
        // Lock piece immediately
        lockPiece();
    }
    
    @Override
    public void onHoldPiece() {
        if (!gameState.isActive() || currentPiece == null) return;
        
        int newPieceType = gameBoard.holdPiece(currentPiece.getPieceType());
        
        if (newPieceType == -1) {
            // First hold or hold not available
            if (gameBoard.getHeldPiece() != -1) {
                // There was no held piece before, get next piece
                spawnNewPiece();
            }
        } else {
            // Swap with previously held piece
            currentPiece = new Tetromino(newPieceType, GameBoard.SPAWN_X, GameBoard.SPAWN_Y);
            
            // Check if swapped piece can spawn
            if (gameBoard.hasCollision(currentPiece)) {
                gameState.setGameOver();
                currentPiece = null;
            }
        }
    }
    
    @Override
    public void onPause() {
        gameState.togglePause();
        
        // Pause/resume timers based on game state
        if (gameState.isPaused()) {
            gameTimer.stop();
        } else if (gameState.isActive()) {
            gameTimer.start();
        }
    }
    
    @Override
    public void onRestart() {
        if (gameState.isGameOver()) {
            restartGame();
        }
    }
    
    /**
     * Restart the game with fresh state
     */
    private void restartGame() {
        // Stop timers
        gameTimer.stop();
        
        // Reset all game components
        gameBoard.reset();
        gameState.reset();
        currentPiece = null;
        
        // Reset input handler
        inputHandler.resetKeyStates();
        
        // Restart game loop with new timing
        gameTimer.setDelay(gameState.getCurrentDropDelay());
        gameTimer.start();
        
        // Request focus back to the game
        requestFocusInWindow();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Cast to Graphics2D for enhanced rendering capabilities
        Graphics2D g2 = (Graphics2D) g.create();
        
        try {
            // Render the entire game using the GameRenderer
            renderer.render(g2, gameBoard, gameState, currentPiece, getWidth(), getHeight());
        } finally {
            // Always dispose of graphics context
            g2.dispose();
        }
    }
    
    /**
     * Handle focus events to ensure proper keyboard input
     */
    @Override
    public void addNotify() {
        super.addNotify();
        requestFocusInWindow();
    }
    
    /**
     * Clean up resources when component is removed
     */
    @Override
    public void removeNotify() {
        super.removeNotify();
        
        // Stop timers to prevent memory leaks
        if (gameTimer != null) {
            gameTimer.stop();
        }
        if (renderTimer != null) {
            renderTimer.stop();
        }
    }
    
    /**
     * Creates and displays the game window
     * 
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        // Create game window on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            createAndShowGUI();
        });
    }
    
    /**
     * Creates and configures the main game window
     */
    private static void createAndShowGUI() {
        // Create main window
        JFrame frame = new JFrame(GAME_TITLE);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Create game instance
        TetrisGame game = new TetrisGame();
        
        // Configure frame
        frame.add(game);
        frame.setResizable(false);
        frame.pack(); // Size to preferred size
        frame.setLocationRelativeTo(null); // Center on screen
        
        // Set window icon (you can add an icon file here)
        // frame.setIconImage(Toolkit.getDefaultToolkit().getImage("tetris_icon.png"));
        
        // Make window visible
        frame.setVisible(true);
        
        // Ensure the game panel has focus for keyboard input
        game.requestFocusInWindow();
    }
    
    /**
     * Gets the current game state (useful for debugging or external access)
     * 
     * @return Current game state
     */
    public GameState getGameState() {
        return gameState;
    }
    
    /**
     * Gets the current game board (useful for debugging or external access)
     * 
     * @return Current game board
     */
    public GameBoard getGameBoard() {
        return gameBoard;
    }
    
    /**
     * Gets the current piece (useful for debugging or external access)
     * 
     * @return Current tetromino piece, or null if none active
     */
    public Tetromino getCurrentPiece() {
        return currentPiece;
    }
}