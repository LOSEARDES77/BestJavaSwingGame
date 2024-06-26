package com.loseardes77.client;

import static com.loseardes77.common.Logger.error;
import static com.loseardes77.common.Logger.info;
import com.loseardes77.common.MainMenu;
import javax.swing.JColorChooser;
import javax.swing.JFrame;

import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class SinglePlayer extends JFrame {

    private MainMenu menu = null;

    private Game gamePanel = null;
    private Player player = null;

    public static SinglePlayer build(MainMenu menu) {
        return new SinglePlayer(menu);
    }

    public SinglePlayer(MainMenu menuFrame) {
        setSize(1900, 1060);
        setLocationRelativeTo(null);
        setResizable(false);
        setUndecorated(true);
        menu = menuFrame;
        menu.hideMenu();

        Color playerColor = JColorChooser.showDialog(null, "Select a player color", new Color(51, 153, 255));
        if (playerColor == null) {
            error("No color selected for player. Exiting...");
            return;
        }
        this.gamePanel = new Game();
        this.player = new Player(true, gamePanel, playerColor);
        gamePanel.addPlayer(player);

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                info("Closing game");
                destroy();
            }
        });

        add(gamePanel);
    }

    public void startGame() {
        info("Starting game");
        setVisible(true);
        if (gamePanel != null && player != null) {
            gamePanel.startGame();
            player.startMovingPLayer();
        }else{
            error("Game not started");
            destroy();
        }
    }

    public void destroy(){
        setVisible(false);
        gamePanel.destroy();
        gamePanel = null;
        System.gc();
        dispose();
        menu.showMenu();
        System.gc();
    }
}
