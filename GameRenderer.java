import java.awt.*;
import java.awt.geom.*;

/**
 * GameRenderer handles all visual aspects of the Tetris game including:
 * - Game board and piece rendering with enhanced graphics
 * - Beautiful UI elements with gradients and shadows
 * - Sidebar information (score, level, next pieces, hold piece)
 * - Comprehensive game instructions and controls
 * - Visual effects and animations
 * 
 * Features modern, beautiful design with:
 * - Gradient backgrounds
 * - 3D-style tile rendering with highlights and shadows
 * - Smooth anti-aliased graphics
 * - Professional color scheme
 * - Clear typography and layout
 */
public class GameRenderer {
    
    // Layout constants
    public static final int TILE_SIZE = 30;
    public static final int SIDEBAR_WIDTH = 200;  // Increased for instructions
    public static final int INSTRUCTIONS_WIDTH = 250; // Right sidebar for instructions
    
    // Calculated dimensions
    public static final int BOARD_PIXEL_WIDTH = GameBoard.BOARD_WIDTH * TILE_SIZE;
    public static final int BOARD_PIXEL_HEIGHT = (GameBoard.BOARD_HEIGHT - 1) * TILE_SIZE;
    public static final int TOTAL_WIDTH = SIDEBAR_WIDTH + BOARD_PIXEL_WIDTH + INSTRUCTIONS_WIDTH;
    public static final int TOTAL_HEIGHT = BOARD_PIXEL_HEIGHT + 50;
    
    // Visual enhancement constants
    private static final int BOARD_OFFSET_X = SIDEBAR_WIDTH;
    private static final int BOARD_OFFSET_Y = 25;
    private static final int SHADOW_OFFSET = 2;
    
    // Color scheme for beautiful design
    private static final Color BACKGROUND_COLOR = new Color(25, 25, 35);
    private static final Color BOARD_BACKGROUND = new Color(15, 15, 25);
    private static final Color GRID_LINE_COLOR = new Color(40, 40, 50);
    private static final Color PANEL_COLOR = new Color(45, 45, 55);
    private static final Color PANEL_BORDER_COLOR = new Color(80, 80, 90);
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color ACCENT_COLOR = new Color(100, 200, 255);
    private static final Color SUCCESS_COLOR = new Color(100, 255, 150);
    private static final Color WARNING_COLOR = new Color(255, 200, 100);
    
    // Fonts for different UI elements
    private Font titleFont;
    private Font headerFont;
    private Font bodyFont;
    private Font smallFont;
    
    /**
     * Initialize the renderer with fonts and resources
     */
    public GameRenderer() {
        initializeFonts();
    }
    
    /**
     * Initialize fonts for different UI elements
     */
    private void initializeFonts() {
        try {
            titleFont = new Font("Arial", Font.BOLD, 24);
            headerFont = new Font("Arial", Font.BOLD, 16);
            bodyFont = new Font("Arial", Font.PLAIN, 14);
            smallFont = new Font("Arial", Font.PLAIN, 12);
        } catch (Exception e) {
            // Fallback to system fonts
            titleFont = new Font(Font.SANS_SERIF, Font.BOLD, 24);
            headerFont = new Font(Font.SANS_SERIF, Font.BOLD, 16);
            bodyFont = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
            smallFont = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
        }
    }
    
    /**
     * Main rendering method - draws the complete game screen
     * 
     * @param g2 Graphics2D context for rendering
     * @param gameBoard The game board state
     * @param gameState The current game state
     * @param currentPiece The active tetromino (null if none)
     * @param width Panel width
     * @param height Panel height
     */
    public void render(Graphics2D g2, GameBoard gameBoard, GameState gameState, 
                      Tetromino currentPiece, int width, int height) {
        
        // Enable high-quality rendering
        setupRenderingHints(g2);
        
        // Draw background
        drawBackground(g2, width, height);
        
        // Draw game board
        drawGameBoard(g2, gameBoard);
        
        // Draw active piece and ghost if in playing state
        if (gameState.isActive() && currentPiece != null) {
            drawGhostPiece(g2, gameBoard, currentPiece);
            drawActivePiece(g2, currentPiece);
        }
        
        // Draw UI panels
        drawLeftSidebar(g2, gameBoard, gameState);
        drawInstructionsSidebar(g2);
        
        // Draw overlays based on game state
        drawGameStateOverlays(g2, gameState, width, height);
    }
    
