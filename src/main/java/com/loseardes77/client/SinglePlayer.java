package com.loseardes77.client;

import com.loseardes77.common.MainMenu;

import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import static com.loseardes77.common.Logger.error;
import static com.loseardes77.common.Logger.info;

public class SinglePlayer extends JFrame {

    private final MainMenu menu;

    private Game gamePanel = null;
    private Player player = null;

    public SinglePlayer(MainMenu menuFrame) {
        setSize(1900, 1060);
        setLocationRelativeTo(null);
        setResizable(false);
        setUndecorated(true);
        menu = menuFrame;
        menu.hideMenus();

        Color playerColor = JColorChooser.showDialog(null, "Select a player color", new Color(51, 153, 255));
        if (playerColor == null) {
            error("No color selected for player. Exiting...");
            return;
        }
        JLabel healthLabel = new JLabel("100 HP");
        this.gamePanel = new Game(this, healthLabel);
        this.player = new Player(true, gamePanel, playerColor);
        gamePanel.addPlayer(player);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                destroy();
                info("Closing game");
            }
        });

        healthLabel.setFont(new Font("Arial", Font.BOLD, 20));
        FontMetrics metrics = healthLabel.getFontMetrics(healthLabel.getFont());
        Dimension healthLabelSize = new Dimension(metrics.stringWidth(healthLabel.getText()), metrics.getHeight());
        Point healthLabelLocation = new Point((int) (1800 - healthLabelSize.getWidth()), 100 + metrics.getHeight() / 2);
        gamePanel.addObjectWithoutCollision(healthLabel, new Rectangle(healthLabelLocation, healthLabelSize));


        add(gamePanel);
    }

    public void startGame() {
        Game.exitThreads = false;
        info("Starting game");
        setVisible(true);
        if (gamePanel != null && player != null) {
            gamePanel.startGame();
            player.startMovingPLayer();
        } else {
            error("Game not started");
            destroy();
        }
    }

    public void destroy() {
        setVisible(false);
        for (Component c : gamePanel.getComponents()) {
            gamePanel.remove(c);
        }
        for (Component component : getComponents()) {
            remove(component);
        }
        System.gc();
        dispose();
        menu.showMainMenu();
        System.gc();
    }
}
