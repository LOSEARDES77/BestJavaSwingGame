package com.loseardes77.client;


import com.loseardes77.common.Direction;

import javax.swing.JButton;
import java.awt.Color;
import java.awt.Rectangle;

import static com.loseardes77.common.Logger.warning;

public class Enemy extends JButton {
    private Game game = null;
    private final long moveDelay = 16; // In mills (16ms)
    private final int speed = 2;
    private Thread moveThread;

    public Enemy(Game gameInstance, Rectangle bounds) {
        if (game == null) {
            game = gameInstance;
        }
        setText("•_•");
        setBounds(bounds);
        setBackground(new Color(255, 50, 50));
        setForeground(new Color(0, 0, 0));
        setSize(50, 50);
    }

    private Rectangle genRandomPosition() {
        int size = 50;
        Rectangle r;
        do {
            r = new Rectangle(
                    game.getRandom().nextInt(game.getWidth() - (size * 2)),
                    game.getRandom().nextInt(game.getHeight() - (size * 2)),
                    size,
                    size);
        } while (game.checkCollision(r, this) || game.checkCollisionWithEnemies(r, this));
        return r;
    }

    public void startMove() {
        moveThread = new Thread(() -> {
            boolean axis = game.getRandom().nextBoolean();
            boolean direction = game.getRandom().nextBoolean();

            int counter = 0;
            int change = 0;
            boolean result;
            while (!Game.exitThreads) {
                long startTime = System.currentTimeMillis();

                if (counter++ > change) {
                    axis = game.getRandom().nextBoolean();
                    direction = game.getRandom().nextBoolean();
                    counter = 0;
                    change = game.getRandom().nextInt(500) + 100;
                }
                if (axis) {
                    if (direction) {
                        if (getX() + getWidth() + 15 > game.getWidth())
                            direction = false;
                        result = moveEnemy(Direction.RIGHT);
                    } else {
                        if (getX() - 15 < 0)
                            direction = true;
                        result = moveEnemy(Direction.LEFT);
                    }
                } else {
                    if (direction) {
                        if (getY() + getHeight() + 15 > game.getHeight())
                            direction = false;
                        result = moveEnemy(Direction.DOWN);
                    } else {
                        if (getY() - 15 < 0)
                            direction = true;
                        result = moveEnemy(Direction.UP);
                    }
                }
                if (result) {
                    direction = !direction;
                    axis = !axis;
                }

                if (game.getPlayer().getBounds().intersects(getBounds())) {
                    byte health = (byte) (game.getPlayer().getHealth() - 10);
                    game.getPlayer().setHealth(health);
                    game.updateHealthLabel(health);
                    setLocation(genRandomPosition().getLocation());
                    direction = game.getRandom().nextBoolean();
                    axis = game.getRandom().nextBoolean();
                    if (health <= 0) {
                        game.end(false);
                        break;
                    }
                }

                long elapsedTime = System.currentTimeMillis() - startTime;

                if (elapsedTime < moveDelay) {
                    try {
                        Thread.sleep(moveDelay - elapsedTime);
                    } catch (InterruptedException _) {
                        Thread.currentThread().interrupt();
                    }
                }

                if (elapsedTime > moveDelay) {
                    warning("Enemy movement took too long (" + (elapsedTime - moveDelay) + "ms more)");
                }
            }
        });
        moveThread.start();
    }

    private boolean moveEnemy(Direction d) {
        int original_x = getX();
        int original_y = getY();
        int x = original_x;
        int y = original_y;
        switch (d) {
            case UP:
                y -= speed;
                break;
            case DOWN:
                y += speed;
                break;
            case LEFT:
                x -= speed;
                break;
            case RIGHT:
                x += speed;
                break;
        }
        Rectangle r = new Rectangle(x, y, getWidth(), getHeight());
        if (game.checkCollision(r, this, game.getPlayer()))
            return true;

        if (game.checkCollisionWithEnemies(r, this))
            return true;

        setLocation(x, y);

        if (game.checkCollision(r, this, game.getPlayer())) {
            setLocation(original_x, original_y);
            return true;
        }

        if (game.checkCollisionWithEnemies(r, this)) {
            setLocation(original_x, original_y);
            return true;
        }

        return false;
    }

}