    /**
     * Configure high-quality rendering settings
     */
    private void setupRenderingHints(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }
    
    /**
     * Draws the main background with gradient
     */
    private void drawBackground(Graphics2D g2, int width, int height) {
        // Create gradient background
        GradientPaint gradient = new GradientPaint(
            0, 0, BACKGROUND_COLOR,
            width, height, BACKGROUND_COLOR.darker()
        );
        g2.setPaint(gradient);
        g2.fillRect(0, 0, width, height);
    }
    
    /**
     * Draws the game board with enhanced visuals
     */
    private void drawGameBoard(Graphics2D g2, GameBoard gameBoard) {
        // Draw board background with shadow
        drawShadow(g2, BOARD_OFFSET_X, BOARD_OFFSET_Y, BOARD_PIXEL_WIDTH, BOARD_PIXEL_HEIGHT);
        
        // Board background
        g2.setColor(BOARD_BACKGROUND);
        g2.fillRoundRect(BOARD_OFFSET_X, BOARD_OFFSET_Y, BOARD_PIXEL_WIDTH, BOARD_PIXEL_HEIGHT, 10, 10);
        
        // Draw board cells
        for (int x = 0; x < gameBoard.getWidth(); x++) {
            for (int y = 0; y < gameBoard.getHeight() - 1; y++) { // -1 to exclude bottom border
                Color cellColor = gameBoard.getColorAt(x, y);
                int pixelX = BOARD_OFFSET_X + x * TILE_SIZE;
                int pixelY = BOARD_OFFSET_Y + y * TILE_SIZE;
                
                if (cellColor == Color.BLACK) {
                    // Empty cell - draw subtle grid lines
                    g2.setColor(GRID_LINE_COLOR);
                    g2.drawRect(pixelX, pixelY, TILE_SIZE, TILE_SIZE);
                } else if (cellColor == Color.DARK_GRAY) {
                    // Border cell - draw as solid border
                    drawBorderTile(g2, pixelX, pixelY);
                } else {
                    // Filled cell - draw with 3D effect
                    draw3DTile(g2, pixelX, pixelY, cellColor);
                }
            }
        }
    }
    
    /**
     * Draws a border tile with metal-like appearance
     */
    private void drawBorderTile(Graphics2D g2, int x, int y) {
        // Create metallic gradient
        GradientPaint metalGradient = new GradientPaint(
            x, y, new Color(120, 120, 120),
            x + TILE_SIZE, y + TILE_SIZE, new Color(60, 60, 60)
        );
        g2.setPaint(metalGradient);
        g2.fillRect(x, y, TILE_SIZE, TILE_SIZE);
        
        // Add metallic highlight
        g2.setColor(new Color(180, 180, 180));
        g2.drawLine(x, y, x + TILE_SIZE - 1, y);
        g2.drawLine(x, y, x, y + TILE_SIZE - 1);
    }
    
