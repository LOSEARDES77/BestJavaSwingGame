package com.loseardes77.client;

import javax.swing.JButton;
import java.awt.Color;
import java.awt.Font;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;

import static com.loseardes77.common.Logger.info;
import static com.loseardes77.common.Logger.warning;

public class Player extends JButton {
    private final int speed = 5;
    private final boolean[] inputMap = new boolean[4]; // w, a, s, d
    private final Game game;
    private KeyEventDispatcher keyEventDispatcher;
    private byte health = 100;
    private short coinsCounter = 0;
    private final long delay = 12; // In millis (12ms) tested to be decent

    private boolean isLightColor(Color color) {
        float[] hsb = new float[3];
        Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsb);
        return hsb[2] > 0.5;
    }

    public Player(boolean inputs, Game game, Color playerColor) {
        if (inputs)
            startInputDetection();

        if (isLightColor(playerColor))
            setForeground(Color.BLACK);
        else
            setForeground(Color.WHITE);

        this.game = game;
        setFocusable(false);
        setBackground(playerColor);
        setText("^_^");
        setFont(new Font("Arial", Font.PLAIN, 10));
    }

    public void addCoin() {
        coinsCounter++;
    }

    public short getCoinsCount() {
        return coinsCounter;
    }

    public byte getHealth() {
        return health;
    }

    public void setHealth(byte health) {
        this.health = health;
    }

    public boolean teleport(int x, int y) {
        if (game.checkCollision(new Rectangle(x, y, getWidth(), getHeight()), this)) {
            return false;
        } else {
            setLocation(x, y);
            return true;
        }
    }

    public void movePlayer(int dX, int dY) {
        int x = getX();
        int y = getY();

        boolean moved = teleport(x + dX, y + dY);

        if (!moved) {
            // Try moving only horizontally
            if (dX != 0 && teleport(x + dX, y)) return;

            // Try moving only vertically
            if (dY != 0) teleport(x, y + dY);
        }
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

    public void stopInputDetection() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(keyEventDispatcher);
    }

    public void startInputDetection() {
        keyEventDispatcher = (e) -> {
            if (e.getID() == KeyEvent.KEY_PRESSED || e.getID() == KeyEvent.KEY_RELEASED) {
                boolean eventType = e.getID() == KeyEvent.KEY_PRESSED;

                if (e.getKeyCode() == KeyEvent.VK_W)
                    inputMap[0] = eventType;

                if (e.getKeyCode() == KeyEvent.VK_S)
                    inputMap[2] = eventType;

                if (e.getKeyCode() == KeyEvent.VK_A)
                    inputMap[1] = eventType;

                if (e.getKeyCode() == KeyEvent.VK_D)
                    inputMap[3] = eventType;


            }
            return false;
        };
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(keyEventDispatcher);
    }

    public void startMovingPLayer() {
        Thread movement = new Thread(() -> {
            while (!Game.exitThreads) {

                long startTime = System.currentTimeMillis();
                double theta = movementAngle();

                if (theta != -1) {
                    double sX = speed * Math.cos(theta);
                    double sY = speed * Math.sin(theta);

                    movePlayer((int) sX, (int) sY);
                }


                long elapsedTime = System.currentTimeMillis() - startTime;

                if (elapsedTime < delay) {
                    try {
                        Thread.sleep(delay - elapsedTime);
                    } catch (InterruptedException _) {
                        Thread.currentThread().interrupt();
                    }
                } else if (elapsedTime > delay + 5) {
                    warning("Input took too long (" + (elapsedTime - delay) + "ms more)");
                }
            }
            info("Stopping player motion Thread");
            stopInputDetection();
        });
        movement.setPriority(Thread.MAX_PRIORITY);
        movement.start();
    }

}
