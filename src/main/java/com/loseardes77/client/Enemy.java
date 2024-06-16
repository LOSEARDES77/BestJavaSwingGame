package com.loseardes77.client;


import com.loseardes77.common.Direction;
import static com.loseardes77.common.Logger.info;
import javax.swing.JButton;

import java.awt.Color;
import java.awt.Rectangle;

public class Enemy extends JButton {
    private final Game game;
    private final int sleepTime = 10;
    private final int speed = 2;
    private boolean exitThreads = false;
    private Thread moveThread;

    public Enemy(Game game, Rectangle bounds) {
        this.game = game;
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

    public void destroy(){
        exitThreads = true;
        try {
            moveThread.join();
        }catch (InterruptedException e) {
            moveThread.interrupt();
        }
    }

    public void startMove(){
        moveThread = new Movement();
        moveThread.start();
    }

    private class Movement extends Thread {
        private boolean axis; // true move x | false move y
        private boolean direction; // true positive | false negative

        public Movement() {
            this.axis = game.getRandom().nextBoolean();
            this.direction = game.getRandom().nextBoolean();
        }

        @Override
        public void run() {
            int counter = 0;
            int change = 0;
            boolean result;
            while (!exitThreads) {
                if (counter++ > change) {
                    this.axis = game.getRandom().nextBoolean();
                    this.direction = game.getRandom().nextBoolean();
                    counter = 0;
                    change = game.getRandom().nextInt(500) + 100;
                }
                if (axis) {
                    if (direction) {
                        if (getX() + getWidth() + 15 > game.getWidth())
                            direction = !direction;
                        result = moveEnemy(Direction.RIGHT);
                    } else {
                        if (getX() - 15 < 0)
                            direction = !direction;
                        result = moveEnemy(Direction.LEFT);
                    }
                } else {
                    if (direction) {
                        if (getY() + getHeight() + 15 > game.getHeight())
                            direction = !direction;
                        result = moveEnemy(Direction.DOWN);
                    } else {
                        if (getY() - 15 < 0)
                            direction = !direction;
                        result = moveEnemy(Direction.UP);
                    }
                }
                if (result) {
                    this.direction = !direction;
                    this.axis = !axis;
                }

                if (game.getPlayer().getBounds().intersects(getBounds())) {
                    game.getPlayer().setHealth((byte) (game.getPlayer().getHealth() - 10));
                    info("Player health: " + game.getPlayer().getHealth());
                    setLocation(genRandomPosition().getLocation());
                    this.direction = game.getRandom().nextBoolean();
                    this.axis = game.getRandom().nextBoolean();
                }
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
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
        if (game.checkCollision(r, this))
            return true;

        if (game.checkCollisionWithEnemies(r, this))
            return true;

        setLocation(x, y);

        if (game.checkCollision(r, this)) {
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
