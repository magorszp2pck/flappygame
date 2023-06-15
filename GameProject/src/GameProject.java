import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.stream.Stream;
import java.util.stream.IntStream;

public class GameProject extends JPanel implements ActionListener, KeyListener, MouseListener {
    private static final int WIDTH = 600;
    private static final int HEIGHT = 800;
    private static final int PIPE_WIDTH = 80;
    private static final int PIPE_GAP = 200;
    private Clip backgroundMusic;

    private Bird bird;
    private ArrayList<Pipe> pipes;
    private int score;
    private int highScore;
    private boolean gameStarted;
    private Timer timer;
    

    private enum View {
        MENU,
        GAME
    }

    private View currentView;

    public GameProject() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.CYAN);
        addKeyListener(this);
        addMouseListener(this);
        setFocusable(true);
        setFocusTraversalKeysEnabled(false);

        bird = new Bird();
        pipes = new ArrayList<Pipe>();
        score = 0;
        highScore = 0;
        gameStarted = false;
        timer = new Timer(20, this);

        currentView = View.MENU;
    }
    
    private void saveScores() {
        try {
            String fileName = "scores.txt";
            String filePath = System.getProperty("user.dir") + File.separator + fileName;
            FileWriter writer = new FileWriter(filePath);
            writer.write(String.valueOf(highScore));
            writer.close();
        } catch (IOException e) {
            System.out.println("Error saving scores: " + e.getMessage());
        }
    }

    private void loadScores() {
        try {
            String fileName = "scores.txt";
            String filePath = System.getProperty("user.dir") + File.separator + fileName;
            FileReader reader = new FileReader(filePath);
            BufferedReader bufferedReader = new BufferedReader(reader);
            String line = bufferedReader.readLine();
            if (line != null) {
                highScore = Integer.parseInt(line);
            }
            bufferedReader.close();
        } catch (IOException e) {
            System.out.println("Error loading scores: " + e.getMessage());
        }
    }

    private Stream<Integer> loadHighScores() {
        try {
            String fileName = "scores.txt";
            String filePath = System.getProperty("user.dir") + File.separator + fileName;
            FileReader reader = new FileReader(filePath);
            BufferedReader bufferedReader = new BufferedReader(reader);
            Stream<String> lines = bufferedReader.lines();
            return lines.map(Integer::parseInt);
        } catch (IOException e) {
            System.out.println("Error loading high scores: " + e.getMessage());
        }
        return Stream.empty();
    }

    private void playMusic() {
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new File("audio.mp3").getAbsoluteFile());
            backgroundMusic = AudioSystem.getClip();
            backgroundMusic.open(audioInputStream);
            backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (Exception ex) {
            System.out.println("Error playing music");
            ex.printStackTrace();
        }
    }

    private void stopMusic() {
        if (backgroundMusic != null && backgroundMusic.isRunning()) {
            backgroundMusic.stop();
        }
    }

    public void startGame() {
        bird.reset();
        pipes.clear();
        pipes.add(new Pipe(WIDTH + PIPE_WIDTH));
        score = 0;
        gameStarted = true;
        stopMusic();
        playMusic();
        loadScores();
        timer.start();
    }
    public void actionPerformed(ActionEvent e) {
        if (gameStarted) {
            bird.move();
            for (Pipe pipe : pipes) {
                pipe.move(5);
                if (bird.collidesWith(pipe)) {
                    gameOver();
                    break;
                }
                if (pipe.getX() == bird.getX()) {
                    score++;
                    if (score > highScore) {
                        highScore = score;
                    }
                }
            }
            if (bird.getY() >= HEIGHT) {
                gameOver();
            }
            if (score > 0 && score % 10 == 0) {
                pipes.add(new Pipe(WIDTH));
            }
            repaint();
        }
        if (e.getSource() instanceof JMenuItem) {
            JMenuItem menuItem = (JMenuItem) e.getSource();
            if (menuItem.getText().equals("Exit")) {
                System.exit(0);
            }
        }
    }

    public class Bird {
        private static final int SIZE = 40;
        private static final int GRAVITY = 1;
        private static final int JUMP_VELOCITY = -15;

        private int x, y, velocity;
        private Color color;

        public Bird() {
            x = 100;
            y = 200;
            velocity = 0;
            color = Color.YELLOW;
        }

        public void reset() {
            x = 100;
            y = 200;
            velocity = 0;
        }

        public void move() {
            velocity += GRAVITY;
            y += velocity;
        }

        public void jump() {
            velocity = JUMP_VELOCITY;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getSize() {
            return SIZE;
        }

        public boolean collidesWith(Pipe pipe) {
            int pipeX = pipe.getX();
            int pipeHeight = pipe.getHeight();

            if (x + SIZE > pipeX && x < pipeX + PIPE_WIDTH) {
                if (y < pipeHeight || y + SIZE > pipeHeight + PIPE_GAP) {
                    return true;
                }
            }

            return false;
        }

        public void draw(Graphics g) {
            g.setColor(color);
            g.fillRect(x, y, SIZE, SIZE);
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (currentView == View.GAME) {
            bird.draw(g);
            for (Pipe pipe : pipes) {
                pipe.draw(g);
            }
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 36));
            g.drawString("Score: " + score, 10, 50);
            g.drawString("High Score: " + highScore, 10, 100);
            if (!gameStarted) {
                g.setFont(new Font("Arial", Font.BOLD, 48));
                g.drawString("Click to start", 50, HEIGHT / 2);
            }
        } else if (currentView == View.MENU) {
            g.setFont(new Font("Arial", Font.BOLD, 48));
            g.drawString("Main Menu", WIDTH / 2 - 120, HEIGHT / 2 - 24);
            g.setFont(new Font("Arial", Font.BOLD, 24));
            g.drawString("Press Space to start the game", WIDTH / 2 - 160, HEIGHT / 2 + 24);
        }
    }

    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            if (currentView == View.GAME) {
                bird.jump();
            } else if (currentView == View.MENU) {
                currentView = View.GAME;
                startGame();
            }
        }
    }

    public void keyTyped(KeyEvent e) {}

    public void keyReleased(KeyEvent e) {}

    private void gameOver() {
        timer.stop();
        gameStarted = false;
        saveScores();
        JOptionPane.showMessageDialog(this, "Game over!\nScore: " + score + "\nHigh Score: " + highScore);
        currentView = View.MENU;
    }

    public void mouseClicked(MouseEvent e) {
        if (currentView == View.MENU) {
            currentView = View.GAME;
            startGame();
        }
    }

    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}

    private class Pipe {
        private int x;
        private int height;

        public Pipe(int x) {
            this.x = x;
            this.height = (int)(Math.random() * (HEIGHT - 200)) + 100;
        }

        public void move(int speed) {
            x -= speed;
            if (x < -PIPE_WIDTH) {
                x = WIDTH;
                height = (int)(Math.random() * (HEIGHT - 200)) + 100;
            }
        }

        public int getX() {
            return x;
        }

        public int getHeight() {
            return height;
        }

        public void draw(Graphics g) {
            g.setColor(Color.GREEN);
            g.fillRect(x, 0, PIPE_WIDTH, height);
            g.fillRect(x, height + PIPE_GAP, PIPE_WIDTH, HEIGHT - height - PIPE_GAP);
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Flappy Bird");
        GameProject game = new GameProject();
        frame.setJMenuBar(game.createMenuBar());
        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("File");
        JMenuItem exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.addActionListener(this);
        fileMenu.add(exitMenuItem);
        menuBar.add(fileMenu);

        JMenu highScoresMenu = new JMenu("High Scores");
        Stream<Integer> highScoresStream = loadHighScores(); // Fájlbeolvasás streammel
        AtomicInteger index = new AtomicInteger(1);
        highScoresStream.forEach(score -> {
            JMenuItem menuItem = new JMenuItem("High Score " + index.getAndIncrement() + ": " + score);
            highScoresMenu.add(menuItem);
        });
        menuBar.add(highScoresMenu);

        return menuBar;
    }



    private void showHighScores() {
        JOptionPane.showMessageDialog(this, "High Scores:\n" +
                "Player 1: 100\n" +
                "Player 2: 90\n" +
                "Player 3: 80");
    }

    private void restartGame() {
        currentView = View.GAME;
        startGame();
    }
}
