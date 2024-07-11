package com.loseardes77.client;

import com.loseardes77.common.ThreadPool;
import com.loseardes77.common.Wall;
import com.loseardes77.db.HighScoreTable;
import com.loseardes77.db.Score;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.loseardes77.common.Logger.error;
import static com.loseardes77.common.Logger.info;
import static com.loseardes77.common.Logger.warning;

public class Game extends JPanel {

    private static final JLabel dummy = new JLabel("");
    protected static boolean exitThreads = true;
    private final int[] windowDimensions = {1900, 1060};
    private final List<Component> Objects = new CopyOnWriteArrayList<>();
    private final List<Enemy> enemies = new CopyOnWriteArrayList<>();
    private final List<Coin> coins = new CopyOnWriteArrayList<>();
    private final Random random;
    private Player player;
    private final SinglePlayer frame;
    private final JLabel healthLabel;
    private final ThreadPool pool;
    private final JLabel scoreLabel;
    private static final int COIN_GENERATION_SPEED = 500;


    public Game(SinglePlayer frame) {
        this.frame = frame;
        setLayout(new BorderLayout());
        setBounds(0, 0, windowDimensions[0], windowDimensions[1]);

        this.healthLabel = new JLabel("100 HP");
        healthLabel.setFont(new Font("Arial", Font.BOLD, 20));
        FontMetrics healthLabelMetrics = healthLabel.getFontMetrics(healthLabel.getFont());
        Dimension healthLabelSize = new Dimension(healthLabelMetrics.stringWidth(healthLabel.getText()), healthLabelMetrics.getHeight());
        Point healthLabelLocation = new Point((int) ((getWidth() / 2.0) - (healthLabelSize.getWidth() / 2.0)), getHeight() - healthLabelMetrics.getHeight() - 50);
        add(healthLabel);
        healthLabel.setBounds(new Rectangle(healthLabelLocation, healthLabelSize));

        this.scoreLabel = new JLabel("0 Coins");
        scoreLabel.setFont(new Font("Arial", Font.BOLD, 20));
        FontMetrics scoreLabelMetrics = scoreLabel.getFontMetrics(scoreLabel.getFont());
        Dimension scoreLabelSize = new Dimension(scoreLabelMetrics.stringWidth(scoreLabel.getText()), scoreLabelMetrics.getHeight());
        Point scoreLabelLocation = new Point((int) ((getWidth() / 2.0) - (scoreLabelSize.getWidth() / 2.0)), getHeight() - scoreLabelMetrics.getHeight() - 80);
        add(scoreLabel);
        scoreLabel.setBounds(new Rectangle(scoreLabelLocation, scoreLabelSize));


        DefaultTableModel model = HighScoreTable.getHighScoreTableModel();
        if (model != null) {
            JTable highScore = new JTable(model);
            highScore.setSelectionBackground(getBackground());
            highScore.setBackground(getBackground());
            add(highScore);
            highScore.setBounds(windowDimensions[0] - 175, 25, 150, 250);
        } else {
            error("Couldn't connect to the database");
        }


        add(dummy, BorderLayout.CENTER);
        int seed = (int) (Math.random() * 999999) + 1;
        info("Using seed \"" + seed + "\"");
        random = new Random(seed);
        buildWalls();
        addEnemies(15);
        addCoins();

        // Tested with 4 threads and worked fine
        pool = new ThreadPool(Runtime.getRuntime().availableProcessors());
    }

    private void addCoins() {
        for (int i = 0; i < 5; i++) {
            Rectangle pos;
            do {
                pos = new Rectangle(
                        getRandom().nextInt(getWidth() - 20),
                        getRandom().nextInt(getHeight() - 20),
                        20,
                        20
                );
            } while (checkCollision(pos, null));

            coins.add(new Coin(pos));
            add(coins.get(i));
        }
    }

    public void addPlayer(Player player) {
        this.player = player;
        addObject(player, genRandomPosition(50));
    }