    /**
     * Draws a 3D-style game tile with highlights and shadows
     */
    private void draw3DTile(Graphics2D g2, int x, int y, Color baseColor) {
        // Main tile body
        g2.setColor(baseColor);
        g2.fillRect(x + 1, y + 1, TILE_SIZE - 2, TILE_SIZE - 2);
        
        // Top and left highlights (lighter)
        g2.setColor(brighterColor(baseColor));
        g2.drawLine(x, y, x + TILE_SIZE - 1, y); // Top edge
        g2.drawLine(x, y, x, y + TILE_SIZE - 1); // Left edge
        
        // Bottom and right shadows (darker)
        g2.setColor(darkerColor(baseColor));
        g2.drawLine(x + 1, y + TILE_SIZE - 1, x + TILE_SIZE - 1, y + TILE_SIZE - 1); // Bottom edge
        g2.drawLine(x + TILE_SIZE - 1, y + 1, x + TILE_SIZE - 1, y + TILE_SIZE - 1); // Right edge
        
        // Inner highlight for extra depth
        g2.setColor(new Color(255, 255, 255, 30));
        g2.fillRect(x + 2, y + 2, TILE_SIZE - 6, 2);
    }
    
    /**
     * Draws the ghost piece (preview of where piece will land)
     */
    private void drawGhostPiece(Graphics2D g2, GameBoard gameBoard, Tetromino piece) {
        int ghostY = gameBoard.calculateGhostY(piece);
        Point[] shape = piece.getCurrentShape();
        
        // Draw ghost with transparent white overlay
        g2.setColor(new Color(255, 255, 255, 60));
        for (Point p : shape) {
            int x = BOARD_OFFSET_X + (piece.getX() + p.x) * TILE_SIZE;
            int y = BOARD_OFFSET_Y + (ghostY + p.y) * TILE_SIZE;
            
            g2.fillRect(x + 2, y + 2, TILE_SIZE - 4, TILE_SIZE - 4);
            g2.setColor(new Color(255, 255, 255, 120));
            g2.drawRect(x + 1, y + 1, TILE_SIZE - 2, TILE_SIZE - 2);
            g2.setColor(new Color(255, 255, 255, 60));
        }
    }
    
    /**
     * Draws the currently active tetromino piece
     */
    private void drawActivePiece(Graphics2D g2, Tetromino piece) {
        Point[] shape = piece.getCurrentShape();
        Color pieceColor = piece.getColor();
        
        for (Point p : shape) {
            int x = BOARD_OFFSET_X + (piece.getX() + p.x) * TILE_SIZE;
            int y = BOARD_OFFSET_Y + (piece.getY() + p.y) * TILE_SIZE;
            
            draw3DTile(g2, x, y, pieceColor);
        }
    }
    
    /**
     * Draws the left sidebar with game information
     */
    private void drawLeftSidebar(Graphics2D g2, GameBoard gameBoard, GameState gameState) {
        int panelX = 15;  // More margin from edge
        int panelY = 30;  // More margin from top
        int panelWidth = SIDEBAR_WIDTH - 30;  // More margin
        
        // Hold piece section
        drawInfoPanel(g2, panelX, panelY, panelWidth, 120, "HOLD PIECE");
        int holdPiece = gameBoard.getHeldPiece();
        if (holdPiece != -1) {
            drawMiniPiece(g2, holdPiece, panelX + 40, panelY + 50);
        }
        panelY += 150;  // More space between panels
        
        // Next pieces section
        drawInfoPanel(g2, panelX, panelY, panelWidth, 300, "NEXT PIECES");
        int[] nextPieces = gameBoard.peekNextPieces(3);
        int miniY = panelY + 50;  // More space from panel top
        for (int piece : nextPieces) {
            drawMiniPiece(g2, piece, panelX + 40, miniY);
            miniY += 90;  // More space between pieces
        }
        panelY += 330;  // More space between panels
        
        // Statistics section
        drawInfoPanel(g2, panelX, panelY, panelWidth, 250, "STATISTICS");
        drawGameStatistics(g2, gameState, panelX + 20, panelY + 50);  // More internal spacing
    }
    
