# Tetris Game 

Made using **Java/Swing Tetris** with modern rendering, smooth input, and standard scoring mechanics.

---

## Overview

**Description:**  
A modern Tetris implementation featuring gradient backgrounds, 3D tiles, side panels, overlays (pause, countdown, game over), and responsive gameplay.

**Entry Point:**  
`TetrisLauncher.java` → launches the game via `TetrisGame.java`.

**Requirements:**  
- JDK 8+ (Java 11+ recommended)  
- No external dependencies

---

## Features

- **Modern UI:** Gradient background, 3D tiles, side panels, overlays via `GameRenderer.java`.
- **Gameplay Mechanics:**  
  - 7‑bag piece queue  
  - Ghost preview  
  - Line clear logic, borders, hold system (`GameBoard.java`)
- **Scoring & Levels:** Standard Tetris scoring, level-based speed (`GameState.java`)  
- **Responsive Input:** Key repeat, rotation cooldowns, hard drop safety (`InputHandler.java`)  
- **Statistics Sidebar:** Score, level, lines, time, pieces, next-level progress

---

## Controls

| Action | Key(s) |
|--------|--------|
| Rotate CW | ↑ / W / X |
| Rotate CCW | Z / Ctrl |
| Move | ← / A and → / D |
| Soft Drop | ↓ / S (+1 point per cell) |
| Hard Drop | Space (+2 points per cell) |
| Hold Piece | C / Shift |
| Pause/Resume | P / Escape |
| Restart (Game Over) | R |

---

## Build & Run

**Windows (PowerShell or CMD):**

```powershell
javac *.java
java TetrisLauncher