    public Rectangle genRandomPosition(int size) {
        Rectangle r;
        do {
            r = new Rectangle(
                    random.nextInt(windowDimensions[0] - (size / 2)),
                    random.nextInt(windowDimensions[1] - (size / 2)),
                    size,
                    size);
        } while (checkCollision(r, null));
        return r;
    }


    public Player getPlayer() {
        return player;
    }

    public Random getRandom() {
        return random;
    }

    public ThreadPool getPool() {
        return pool;
    }

    public boolean checkCollision(Rectangle r, Component self, Player player) {
        if (r.getX() < 0 || r.getY() < 0 || r.getX() + r.getWidth() > windowDimensions[0] || r.getY() + r.getHeight() > windowDimensions[1])
            return true;

        for (Component comp : Objects) {
            Rectangle rect = comp.getBounds();
            if (!comp.equals(self))
                if (!comp.equals(player))
                    if (r.intersects(rect))
                        return true;
        }

        return false;
    }

    public boolean checkCollision(Rectangle r, Component self) {
        return checkCollision(r, self, null);
    }

    public boolean checkCollisionWithEnemies(Rectangle r, Component self) {
        for (Enemy e : enemies) {
            if (!e.equals(self) && r.intersects(e.getBounds())) {
                return true;
            }
        }
        return false;
    }

    public void addObject(Component comp, Rectangle bounds) {
        addObjectWithoutCollision(comp, bounds);
        Objects.add(comp);

    }

    public void addObjectWithoutCollision(Component comp, Rectangle bounds) {
        remove(dummy);
        add(comp);
        comp.setBounds(bounds);
        add(dummy, BorderLayout.CENTER);
        repaint();
        info("Added " + comp.getClass().getSimpleName() + " at " + bounds.x + ", " + bounds.y + " with size " + bounds.width + "x" + bounds.height);
    }

    public void startGame() {
        Game.exitThreads = false;
        setVisible(true);
        player.startMovingPLayer();
        setupEnemyMovementThread();
        startCoinGeneration();

    }

    private void startCoinGeneration() {
        Thread coinGen = new Thread(() -> {
            while (!Game.exitThreads) {
                if (coins.size() < 35)
                    pool.execute(() -> {
                        if (getRandom().nextFloat() > 0.8) {
                            Rectangle pos;
                            do {
                                pos = new Rectangle(
                                        getRandom().nextInt(getWidth() - 80),
                                        getRandom().nextInt(getHeight() - 80),
                                        20,
                                        20
                                );
                            } while (checkCollision(pos, null));
                            Coin c = new Coin(pos);
                            coins.add(c);
                            remove(dummy);
                            add(c);
                            c.setBounds(pos);
                            add(dummy, BorderLayout.CENTER);
                        }
                    });
                try {
                    Thread.sleep(COIN_GENERATION_SPEED);
                } catch (InterruptedException e) {
                    error("Stopping coin generation thread");
                    Thread.currentThread().interrupt();
                }
            }
            info("Stopping coin generation thread");
        });

        coinGen.setPriority(2);
        coinGen.start();

        Thread coinCol = coinGenThread();
        coinCol.start();

    }

    private Thread coinGenThread() {
        Thread coinCol = new Thread(() -> {
            while (!Game.exitThreads) {
                Rectangle playerHitBox = getPlayer().getBounds();
                for (Coin c : coins) {
                    if (c.getBounds().intersects(playerHitBox)) {
                        updateCoins();
                        remove(c);
                        coins.remove(c);
                        c.setText("");
                        c.repaint();
                    }
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    error("Stopping coin collision check thread");
                    Thread.currentThread().interrupt();
                }
            }
            info("Stopping coin collision check thread");
        });
        coinCol.setPriority(7);
        return coinCol;
    }