    /**
     * Draws the comprehensive instructions sidebar
     */
    private void drawInstructionsSidebar(Graphics2D g2) {
        int panelX = BOARD_OFFSET_X + BOARD_PIXEL_WIDTH + 15;  // More margin from board
        int panelY = 30;  // More margin from top
        int panelWidth = INSTRUCTIONS_WIDTH - 30;  // More margin
        
        // Game Controls
        drawInfoPanel(g2, panelX, panelY, panelWidth, 210, "GAME CONTROLS");
        drawInstructions(g2, panelX + 20, panelY + 50, new String[][]{  // More internal padding
            {"↑ Arrow", "Rotate piece"},
            {"← →", "Move left/right"},
            {"↓ Arrow", "Soft drop (+1 pt)"},
            {"SPACE", "Hard drop (+2 pts)"},
            {"C / SHIFT", "Hold piece"},
            {"P", "Pause game"},
            {"R", "Restart"}
        });
        panelY += 240;  // More space between panels
        
        // Scoring System
        drawInfoPanel(g2, panelX, panelY, panelWidth, 150, "SCORING");
        drawInstructions(g2, panelX + 20, panelY + 50, new String[][]{  // More internal padding
            {"1 Line", "100 × Level"},
            {"2 Lines", "300 × Level"},
            {"3 Lines", "500 × Level"},
            {"4 Lines (Tetris!)", "800 × Level"}
        });
        panelY += 180;  // More space between panels
        
        // Game Instructions
        drawInfoPanel(g2, panelX, panelY, panelWidth, 180, "INSTRUCTIONS");
        String[] tips = {
            "• T-spins score bonus points",
            "• Clear 4 lines for max score",
            "• Use hold to save pieces",
            "• Speed increases every level",
        };
        drawTipsList(g2, panelX + 20, panelY + 50, tips);  // More internal padding
    }
    
    /**
     * Draws game statistics in the sidebar
     */
    private void drawGameStatistics(Graphics2D g2, GameState gameState, int x, int y) {
        g2.setFont(bodyFont);
        
        // Score with accent color
        g2.setColor(ACCENT_COLOR);
        g2.drawString("SCORE", x, y);
        g2.setColor(TEXT_COLOR);
        g2.drawString(String.format("%,d", gameState.getScore()), x, y + 20);
        y += 50;  // More space between sections
        
        // Level with success color
        g2.setColor(SUCCESS_COLOR);
        g2.drawString("LEVEL", x, y);
        g2.setColor(TEXT_COLOR);
        g2.drawString(String.valueOf(gameState.getLevel()), x, y + 20);
        y += 50;  // More space between sections
        
        // Lines cleared
        g2.setColor(WARNING_COLOR);
        g2.drawString("LINES", x, y);
        g2.setColor(TEXT_COLOR);
        g2.drawString(String.valueOf(gameState.getTotalLinesCleared()), x, y + 20);
        y += 50;  // More space between sections
        
        // Additional stats with more spacing
        g2.setColor(TEXT_COLOR);
        g2.setFont(smallFont);
        g2.drawString("Next Level: " + gameState.getLinesUntilNextLevel(), x, y);
        y += 20;  // More space between lines
        g2.drawString("Pieces: " + gameState.getTotalPiecesDropped(), x, y);
        y += 20;  // More space between lines
        
        gameState.updateGameTime();
        long seconds = gameState.getGameDuration() / 1000;
        g2.drawString(String.format("Time: %02d:%02d", seconds / 60, seconds % 60), x, y);
    }
    
    /**
     * Draws control instructions in two-column format
     */
    private void drawInstructions(Graphics2D g2, int x, int y, String[][] instructions) {
        g2.setFont(smallFont);
        
        for (String[] instruction : instructions) {
            // Control key (colored)
            g2.setColor(ACCENT_COLOR);
            g2.drawString(instruction[0], x, y);
            
            // Description (white)
            g2.setColor(TEXT_COLOR);
            g2.drawString(instruction[1], x + 85, y);
            
            y += 20;  // More space between instruction lines
        }
    }
    
