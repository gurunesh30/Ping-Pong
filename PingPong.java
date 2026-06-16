import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class PingPong extends JPanel implements ActionListener, KeyListener {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 500;
    private static final int PADDLE_WIDTH = 12;
    private static final int PADDLE_HEIGHT = 90;
    private static final int BALL_SIZE = 14;
    private static final int PADDLE_SPEED = 6;
    private static final int BALL_SPEED = 5;
    private static final double SPEED_INCREMENT = 0.15;
    private static final double MAX_SPEED = 3.0;

    // Game states
    private enum GameState {
        MENU, PLAYING, GAME_OVER
    }
    
    private GameState gameState = GameState.MENU;
    private int winningScore = 5; // Default winning score
    private String winner = "";
    
    private int leftY = HEIGHT / 2 - PADDLE_HEIGHT / 2;
    private int rightY = HEIGHT / 2 - PADDLE_HEIGHT / 2;
    private int ballX = WIDTH / 2 - BALL_SIZE / 2;
    private int ballY = HEIGHT / 2 - BALL_SIZE / 2;
    private int ballDX = BALL_SPEED;
    private int ballDY = BALL_SPEED;
    private double speedMultiplier = 1.0;

    private int leftScore = 0;
    private int rightScore = 0;

    private boolean upPressed = false;
    private boolean downPressed = false;
    private boolean wPressed = false;
    private boolean sPressed = false;
    private boolean enterPressed = false;
    private boolean escPressed = false;
    
    // Menu selection tracking
    private long lastKeyPressTime = 0;
    private static final long KEY_COOLDOWN = 200; // milliseconds

    private final Timer timer;

    public PingPong() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        timer = new Timer(16, this); // ~60 FPS
        timer.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (gameState) {
            case MENU:
                // Nothing to update in menu state
                break;
                
            case PLAYING:
                updateGame();
                break;
                
            case GAME_OVER:
                // Nothing to update in game over state
                break;
        }
        
        repaint();
    }
    
    private void updateGame() {
        // Move left paddle (W/S)
        if (wPressed && leftY > 0) {
            leftY -= PADDLE_SPEED;
        }
        if (sPressed && leftY < HEIGHT - PADDLE_HEIGHT) {
            leftY += PADDLE_SPEED;
        }

        // Move right paddle (Up/Down)
        if (upPressed && rightY > 0) {
            rightY -= PADDLE_SPEED;
        }
        if (downPressed && rightY < HEIGHT - PADDLE_HEIGHT) {
            rightY += PADDLE_SPEED;
        }

        // Move ball with speed multiplier
        ballX += (int) Math.round(ballDX * speedMultiplier);
        ballY += (int) Math.round(ballDY * speedMultiplier);

        // Top/bottom wall bounce
        if (ballY <= 0 || ballY >= HEIGHT - BALL_SIZE) {
            ballDY = -ballDY;
        }

        // Left paddle collision
        if (ballX <= PADDLE_WIDTH + 20 && ballY + BALL_SIZE >= leftY && ballY <= leftY + PADDLE_HEIGHT && ballDX < 0) {
            ballDX = -ballDX;
            // Add slight angle based on where ball hits paddle
            int hitPos = (ballY + BALL_SIZE / 2) - (leftY + PADDLE_HEIGHT / 2);
            ballDY = hitPos / 10;
            // Increase speed
            speedMultiplier = Math.min(speedMultiplier + SPEED_INCREMENT, MAX_SPEED);
        }

        // Right paddle collision
        if (ballX + BALL_SIZE >= WIDTH - PADDLE_WIDTH - 20 && ballY + BALL_SIZE >= rightY && ballY <= rightY + PADDLE_HEIGHT && ballDX > 0) {
            ballDX = -ballDX;
            int hitPos = (ballY + BALL_SIZE / 2) - (rightY + PADDLE_HEIGHT / 2);
            ballDY = hitPos / 10;
            // Increase speed
            speedMultiplier = Math.min(speedMultiplier + SPEED_INCREMENT, MAX_SPEED);
        }

        // Score - ball goes past left edge
        if (ballX < 0) {
            rightScore++;
            checkWinCondition();
            if (gameState == GameState.PLAYING) {
                resetBall(1);
            }
        }

        // Score - ball goes past right edge
        if (ballX > WIDTH) {
            leftScore++;
            checkWinCondition();
            if (gameState == GameState.PLAYING) {
                resetBall(-1);
            }
        }
    }
    
    private void checkWinCondition() {
        if (leftScore >= winningScore) {
            gameState = GameState.GAME_OVER;
            winner = "Left Player Wins!";
        } else if (rightScore >= winningScore) {
            gameState = GameState.GAME_OVER;
            winner = "Right Player Wins!";
        }
    }

    private void resetBall(int direction) {
        ballX = WIDTH / 2 - BALL_SIZE / 2;
        ballY = HEIGHT / 2 - BALL_SIZE / 2;
        ballDX = BALL_SPEED * direction;
        ballDY = BALL_SPEED * (Math.random() > 0.5 ? 1 : -1);
        speedMultiplier = 1.0;
    }
    
    private void resetGame() {
        leftScore = 0;
        rightScore = 0;
        leftY = HEIGHT / 2 - PADDLE_HEIGHT / 2;
        rightY = HEIGHT / 2 - PADDLE_HEIGHT / 2;
        resetBall(1);
        gameState = GameState.PLAYING;
    }
    
    private void returnToMenu() {
        gameState = GameState.MENU;
        leftScore = 0;
        rightScore = 0;
        winner = "";
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        switch (gameState) {
            case MENU:
                drawMenu(g);
                break;
            case PLAYING:
                drawGame(g);
                break;
            case GAME_OVER:
                drawGameOver(g);
                break;
        }
    }
    
    private void drawMenu(Graphics g) {
        g.setColor(Color.WHITE);
        
        // Title
        g.setFont(new Font("Monospaced", Font.BOLD, 48));
        String title = "PING PONG";
        FontMetrics fm = g.getFontMetrics();
        int titleWidth = fm.stringWidth(title);
        g.drawString(title, (WIDTH - titleWidth) / 2, HEIGHT / 3);
        
        // Winning score selector
        g.setFont(new Font("Monospaced", Font.BOLD, 24));
        String scoreText = "Winning Score: " + winningScore;
        fm = g.getFontMetrics();
        int scoreWidth = fm.stringWidth(scoreText);
        g.drawString(scoreText, (WIDTH - scoreWidth) / 2, HEIGHT / 2);
        
        // Instructions for score selection
        g.setFont(new Font("Monospaced", Font.PLAIN, 18));
        String instruction1 = "Use UP/DOWN arrows to change score";
        fm = g.getFontMetrics();
        int inst1Width = fm.stringWidth(instruction1);
        g.drawString(instruction1, (WIDTH - inst1Width) / 2, HEIGHT / 2 + 40);
        
        // Instructions for starting
        String instruction2 = "Press ENTER to start game";
        int inst2Width = fm.stringWidth(instruction2);
        g.drawString(instruction2, (WIDTH - inst2Width) / 2, HEIGHT / 2 + 80);
        
        // Controls
        g.setFont(new Font("Monospaced", Font.PLAIN, 14));
        g.drawString("Controls:", 50, HEIGHT - 100);
        g.drawString("Left Player: W/S", 50, HEIGHT - 75);
        g.drawString("Right Player: UP/DOWN", 50, HEIGHT - 50);
        g.drawString("ESC: Return to menu", 50, HEIGHT - 25);
    }
    
    private void drawGame(Graphics g) {
        g.setColor(Color.WHITE);

        // Draw paddles
        g.fillRect(20, leftY, PADDLE_WIDTH, PADDLE_HEIGHT);
        g.fillRect(WIDTH - PADDLE_WIDTH - 20, rightY, PADDLE_WIDTH, PADDLE_HEIGHT);

        // Draw ball
        g.fillOval(ballX, ballY, BALL_SIZE, BALL_SIZE);

        // Draw center line
        for (int i = 0; i < HEIGHT; i += 20) {
            g.fillRect(WIDTH / 2 - 1, i, 2, 10);
        }

        // Draw scores and winning score target
        g.setFont(new Font("Monospaced", Font.BOLD, 36));
        FontMetrics fm = g.getFontMetrics();
        
        String leftScoreText = String.valueOf(leftScore);
        String rightScoreText = String.valueOf(rightScore);
        String targetText = "First to " + winningScore;
        
        int leftScoreWidth = fm.stringWidth(leftScoreText);
        int rightScoreWidth = fm.stringWidth(rightScoreText);
        
        g.drawString(leftScoreText, WIDTH / 4 - leftScoreWidth / 2, 50);
        g.drawString(rightScoreText, WIDTH * 3 / 4 - rightScoreWidth / 2, 50);
        
        // Draw target score
        g.setFont(new Font("Monospaced", Font.PLAIN, 16));
        fm = g.getFontMetrics();
        int targetWidth = fm.stringWidth(targetText);
        g.drawString(targetText, (WIDTH - targetWidth) / 2, 30);
    }
    
    private void drawGameOver(Graphics g) {
        // Draw the game in background (frozen)
        g.setColor(new Color(255, 255, 255, 100));
        
        // Draw paddles
        g.fillRect(20, leftY, PADDLE_WIDTH, PADDLE_HEIGHT);
        g.fillRect(WIDTH - PADDLE_WIDTH - 20, rightY, PADDLE_WIDTH, PADDLE_HEIGHT);

        // Draw ball
        g.fillOval(ballX, ballY, BALL_SIZE, BALL_SIZE);

        // Draw center line
        for (int i = 0; i < HEIGHT; i += 20) {
            g.fillRect(WIDTH / 2 - 1, i, 2, 10);
        }

        // Draw scores
        g.setFont(new Font("Monospaced", Font.BOLD, 36));
        FontMetrics fm = g.getFontMetrics();
        String leftScoreText = String.valueOf(leftScore);
        String rightScoreText = String.valueOf(rightScore);
        g.drawString(leftScoreText, WIDTH / 4 - fm.stringWidth(leftScoreText) / 2, 50);
        g.drawString(rightScoreText, WIDTH * 3 / 4 - fm.stringWidth(rightScoreText) / 2, 50);
        
        // Semi-transparent overlay
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, WIDTH, HEIGHT);
        
        // Game Over text
        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, 48));
        fm = g.getFontMetrics();
        String gameOverText = "GAME OVER";
        int gameOverWidth = fm.stringWidth(gameOverText);
        g.drawString(gameOverText, (WIDTH - gameOverWidth) / 2, HEIGHT / 3);
        
        // Winner text
        g.setFont(new Font("Monospaced", Font.BOLD, 36));
        fm = g.getFontMetrics();
        int winnerWidth = fm.stringWidth(winner);
        g.drawString(winner, (WIDTH - winnerWidth) / 2, HEIGHT / 2);
        
        // Final score
        g.setFont(new Font("Monospaced", Font.PLAIN, 24));
        String finalScore = leftScore + " - " + rightScore;
        fm = g.getFontMetrics();
        int finalScoreWidth = fm.stringWidth(finalScore);
        g.drawString(finalScore, (WIDTH - finalScoreWidth) / 2, HEIGHT / 2 + 50);
        
        // Instructions
        g.setFont(new Font("Monospaced", Font.PLAIN, 18));
        String instruction1 = "Press ENTER to play again";
        fm = g.getFontMetrics();
        int inst1Width = fm.stringWidth(instruction1);
        g.drawString(instruction1, (WIDTH - inst1Width) / 2, HEIGHT / 2 + 100);
        
        String instruction2 = "Press ESC for main menu";
        int inst2Width = fm.stringWidth(instruction2);
        g.drawString(instruction2, (WIDTH - inst2Width) / 2, HEIGHT / 2 + 130);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        long currentTime = System.currentTimeMillis();
        
        switch (gameState) {
            case MENU:
                handleMenuKeys(e, currentTime);
                break;
            case PLAYING:
                handleGameKeys(e);
                break;
            case GAME_OVER:
                handleGameOverKeys(e, currentTime);
                break;
        }
    }
    
    private void handleMenuKeys(KeyEvent e, long currentTime) {
        // Add cooldown to prevent too fast changes
        if (currentTime - lastKeyPressTime < KEY_COOLDOWN) {
            return;
        }
        
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
                if (winningScore < 15) {
                    winningScore++;
                    lastKeyPressTime = currentTime;
                }
                break;
            case KeyEvent.VK_DOWN:
                if (winningScore > 1) {
                    winningScore--;
                    lastKeyPressTime = currentTime;
                }
                break;
            case KeyEvent.VK_ENTER:
                resetGame();
                break;
        }
    }
    
    private void handleGameKeys(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W -> wPressed = true;
            case KeyEvent.VK_S -> sPressed = true;
            case KeyEvent.VK_UP -> upPressed = true;
            case KeyEvent.VK_DOWN -> downPressed = true;
            case KeyEvent.VK_ESCAPE -> returnToMenu();
        }
    }
    
    private void handleGameOverKeys(KeyEvent e, long currentTime) {
        if (currentTime - lastKeyPressTime < KEY_COOLDOWN) {
            return;
        }
        
        switch (e.getKeyCode()) {
            case KeyEvent.VK_ENTER:
                resetGame();
                lastKeyPressTime = currentTime;
                break;
            case KeyEvent.VK_ESCAPE:
                returnToMenu();
                lastKeyPressTime = currentTime;
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_W -> wPressed = false;
            case KeyEvent.VK_S -> sPressed = false;
            case KeyEvent.VK_UP -> upPressed = false;
            case KeyEvent.VK_DOWN -> downPressed = false;
            case KeyEvent.VK_ENTER -> enterPressed = false;
            case KeyEvent.VK_ESCAPE -> escPressed = false;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        JFrame frame = new JFrame("Ping Pong");
        PingPong game = new PingPong();
        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        game.requestFocusInWindow();
    }
}