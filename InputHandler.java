import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * InputHandler manages all keyboard input for the Tetris game.
 * 
 * This class implements the KeyListener interface and handles:
 * - Piece movement (left, right, rotation)
 * - Piece dropping (soft drop, hard drop)
 * - Game controls (pause, restart, hold)
 * 
 * Key Mappings:
 * - Arrow Keys: Movement and rotation
 * - Space: Hard drop (instant drop with bonus points)
 * - Down Arrow: Soft drop (faster fall with small bonus)
 * - C/Shift: Hold piece
 * - P: Pause/unpause
 * - R: Restart when game over
 */
public class InputHandler implements KeyListener {
    
    /**
     * Interface for handling input events from the InputHandler
     */
    public interface InputEventListener {
        void onMoveLeft();
        void onMoveRight();
        void onRotateClockwise();
        void onRotateCounterclockwise();
        void onSoftDrop();
        void onHardDrop();
        void onHoldPiece();
        void onPause();
        void onRestart();
    }
    
    // Reference to the object that will handle input events
    private InputEventListener eventListener;
    
    // Key state tracking for advanced input handling
    private boolean[] keyPressed = new boolean[256];
    private long[] keyPressTime = new long[256];
    
    // Input timing constants for responsive controls
    private static final long KEY_REPEAT_DELAY = 150;  // Initial delay before key repeat (ms)
    private static final long KEY_REPEAT_RATE = 50;    // Rate of key repeat (ms)
    private static final long ROTATION_COOLDOWN = 100; // Prevent rotation spam (ms)
    
    // Last action times for cooldown management
    private long lastRotationTime = 0;
    private long lastHardDropTime = 0;
    
    /**
     * Creates a new InputHandler with the specified event listener
     * 
     * @param eventListener The object that will receive input events
     */
    public InputHandler(InputEventListener eventListener) {
        this.eventListener = eventListener;
    }
    
    /**
     * Sets the event listener for this input handler
     * 
     * @param eventListener The new event listener
     */
    public void setEventListener(InputEventListener eventListener) {
        this.eventListener = eventListener;
    }
    
    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        
        // Prevent array bounds issues
        if (keyCode >= keyPressed.length) {
            return;
        }
        
