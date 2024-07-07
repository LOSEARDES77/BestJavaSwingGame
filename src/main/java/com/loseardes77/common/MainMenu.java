package com.loseardes77.common;

import com.loseardes77.client.SinglePlayer;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import static com.loseardes77.common.Logger.info;

public class MainMenu extends JFrame {

    private final JPanel mainMenuPanel;
    private final JPanel multiplayerSelectorPanel;

    public MainMenu() {
        int[] windowDimensions = {800, 500};
        setTitle("Main Menu");
        setSize(windowDimensions[0], windowDimensions[1]);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setUndecorated(true);

        mainMenuPanel = new JPanel(new BorderLayout());

        JLabel title = new JLabel("Best Java Swing Game");
        mainMenuPanel.add(title);
        title.setFont(new Font("Arial", Font.BOLD, 40));
        title.setBounds(170, 20, 500, 100);

        JButton singlePlayer = new JButton("Single Player");
        mainMenuPanel.add(singlePlayer);
        singlePlayer.setBounds(210, 180, 350, 60);
        singlePlayer.setFont(new Font("Arial", Font.BOLD, 18));
        singlePlayer.addActionListener(_ -> startSinglePlayer());

        JButton multiPlayer = new JButton("Multi Player");
        mainMenuPanel.add(multiPlayer);
        multiPlayer.setBounds(210, 270, 350, 60);
        multiPlayer.setFont(new Font("Arial", Font.BOLD, 18));
        multiPlayer.addActionListener(_ -> showMultiplayerSelector());

        JButton quit = new JButton("Quit");
        mainMenuPanel.add(quit);
        quit.setBounds(210, 360, 350, 60);
        quit.setFont(new Font("Arial", Font.BOLD, 18));

        quit.addActionListener(_ -> dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING)));

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                info("Closing Main Menu");
                super.windowClosing(e);
            }
        });

        mainMenuPanel.add(new JLabel(" "));

        // TODO: Add the options to host or join a game and play
        multiplayerSelectorPanel = new JPanel(new BorderLayout());

    }

    public void showMainMenu() {
        setVisible(false);
        if (!getComponent(0).equals(mainMenuPanel)) {
            remove(multiplayerSelectorPanel);
            add(mainMenuPanel);
        }
        setVisible(true);
    }

    public void showMultiplayerSelector() {
        setVisible(false);
        if (!getComponent(0).equals(multiplayerSelectorPanel)) {
            remove(mainMenuPanel);
            add(mainMenuPanel);
        }
        setVisible(true);
    }

    public void hideMenus() {
        setVisible(false);
    }

    public void startSinglePlayer() {
        hideMenus();
        SinglePlayer sp = new SinglePlayer(this);
        sp.startGame();
    }

    public void startMultiplayer() {
        hideMenus();
        // MultiPlayer mp = new MultiPlayer(this)
        // mp.startGame();
    }
}
