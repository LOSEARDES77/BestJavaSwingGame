package com.loseardes77.client;

import com.loseardes77.common.MainMenu;

import javax.swing.JColorChooser;
import javax.swing.JFrame;
import java.awt.Color;
import java.awt.Component;
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
        this.gamePanel = new Game(this);
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


        add(gamePanel);
    }

    public void startGame() {
        info("Starting game");
        setVisible(true);
        if (gamePanel != null && player != null) {
            gamePanel.startGame();
        } else {
            error("Game not started");
            destroy();
        }
    }

    public void destroy() {
        Game.exitThreads = true;
        setVisible(false);
        gamePanel.getPool().shutdown();
        for (Component c : gamePanel.getComponents()) {
            gamePanel.remove(c);
        }
        for (Component component : getComponents()) {
            remove(component);
        }
        System.gc();
        menu.showMainMenu();
        System.gc();
    }
}