    private void updateCoins() {
        getPlayer().addCoin();
        scoreLabel.setText(getPlayer().getCoinsCount() + " Coins");
        scoreLabel.repaint();

        FontMetrics scoreLabelMetrics = scoreLabel.getFontMetrics(scoreLabel.getFont());
        Dimension scoreLabelSize = new Dimension(scoreLabelMetrics.stringWidth(scoreLabel.getText()), scoreLabelMetrics.getHeight());
        Point scoreLabelLocation = new Point((int) ((getWidth() / 2.0) - (scoreLabelSize.getWidth() / 2.0)), getHeight() - scoreLabelMetrics.getHeight() - 80);
        scoreLabel.setBounds(new Rectangle(scoreLabelLocation, scoreLabelSize));

    }

    public void buildWalls() {
        info("Building walls");
        Wall[] ws = new Wall[]{
                new Wall(100, 100, 100, 100),
                new Wall(100, getHeight() - 200, 100, 100),
                new Wall(getWidth() - 500, 100, 100, 100),
                new Wall(getWidth() - 500, getHeight() - 200, 100, 100),
                new Wall(getWidth() / 2 - 50, getHeight() / 2 - 50, 100, 100),
        };

        for (Wall w : ws)
            addObject(w, w.getBounds());
    }

    public void addEnemies(int amount) {
        List<Rectangle> enemies = new CopyOnWriteArrayList<>();
        for (int i = 0; i < amount; i++) {
            Rectangle bounds = genBounds(enemies);
            enemies.add(bounds);
            addEnemy(new Enemy(this, bounds), bounds);
        }
    }

    public Rectangle genBounds(List<Rectangle> rectsToAvoid) {
        Rectangle bounds = genRandomPosition(50);
        for (Rectangle r : rectsToAvoid) {
            if (r.intersects(bounds)) {
                return genBounds(rectsToAvoid);
            }
        }
        return bounds;
    }

    private void addEnemy(Enemy enemy, Rectangle rectangle) {
        enemies.add(enemy);
        remove(dummy);
        add(enemy);
        enemy.setBounds(rectangle);
        add(dummy, BorderLayout.CENTER);
        repaint();
    }

    public void updateHealthLabel(byte val) {
        healthLabel.setText(String.valueOf(val) + " HP");
    }

    public void end(boolean wonGame) {
        Game.exitThreads = true;
        Score s = new Score();
        s.setName(player.getName());
        s.setScore(player.getCoinsCount());
        s.sendScore();
        pool.join();
        if (wonGame) { // TODO: Determinate when the player wins
            JOptionPane.showMessageDialog(this, "You won!", "Game Over", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "\tYou lost!\nYou collected " + getPlayer().getCoinsCount() + " coins", "Game Over", JOptionPane.INFORMATION_MESSAGE);
        }
        info("Game Over");
        this.frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
    }

    public void setupEnemyMovementThread() {
        //  TODO: Optimize this
        new Thread(() -> {
            final long ENEMY_MOVE_DELAY = 30; // In mills 25ms)
            while (!Game.exitThreads) {
                long startTime = System.currentTimeMillis();
                AtomicBoolean endGame = new AtomicBoolean(false);
                for (Enemy e : enemies) {
                    pool.execute(() -> {
                        e.move();
                        Rectangle playerHitBox = getPlayer().getBounds();
                        if (playerHitBox.intersects(e.getBounds())) {
                            byte health = (byte) (getPlayer().getHealth() - 10);
                            getPlayer().setHealth(health);
                            updateHealthLabel(health);
                            e.swapLocation();
                            if (health <= 0) {
                                endGame.set(true);
                            }
                        }
                    });

                }
                pool.join();
                if (endGame.get()) {
                    end(false);
                }
                long elapsedTime = System.currentTimeMillis() - startTime;

                if (elapsedTime < ENEMY_MOVE_DELAY) {
                    try {
                        Thread.sleep(ENEMY_MOVE_DELAY - elapsedTime);
                    } catch (InterruptedException _) {
                        Thread.currentThread().interrupt();
                    }
                }

                if (elapsedTime > ENEMY_MOVE_DELAY + 10) {
                    warning("Enemy movement took too long (" + (elapsedTime - ENEMY_MOVE_DELAY) + "ms more)");
                }
            }
            info("Stopping enemy movement Thread");
        }).start();
    }
}
