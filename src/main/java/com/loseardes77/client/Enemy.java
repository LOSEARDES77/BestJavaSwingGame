package com.loseardes77.client;


import com.loseardes77.common.Direction;

import javax.swing.JButton;
import java.awt.Color;
import java.awt.Rectangle;

public class Enemy extends JButton {
	/**
	 * The game instamce the enemy will be acting in
	 */
    private Game game = null;

    //  Movement
	/**
	 * The direction in which to move the enemy [0] is Right or left and [1] is Up and down
	 */
    private final boolean[] axis;

	/**
	 * Increments each time a movement is tried
	 */
    private int counter = 0;

	/**
	 * Is a random interval of movement tries before changing the heading
	 */
    private int change = 0;

	/**
	 * Creates a new enemy 
	 * @param gameInstance the {@link Game} the enemy will be acting in
	 * @param bounds the bounds of the {@link Enemy}
	 */
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

	/**
	 * The enemy movement code
	 *
	 */
    protected void move() {
		/*
		  The enemy movement ai is ruled by two variables:
		    Couter - Increments each time a movement is tried
		    Change - Is a random interval of movement tries before changing the heading
		
		  Each time a movement is tried counter is incremented and tested against change
		  If it is, Counter gets reset to 0 and change gets set to a random integer and a new
		  heading is determinated by seting axis[0] and axis[1] to a random boolean
		    Axis[0] - Right or left
			Axis[1] - Up or down

		  Then we try to move the enemy using the values of axis, if we can't we invert them
		*/
		
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

	/**
	 * Tries to move the {@link Enemy} in a {@link Direction}
	 * @param d a {@link Direction}
	 * @returns {@code true} if the movement was UNSUCCESSFUL or {@code false} if it was SUCCESSFUL
	 */
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
        if (game.checkCollision(r, this, game.getSelfPlayer()))
            return true;		// FIXME Why return true when it wasn't successful

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