    /**
     * Draws a list of tips
     */
    private void drawTipsList(Graphics2D g2, int x, int y, String[] tips) {
        g2.setFont(smallFont);
        g2.setColor(TEXT_COLOR);
        
        for (String tip : tips) {
            g2.drawString(tip, x, y);
            y += 20;  // More space between tip lines
        }
    }
    
    /**
     * Draws an information panel with modern styling
     */
    private void drawInfoPanel(Graphics2D g2, int x, int y, int width, int height, String title) {
        // Panel shadow
        drawShadow(g2, x, y, width, height);
        
        // Panel background with gradient
        GradientPaint panelGradient = new GradientPaint(
            x, y, PANEL_COLOR,
            x, y + height, PANEL_COLOR.darker()
        );
        g2.setPaint(panelGradient);
        g2.fillRoundRect(x, y, width, height, 15, 15);
        
        // Panel border
        g2.setColor(PANEL_BORDER_COLOR);
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(x, y, width, height, 15, 15);
        
        // Title header background
        g2.setColor(PANEL_COLOR.darker());
        g2.fillRoundRect(x + 8, y + 8, width - 16, 25, 8, 8);
        
        // Title text
        g2.setColor(TEXT_COLOR);
        g2.setFont(headerFont);
        FontMetrics fm = g2.getFontMetrics();
        int titleX = x + (width - fm.stringWidth(title)) / 2;
        g2.drawString(title, titleX, y + 25);
    }
    
    /**
     * Draws a mini tetromino piece for previews
     */
    private void drawMiniPiece(Graphics2D g2, int pieceType, int x, int y) {
        if (pieceType < 0 || pieceType >= Tetromino.TETROMINO_COUNT) return;
        
        // Create a temporary tetromino to get shape data
        Tetromino temp = new Tetromino(pieceType, 0, 0);
        Point[] shape = temp.getCurrentShape();
        Color color = Tetromino.getColor(pieceType);
        
        // Draw mini tiles (smaller than game tiles)
        int miniTileSize = 18;
        for (Point p : shape) {
            int tileX = x + p.x * miniTileSize;
            int tileY = y + p.y * miniTileSize;
            
            // Mini 3D tile effect
            g2.setColor(color);
            g2.fillRect(tileX + 1, tileY + 1, miniTileSize - 2, miniTileSize - 2);
            
            g2.setColor(brighterColor(color));
            g2.drawLine(tileX, tileY, tileX + miniTileSize - 1, tileY);
            g2.drawLine(tileX, tileY, tileX, tileY + miniTileSize - 1);
            
            g2.setColor(darkerColor(color));
            g2.drawLine(tileX + 1, tileY + miniTileSize - 1, tileX + miniTileSize - 1, tileY + miniTileSize - 1);
            g2.drawLine(tileX + miniTileSize - 1, tileY + 1, tileX + miniTileSize - 1, tileY + miniTileSize - 1);
        }
    }
    
    /**
     * Draws game state overlays (countdown, pause, game over)
     */
    private void drawGameStateOverlays(Graphics2D g2, GameState gameState, int width, int height) {
        if (gameState.isCountingDown()) {
            drawCountdownOverlay(g2, gameState.getCountdownValue(), width, height);
        } else if (gameState.isPaused()) {
            drawPauseOverlay(g2, width, height);
        } else if (gameState.isGameOver()) {
            drawGameOverOverlay(g2, gameState, width, height);
        }
    }
    
    /**
     * Draws the countdown overlay
     */
    private void drawCountdownOverlay(Graphics2D g2, int countdownValue, int width, int height) {
        // Semi-transparent background
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRect(0, 0, width, height);
        
        // Countdown text with glow effect
        String text = String.valueOf(countdownValue);
        drawGlowText(g2, text, titleFont.deriveFont(120f), ACCENT_COLOR, width / 2, height / 2);
    }
    
