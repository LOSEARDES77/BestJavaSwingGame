package com.loseardes77.common;

import com.loseardes77.client.MultiPlayer;
import com.loseardes77.client.SinglePlayer;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.InetAddress;
import java.net.UnknownHostException;

import static com.loseardes77.common.Logger.error;
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

        multiplayerSelectorPanel = new JPanel(new BorderLayout());

        JLabel titleMp = new JLabel("Best Java Swing Game");
        multiplayerSelectorPanel.add(titleMp);
        titleMp.setFont(new Font("Arial", Font.BOLD, 40));
        titleMp.setBounds(170, 20, 500, 100);

        JPlaceHolderTextField hostInputBox = new JPlaceHolderTextField("Enter server address");
        multiplayerSelectorPanel.add(hostInputBox);
        hostInputBox.setBounds(210, 180, 350, 60);
        hostInputBox.setFont(new Font("Arial", Font.PLAIN, 16));
        hostInputBox.setHorizontalAlignment(JTextField.CENTER);

        JButton joinButton = new JButton("Join server");
        multiplayerSelectorPanel.add(joinButton);
        joinButton.setBounds(210, 270, 350, 60);
        joinButton.setFont(new Font("Arial", Font.BOLD, 18));
        joinButton.addActionListener(_ -> startMultiplayer(hostInputBox.getText()));

        /* TODO
        JButton hostButton = new JButton("Host your own");
        multiplayerSelectorPanel.add(hostButton);
        hostButton.setBounds(390, 270, 170, 60);
        hostButton.setFont(new Font("Arial", Font.BOLD, 18));
        hostButton.addActionListener(_ -> startMultiplayer("0.0.0.0"));
        */

        JButton back = new JButton("Back");
        multiplayerSelectorPanel.add(back);
        back.setBounds(210, 360, 350, 60);
        back.setFont(new Font("Arial", Font.BOLD, 18));

        back.addActionListener(_ -> showMainMenu());

        multiplayerSelectorPanel.add(new JLabel(" "));


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
            add(multiplayerSelectorPanel);
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

    public void startMultiplayer(String host) {
        if (host.isBlank()) {
            JOptionPane.showMessageDialog(this, "Please enter a server address", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        InetAddress address;

        try {
            address = InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            JOptionPane.showMessageDialog(this, "Unknown host", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        MultiPlayer mp = MultiPlayer.build(this, address);
        if (mp == null) {
            error("Multiplayer failed to build");
            return;
        }
        mp.showGameScreen();

    }
}
