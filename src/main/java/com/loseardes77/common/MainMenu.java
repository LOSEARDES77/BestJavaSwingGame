package com.loseardes77.common;

import static com.loseardes77.common.Logger.info;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import com.loseardes77.client.SinglePlayer;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MainMenu extends JFrame {

    private final int[] windowDimensions = {800, 500};
    private final JPanel menuPanel;

    public MainMenu() {
        setTitle("Main Menu");
        setSize(windowDimensions[0], windowDimensions[1]);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setUndecorated(true);

        menuPanel = new JPanel(new BorderLayout());
        add(menuPanel);

        JLabel title = new JLabel("Best Java Swing Game");
        menuPanel.add(title);
        title.setFont(new Font("Arial", Font.BOLD, 40));
        title.setBounds(170, 20, 500, 100);

        JButton singlePlayer = new JButton("Single Player");
        menuPanel.add(singlePlayer);
        singlePlayer.setBounds(210, 180, 350, 60);
        singlePlayer.setFont(new Font("Arial", Font.BOLD, 18));
        singlePlayer.addActionListener(_ -> startSinglePlayer());

        JButton multiPlayer = new JButton("Multi Player");
        menuPanel.add(multiPlayer);
        multiPlayer.setBounds(210, 270, 350, 60);
        multiPlayer.setFont(new Font("Arial", Font.BOLD, 18));
        multiPlayer.addActionListener(_ -> startMultiplayer());

        JButton quit = new JButton("Quit");
        menuPanel.add(quit);
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

        menuPanel.add(new JLabel(" "));
    }
    public void showMenu() {
        setVisible(true);
    }

    public void hideMenu() {
        setVisible(false);
    }

    public void startSinglePlayer() {
        hideMenu();
        SinglePlayer sp = SinglePlayer.build(this);
        sp.startGame();
    }

    public void startMultiplayer() {
        hideMenu();
        // MultiPlayer mp = MultiPlayer.build(this);
        // mp.startGame();
    }
}
