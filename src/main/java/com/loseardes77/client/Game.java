package com.loseardes77.client;

import com.loseardes77.common.ThreadPool;
import com.loseardes77.common.Wall;
import com.loseardes77.db.Score;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.loseardes77.common.Logger.error;
import static com.loseardes77.common.Logger.info;
import static com.loseardes77.common.Logger.warning;

public class Game extends JPanel {

    private static final JLabel dummy = new JLabel("");

	/**
	 * When to exit threded code
	 */
    protected static boolean exitThreads = true;
    private final int[] windowDimensions = {1900, 1060}; //  FIXME hard coded res
    private final List<Component> Objects = new CopyOnWriteArrayList<>();
    private final List<Enemy> enemies = new CopyOnWriteArrayList<>();
    private final List<Coin> coins = new CopyOnWriteArrayList<>();
    private final Random random;

	/**
	 * The {@link Player} that controls the game
	 */
    private Player selfPlayer;
    private final JFrame frame;
    private final JLabel healthLabel;
    private final ThreadPool pool;
    private final JLabel scoreLabel;

	/**
	 * Speed at which to spawn coins (in ms)
	 *
	 */
    private static final int COIN_GENERATION_SPEED = 500;

    // Multiplayer
    private final HashMap<Color, Player> players = new HashMap<>();
    private final boolean clientEnemies;
    private final boolean clientCoins;
    private boolean hasMatchStarted = false;

    public Game(JFrame frame, boolean genCoins, boolean genEnemies, boolean genWalls) {
        this.frame = frame;
        setLayout(new BorderLayout());
        setBounds(0, 0, windowDimensions[0], windowDimensions[1]);

        this.healthLabel = new JLabel("100 HP"); // FIXME hard coded max hp
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


        /* Doesn't work rn
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
        */


        add(dummy, BorderLayout.CENTER);
        if (genEnemies || genWalls || genCoins) {
            int seed = (int) (Math.random() * 999999) + 1;
            info("Using seed \"" + seed + "\"");
            random = new Random(seed);
        } else {
            random = null; // Runs on the server
        }
        if (genWalls)
            buildWalls();
        if (genEnemies)
            addEnemies(15);
        if (genCoins)
            addCoins();

        clientEnemies = genEnemies;
        clientCoins = genCoins;

        // Tested with 4 threads and worked fine
        pool = new ThreadPool(Runtime.getRuntime().availableProcessors());
    }

    public Game(JFrame frame) {
        this(frame, true, true, true);
    }