        // Track key press state and timing
        if (!keyPressed[keyCode]) {
            keyPressed[keyCode] = true;
            keyPressTime[keyCode] = System.currentTimeMillis();
            
            // Handle initial key press
            handleKeyPress(keyCode);
        }
    }
    
    @Override
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();
        
        // Prevent array bounds issues
        if (keyCode >= keyPressed.length) {
            return;
        }
        
        // Reset key state
        keyPressed[keyCode] = false;
        keyPressTime[keyCode] = 0;
    }
    
    @Override
    public void keyTyped(KeyEvent e) {
        // Not used in this implementation
    }
    
    /**
     * Handles initial key press events
     * 
     * @param keyCode The key code that was pressed
     */
    private void handleKeyPress(int keyCode) {
        if (eventListener == null) {
            return;
        }
        
        long currentTime = System.currentTimeMillis();
        
        switch (keyCode) {
            // Movement keys
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_A:
                eventListener.onMoveLeft();
                break;
                
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_D:
                eventListener.onMoveRight();
                break;
            
            // Rotation keys
            case KeyEvent.VK_UP:
            case KeyEvent.VK_W:
            case KeyEvent.VK_X:
                // Clockwise rotation with cooldown
                if (currentTime - lastRotationTime > ROTATION_COOLDOWN) {
                    eventListener.onRotateClockwise();
                    lastRotationTime = currentTime;
                }
                break;
                
            case KeyEvent.VK_Z:
            case KeyEvent.VK_CONTROL:
                // Counterclockwise rotation with cooldown
                if (currentTime - lastRotationTime > ROTATION_COOLDOWN) {
                    eventListener.onRotateCounterclockwise();
                    lastRotationTime = currentTime;
                }
                break;
            
            // Drop keys
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_S:
                eventListener.onSoftDrop();
                break;
                
            case KeyEvent.VK_SPACE:
                // Hard drop with cooldown to prevent accidents
                if (currentTime - lastHardDropTime > 200) {
                    eventListener.onHardDrop();
                    lastHardDropTime = currentTime;
                }
                break;
            
            // Hold piece
            case KeyEvent.VK_C:
            case KeyEvent.VK_SHIFT:
                eventListener.onHoldPiece();
                break;
            
            // Game controls
            case KeyEvent.VK_P:
            case KeyEvent.VK_ESCAPE:
                eventListener.onPause();
                break;
                
            case KeyEvent.VK_R:
                eventListener.onRestart();
                break;
                
            default:
                // Unknown key - no action
                break;
        }
    }
    
    /**
     * Updates input handling for continuous key presses (like movement)
     * This should be called regularly from the game loop to handle key repeat
     */
    public void update() {
        if (eventListener == null) {
            return;
        }
        
        long currentTime = System.currentTimeMillis();
        
        // Handle continuous movement (left/right)
        handleContinuousMovement(KeyEvent.VK_LEFT, currentTime, this::onMoveLeftRepeat);
        handleContinuousMovement(KeyEvent.VK_A, currentTime, this::onMoveLeftRepeat);
        handleContinuousMovement(KeyEvent.VK_RIGHT, currentTime, this::onMoveRightRepeat);
        handleContinuousMovement(KeyEvent.VK_D, currentTime, this::onMoveRightRepeat);
        
        // Handle continuous soft drop
        handleContinuousMovement(KeyEvent.VK_DOWN, currentTime, this::onSoftDropRepeat);
        handleContinuousMovement(KeyEvent.VK_S, currentTime, this::onSoftDropRepeat);
    }
    
    /**
     * Handles continuous key press for movement keys
     * 
     * @param keyCode The key to check
     * @param currentTime Current system time
     * @param action The action to perform on repeat
     */
    private void handleContinuousMovement(int keyCode, long currentTime, Runnable action) {
        if (keyCode >= keyPressed.length) {
            return;
        }
        
        if (keyPressed[keyCode]) {
            long pressDuration = currentTime - keyPressTime[keyCode];
            
            // After initial delay, start repeating at specified rate
            if (pressDuration > KEY_REPEAT_DELAY) {
                long timeSinceLastRepeat = (pressDuration - KEY_REPEAT_DELAY) % KEY_REPEAT_RATE;
                
                // Execute action if it's time for the next repeat
                if (timeSinceLastRepeat < 16) { // Roughly one frame at 60fps
                    action.run();
                }
            }
        }
    }
    
    /**
     * Actions for repeated key presses
     */
    private void onMoveLeftRepeat() {
        eventListener.onMoveLeft();
    }
    
    private void onMoveRightRepeat() {
        eventListener.onMoveRight();
    }
    
    private void onSoftDropRepeat() {
        eventListener.onSoftDrop();
    }
    
    /**
     * Checks if a specific key is currently pressed
     * 
     * @param keyCode The key code to check
     * @return true if the key is currently pressed
     */
    public boolean isKeyPressed(int keyCode) {
        if (keyCode >= keyPressed.length) {
            return false;
        }
        return keyPressed[keyCode];
    }
    
    /**
     * Gets the duration a key has been pressed
     * 
     * @param keyCode The key code to check
     * @return Duration in milliseconds, or 0 if not pressed
     */
    public long getKeyPressDuration(int keyCode) {
        if (keyCode >= keyPressed.length || !keyPressed[keyCode]) {
            return 0;
        }
        return System.currentTimeMillis() - keyPressTime[keyCode];
    }
    
    /**
     * Resets all key states (useful when focus is lost/gained)
     */
    public void resetKeyStates() {
        for (int i = 0; i < keyPressed.length; i++) {
            keyPressed[i] = false;
            keyPressTime[i] = 0;
        }
    }
    
    /**
     * Sets the rotation cooldown time
     * 
     * @param cooldownMs Cooldown time in milliseconds
     */
    public void setRotationCooldown(long cooldownMs) {
        // Update the cooldown by adjusting the last rotation time
        long currentTime = System.currentTimeMillis();
        lastRotationTime = currentTime - ROTATION_COOLDOWN + cooldownMs;
    }
    
    /**
     * Checks if rotation is currently on cooldown
     * 
     * @return true if rotation is on cooldown
     */
    public boolean isRotationOnCooldown() {
        return (System.currentTimeMillis() - lastRotationTime) < ROTATION_COOLDOWN;
    }
    
    /**
     * Gets a formatted string describing all current key bindings
     * 
     * @return Multi-line string with key binding information
     */
    public static String getKeyBindingsDescription() {
        return "GAME CONTROLS:\n" +
               "↑/W/X       - Rotate clockwise\n" +
               "Z/Ctrl      - Rotate counterclockwise\n" +
               "←/A         - Move left\n" +
               "→/D         - Move right\n" +
               "↓/S         - Soft drop (+1 point)\n" +
               "Space       - Hard drop (+2 points/row)\n" +
               "C/Shift     - Hold piece\n" +
               "P/Escape    - Pause game\n" +
               "R           - Restart \n\n" +
               "TIPS:\n" +
               "• Hold keys for continuous movement\n" +
               "• Rotation has cooldown to prevent spam\n" +
               "• Hard drop gives bonus points\n" +
               "• Use hold strategically for better placement";
    }
    
    /**
     * Gets a compact description of the most important controls
     * 
     * @return Single-line string with essential controls
     */
    public static String getQuickControlsDescription() {
        return "↑:Rotate ←→:Move ↓:Drop SPACE:Hard Drop C:Hold P:Pause";
    }
}