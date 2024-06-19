package com.loseardes77.client;

import com.loseardes77.common.Direction;
import javax.swing.JButton;

import java.awt.Color;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;

public class Player extends JButton {

    private final int sleepTime = 10;
    private final int speed = 5;
    private final boolean[] inputMap = new boolean[4]; // w, a, s, d
    private final Game game;
    private boolean exitThreads = false;
    private Thread inputThread;
    private KeyEventDispatcher keyEventDispatcher;
    private byte health = 100;

    private boolean isLightColor(Color color) {
        float[] hsb = new float[3];
        Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsb);
        return hsb[2] > 0.5;
    }

    public Player(boolean inputs, Game game, Color playerColor){
        if (inputs)
            startInputDetection();

        if (isLightColor(playerColor))
            setForeground(Color.BLACK);
        else
            setForeground(Color.WHITE);

        this.game = game;
        setFocusable(false);
        setBackground(playerColor);
        setText("😊");
    }

    public byte getHealth(){
        return health;
    }

    public void setHealth(byte health){
        this.health = health;
    }

	/**
	 * Moves the player to a new position
	 * @param x 
	 * @param y
	 * @return {@code true} if the player moved sucessfuly to the new position
	 */
	public boolean teleport(int x, int y) {
		if (game.checkCollision(new Rectangle(x, y, getWidth(), getHeight()), this)) {
			return false;
		} else {
			setLocation(x, y);
			return true;
		}
	}

    public void movePlayer(int dX, int dY){
        int x = getX();
        int y = getY();
		
		teleport(x + dX, y + dY);
    }

    public double movementAngle() {
        int dX = 0;
        int dY = 0;

        if (inputMap[0]) dY--; // W
        if (inputMap[2]) dY++; // S
        if (inputMap[1]) dX--; // A
        if (inputMap[3]) dX++; // D

        if (dX == 0 && dY == 0) {
            return -1; // No movement
        }

        return Math.atan2(dY, dX);
    }

    public void stopInputDetection(){
        KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(keyEventDispatcher);
    }

    public void startInputDetection(){
        keyEventDispatcher = (e) -> {
            if (e.getID() == KeyEvent.KEY_PRESSED) {
                if (e.getKeyCode() == KeyEvent.VK_W)
                    inputMap[0] = true;

                if (e.getKeyCode() == KeyEvent.VK_S)
                    inputMap[2] = true;

                if (e.getKeyCode() == KeyEvent.VK_A)
                    inputMap[1] = true;

                if (e.getKeyCode() == KeyEvent.VK_D)
                    inputMap[3] = true;


            }
            if (e.getID() == KeyEvent.KEY_RELEASED) {
                if (e.getKeyCode() == KeyEvent.VK_W)
                    inputMap[0] = false;

                if (e.getKeyCode() == KeyEvent.VK_S)
                    inputMap[2] = false;

                if (e.getKeyCode() == KeyEvent.VK_A)
                    inputMap[1] = false;

                if (e.getKeyCode() == KeyEvent.VK_D)
                    inputMap[3] = false;
            }
            return false;
        };
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(keyEventDispatcher);
    }

    public void destroy(){
        stopInputDetection();
        exitThreads = true;
        try {
            inputThread.join();
        } catch (InterruptedException e) {
            inputThread.interrupt();
        }
    }

    public void startMovingPLayer(){
       inputThread = new Thread(() -> {
            while (!exitThreads) {

				double theta = movementAngle();

				// NEXT Now when trying to move into a wall and to the side, both movements will be canceled instead of just the one that isn't posible

				if (theta != -1) {
					double sX = speed * Math.cos(theta);
					double sY = speed * Math.sin(theta);

					movePlayer((int) sX, (int) sY);
				}

                try{
                    Thread.sleep(sleepTime); //  MAYBE Use delta time to figure out when to check the input
                } catch (InterruptedException _) {

                }
            }

        });

        inputThread.start();
    }

}
