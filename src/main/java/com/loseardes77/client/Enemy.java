package com.loseardes77.client;


import com.loseardes77.common.Direction;

import javax.swing.JButton;
import java.awt.Color;
import java.awt.Rectangle;

public class Enemy extends JButton {
    private Game game = null;

    //  Movement
    private final boolean[] axis;

    private int counter = 0;
    private int change = 0;

    public Enemy(Game gameInstance, Rectangle bounds) {

        if (game == null) {
            game = gameInstance;
        }

        this.axis = new boolean[]{game.getRandom().nextBoolean(), game.getRandom().nextBoolean()};

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

    protected void move() {
        if (++counter > change) {
            axis[0] = game.getRandom().nextBoolean();
            axis[1] = game.getRandom().nextBoolean();
            counter = 0;
            change = game.getRandom().nextInt(500) + 100;
        }

        boolean result;

        if (axis[0]) {
            if (axis[1]) {
                if (getX() + getWidth() + 15 > game.getWidth())
                    axis[1] = false;
                result = moveEnemy(Direction.RIGHT);
            } else {
                if (getX() - 15 < 0)
                    axis[1] = true;
                result = moveEnemy(Direction.LEFT);
            }
        } else {
            if (axis[1]) {
                if (getY() + getHeight() + 15 > game.getHeight())
                    axis[1] = false;
                result = moveEnemy(Direction.DOWN);
            } else {
                if (getY() - 15 < 0)
                    axis[1] = true;
                result = moveEnemy(Direction.UP);
            }
        }
        if (result) {
            axis[1] = !axis[1];
            axis[0] = !axis[0];
        }
    }

    private boolean moveEnemy(Direction d) {

        int speed = 3;

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


        return false;
    }

    public void swapLocation() {
        setLocation(genRandomPosition().getLocation());
        axis[1] = game.getRandom().nextBoolean();
        axis[0] = game.getRandom().nextBoolean();
    }
}