	/**
	 * Generates 5 coins in random position
	 */
    private void addCoins() {
        for (int i = 0; i < 5; i++) { // FIXME Hard coded coin spawn count 
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

	/**
	 * Adds a player to the game
	 * @param player the {@link Player} to add
	 * @param isSelf if the added player is the one that is controlling the game
	 */
    public void addPlayer(Player player, boolean isSelf) {
        if (isSelf) {
            this.selfPlayer = player;
        }
        this.players.put(player.getColor(), player);
    }

	/**
	 * Adds a player to the game
	 * @param player the {@link Player} to add
	 */	
    public void addPlayer(Player player) {
        this.selfPlayer = player;
        addObject(player, genRandomPosition(50));
    }

	/**
	 * Gets a random position inside the gamefield of a specified size
	 * @param size the size of the requested random position
	 * @return a rectangle of the given size that is in a random position
	 */
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


    public Player getSelfPlayer() {
        return selfPlayer;
    }

	/**
	 * Gets a player from a color (used as an uuid)
	 * @param c the {@link Color} of the player to search
	 * @return {@code null} if the player isn't found, a {@link Player} if it is
	 */
    public Player getPlayer(Color c) {
        return players.get(c);
    }

	/**
	 * Check if a player of a color (used as an uuid) exists in the game
	 * @param c the {@link Color} of the player to search
	 * @return {@code true} if the player was found, {@code false} if it wasn't
	 */
    public boolean hasPlayer(Color c) {
        return players.containsKey(c);
    }

    public Random getRandom() {
        return random;
    }

    public ThreadPool getPool() {
        return pool;
    }

	/**
	 * Checks if a rectangle doesn't collide with itself or the player
	 * @param r the {@link Rectangle} to check for collision
	 * @param self the {@link Component} that is checking for collision
	 * @param player the {@link Player} to check collisuon against
	 * @return {@code true} if they collide, {@code false} if they don't
	 */
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

	/**
	 * Checks for collision between a rectangle and a component  
	 * @param r the {@link Rectangle} to test for collision
	 * @param self a {@link Component} to test for collision
	 * @return {@code true} if they collide, {@code false} if they don't
	 */
    public boolean checkCollision(Rectangle r, Component self) {
        return checkCollision(r, self, null);
    }

	
    /**
	 * Checks for the collision between a rectangle and enemies 
     * @param r the {@link Rectangle} to check collision in
     * @param self a component to spare from the check
     * @return {@code true} if they collide, {@code false} if they don't
     */
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
	
    /**
     * Starts the game, starting moving the player, the enemies and generating coins
     */
    public void startGame() {
        this.hasMatchStarted = true;
        Game.exitThreads = false;
        setVisible(true);
        selfPlayer.startMovingPLayer();
        if (clientEnemies)
            setupEnemyMovementThread();
        if (clientCoins)
            startCoinGeneration();

    }

    public boolean hasMatchStarted() {
        return hasMatchStarted;
    }

    private void startCoinGeneration() {
        Thread coinGen = new Thread(() -> {
            while (!Game.exitThreads) {
                if (coins.size() < 35) // Test that there are no more than 35 coins
					
					// generates a new coin in a random location
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

    /**
     * For some freak reason this func does the checks for player-coin collision
     */
    private Thread coinGenThread() {
        Thread coinCol = new Thread(() -> {
            while (!Game.exitThreads) {
                Rectangle playerHitBox = getSelfPlayer().getBounds();
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

	/**
	 * Updates the player coin counter
	 */
    private void updateCoins() { //  MAYBE This shouldn't be in the game class
        getSelfPlayer().addCoin();
        scoreLabel.setText(getSelfPlayer().getCoinsCount() + " Coins");
        scoreLabel.repaint();

        FontMetrics scoreLabelMetrics = scoreLabel.getFontMetrics(scoreLabel.getFont());
        Dimension scoreLabelSize = new Dimension(scoreLabelMetrics.stringWidth(scoreLabel.getText()), scoreLabelMetrics.getHeight());
        Point scoreLabelLocation = new Point((int) ((getWidth() / 2.0) - (scoreLabelSize.getWidth() / 2.0)), getHeight() - scoreLabelMetrics.getHeight() - 80);
        scoreLabel.setBounds(new Rectangle(scoreLabelLocation, scoreLabelSize));

    }

    public void buildWalls() {
        info("Building walls");
        Wall[] ws = new Wall[]{ //  FIXME Hard coded walls
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
        /* API Down
        s.setName(selfPlayer.getName());
        s.setScore(selfPlayer.getCoinsCount());
        s.sendScore();
        */
        pool.join();
        if (wonGame) { // TODO: Determinate when the player wins
            JOptionPane.showMessageDialog(this, "You won!", "Game Over", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "\tYou lost!\nYou collected " + getSelfPlayer().getCoinsCount() + " coins", "Game Over", JOptionPane.INFORMATION_MESSAGE);
        }
        info("Game Over");
        this.frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
    }

    public void setupEnemyMovementThread() {
        final long ENEMY_MOVE_DELAY = 30; // In mills
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            long startTime = System.currentTimeMillis();
            boolean endGame = false;
            Rectangle playerHitBox = getSelfPlayer().getBounds();
            for (Enemy e : enemies) {
                e.move();
                if (playerHitBox.intersects(e.getBounds())) {
                    byte health = (byte) (getSelfPlayer().getHealth() - 10);
                    getSelfPlayer().setHealth(health);
                    updateHealthLabel(health);
                    e.swapLocation();
                    if (health <= 0) {
                        endGame = true;
                    }
                }
            }
            if (endGame) {
                scheduler.shutdown();
                end(false);
            }
            long elapsedTime = System.currentTimeMillis() - startTime;
            if (elapsedTime > ENEMY_MOVE_DELAY + 10) {
                warning("Enemy movement took too long (" + (elapsedTime - ENEMY_MOVE_DELAY) + "ms more)");
            }
        }, 0, ENEMY_MOVE_DELAY, TimeUnit.MILLISECONDS);
    }
}
