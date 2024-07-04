package com.loseardes77.client;

import com.loseardes77.common.Wall;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.loseardes77.common.Logger.info;

public class Game extends JPanel {

    private static final JLabel dummy = new JLabel("");
    protected static boolean exitThreads = false;
    private final int[] windowDimensions = {1900, 1060};
    private final ArrayList<Component> Objects = new ArrayList<>();
    private final ArrayList<Enemy> enemies = new ArrayList<>();
    private final Random random;
    private Player player;
    private final SinglePlayer frame;
    private final JLabel healthLabel;


    public Game(SinglePlayer frame, JLabel healthLabel) {
        this.healthLabel = healthLabel;
        this.frame = frame;
        setLayout(new BorderLayout());
        setBounds(0, 0, windowDimensions[0], windowDimensions[1]);
        add(dummy, BorderLayout.CENTER);
        int seed = (int) (Math.random() * 999999) + 1;
        info("Using seed \"" + seed + "\"");
        random = new Random(seed);
        buildWalls();
        addEnemies(10);
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
        setVisible(true);
        for (Enemy e : enemies)
            e.startMove();

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
        ArrayList<Rectangle> enemies = new ArrayList<>();
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
        if (wonGame) { // TODO: Determinate when the player wins
            JOptionPane.showMessageDialog(this, "You won!", "Game Over", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "You lost!", "Game Over", JOptionPane.INFORMATION_MESSAGE);
        }
        info("Game Over");

        this.frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
    }
}