    /**
     * Draws the pause overlay
     */
    private void drawPauseOverlay(Graphics2D g2, int width, int height) {
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, 0, width, height);
        
        drawGlowText(g2, "PAUSED", titleFont.deriveFont(60f), WARNING_COLOR, width / 2, height / 2);
        drawCenteredText(g2, "Press P to Resume", bodyFont, TEXT_COLOR, width / 2, height / 2 + 60);
    }
    
    /**
     * Draws the game over overlay
     */
    private void drawGameOverOverlay(Graphics2D g2, GameState gameState, int width, int height) {
        g2.setColor(new Color(0, 0, 0, 200));
        g2.fillRect(0, 0, width, height);
        
        int centerX = width / 2;
        int centerY = height / 2;
        
        // Game Over title
        drawGlowText(g2, "GAME OVER", titleFont.deriveFont(50f), new Color(255, 100, 100), centerX, centerY - 60);
        
        // Final score
        drawCenteredText(g2, "Final Score: " + String.format("%,d", gameState.getScore()), 
                        headerFont, ACCENT_COLOR, centerX, centerY - 20);
        
        // Level reached
        drawCenteredText(g2, "Level Reached: " + gameState.getLevel(), 
                        bodyFont, SUCCESS_COLOR, centerX, centerY + 10);
        
        // Restart instruction
        drawCenteredText(g2, "Press R to Play Again", 
                        bodyFont, TEXT_COLOR, centerX, centerY + 50);
    }
    
    /**
     * Draws text with a glow effect
     */
    private void drawGlowText(Graphics2D g2, String text, Font font, Color color, int x, int y) {
        g2.setFont(font);
        FontMetrics fm = g2.getFontMetrics();
        x -= fm.stringWidth(text) / 2;
        y += fm.getAscent() / 2;
        
        // Draw glow (multiple offset layers)
        g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 50));
        for (int i = 1; i <= 4; i++) {
            g2.drawString(text, x - i, y);
            g2.drawString(text, x + i, y);
            g2.drawString(text, x, y - i);
            g2.drawString(text, x, y + i);
        }
        
        // Draw main text
        g2.setColor(color);
        g2.drawString(text, x, y);
    }
    
    /**
     * Draws centered text
     */
    private void drawCenteredText(Graphics2D g2, String text, Font font, Color color, int centerX, int centerY) {
        g2.setFont(font);
        g2.setColor(color);
        FontMetrics fm = g2.getFontMetrics();
        int x = centerX - fm.stringWidth(text) / 2;
        int y = centerY + fm.getAscent() / 2;
        g2.drawString(text, x, y);
    }
    
    /**
     * Draws a drop shadow for panels and elements
     */
    private void drawShadow(Graphics2D g2, int x, int y, int width, int height) {
        g2.setColor(new Color(0, 0, 0, 100));
        g2.fillRoundRect(x + SHADOW_OFFSET, y + SHADOW_OFFSET, width, height, 15, 15);
    }
    
    /**
     * Creates a brighter version of a color
     */
    private Color brighterColor(Color color) {
        int r = Math.min(255, color.getRed() + 60);
        int g = Math.min(255, color.getGreen() + 60);
        int b = Math.min(255, color.getBlue() + 60);
        return new Color(r, g, b);
    }
    
    /**
     * Creates a darker version of a color
     */
    private Color darkerColor(Color color) {
        int r = Math.max(0, color.getRed() - 60);
        int g = Math.max(0, color.getGreen() - 60);
        int b = Math.max(0, color.getBlue() - 60);
        return new Color(r, g, b);
    }
    
    // Getter methods for layout calculations
    public int getTotalWidth() { return TOTAL_WIDTH; }
    public int getTotalHeight() { return TOTAL_HEIGHT; }
    public int getBoardOffsetX() { return BOARD_OFFSET_X; }
    public int getBoardOffsetY() { return BOARD_OFFSET_Y; }
}