package com.loseardes77.client;

import com.loseardes77.common.MainMenu;
import com.loseardes77.common.StreamData;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.InetAddress;
import java.net.SocketException;

import static com.loseardes77.common.Logger.error;
import static com.loseardes77.common.Logger.info;
import static com.loseardes77.common.Logger.warning;

public class MultiPlayer extends JFrame {
    private final MainMenu menuFrame;
    private final PacketManager packetManager;
    private final Game gamePanel;
    private final Player selfPlayer;

    public static MultiPlayer build(MainMenu frame, InetAddress host) {
        try {
            return new MultiPlayer(frame, host);
        } catch (SocketException e) {
            JOptionPane.showMessageDialog(frame, "Failed to connect to the server", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    public MultiPlayer(MainMenu frame, InetAddress host) throws SocketException {
        this.menuFrame = frame;
        this.packetManager = new PacketManager(host);

        packetManager.ping();

        Color playerColor = JColorChooser.showDialog(null, "Select a player color", new Color((int) (255 * Math.random()), (int) (255 * Math.random()), (int) (255 * Math.random())));
        if (playerColor == null) {
            error("No color selected for player. Exiting...");
            System.exit(1);
        } else {
            playerColor = setPlayerColor(playerColor);
        }


        info("Creating game instance");

        this.gamePanel = new Game(this, false, false, false);

        info("Creating player instance");

        this.selfPlayer = new Player(true, gamePanel, playerColor);
        gamePanel.addPlayer(selfPlayer, true);

        info("Setting up game window");

        setSize(1900, 1060);
        setLocationRelativeTo(null);
        setResizable(false);
        setUndecorated(true);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                destroy();
                info("Closing game");
            }
        });


        add(gamePanel);

        info("Everything setup successfully");

    }

    // TODO: Read packets for coins
    // TODO: Read packets for other players
    // TODO: Send packets for player movement
    // TODO: Ready up system
    // TODO: Handle game end
    // TODO: Handle player disconnect
    // TODO: Handle more than one player

    public void showGameScreen() {
        Game.exitThreads = false;
        menuFrame.hideMenus();
        setVisible(true);

        JButton readyButton = new JButton("Not Ready");
        readyButton.setFont(new Font("Arial", Font.BOLD, 40));
        readyButton.setBackground(new Color(100, 0, 0));
        gamePanel.addObjectWithoutCollision(readyButton, new Rectangle(622, 380, 656, 300));
        readyButton.addActionListener(e -> {
            readyButton.setText(readyButton.getText().equals("✓ Ready") ? "Not Ready" : "✓ Ready");
            if (readyButton.getText().equals("✓ Ready")) {
                packetManager.sendPacket(new Packet(StreamData.Type.READY, null));
                readyButton.setBackground(new Color(0, 100, 0));
            } else {
                packetManager.sendPacket(new Packet(StreamData.Type.READY, null));
                readyButton.setBackground(new Color(100, 0, 0));
            }
        });
        JLabel pingLabel = new JLabel("Ping: --ms");
        pingLabel.setFont(new Font("Arial", Font.BOLD, 18));
        gamePanel.addObjectWithoutCollision(pingLabel, new Rectangle(20, 20, 200, 50));
        new Thread(() -> {
            while (!Game.exitThreads) {
                info("RUNNING PING");
                int[] pings = packetManager.ping();
                if (pings != null) {
                    int averagePing = (int) (((double) pings[0] + pings[1]) / 2.0);
                    pingLabel.setText(String.format("Ping: %02dims", averagePing));
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    error("Failed to sleep thread (" + e.getMessage() + ")");
                }
            }
        }).start();

        // startListener();

    }

    public void startListener() {
        new Thread(() -> {
            while (!Game.exitThreads) {
                Packet p = packetManager.receivePacket();
                switch (p.getType()) {
                    case MOVE -> {
                        String[] data = p.getData().split(";");
                        String[] movedPlayerParts = data[0].split(",");
                        Color movedPlayerColor = new Color(Integer.parseInt(movedPlayerParts[0]), Integer.parseInt(movedPlayerParts[1]), Integer.parseInt(movedPlayerParts[2]));

                        if (gamePanel.hasPlayer(movedPlayerColor)) {
                            Player movedPlayer = gamePanel.getPlayer(movedPlayerColor);
                            movedPlayer.teleport(Integer.parseInt(data[1]), Integer.parseInt(data[2]));
                        } else {
                            warning("Player not found (" + movedPlayerColor + ")");
                        }

                    }
                    case START_GAME -> {
                        startGame();
                    }
                    case MATCH_ENDED -> {
                        JOptionPane.showMessageDialog(this, "Game ended", "Game Over", JOptionPane.INFORMATION_MESSAGE);
                        dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
                        destroy();

                    }
                    case PLAYER_JOINED -> {
                        String[] data = p.getData().split(";");
                        Color newPlayerColor = new Color(Integer.parseInt(data[0]), Integer.parseInt(data[1]), Integer.parseInt(data[2]));
                        if (!gamePanel.hasMatchStarted()) {
                            Player newPlayer = new Player(false, gamePanel, newPlayerColor);
                        }

                    }
                    default -> {
                    }
                }
            }
        }).start();

    }

    public void startGame() {
        gamePanel.startGame();
        selfPlayer.startMovingPLayer();
    }

    public Color setPlayerColor(Color playerColor) {
        Packet response = packetManager.sendPacket(new Packet(StreamData.Type.JOIN, playerColor.getRed() + ";" + playerColor.getGreen() + ";" + playerColor.getBlue()));

        if (response == null) {
            JOptionPane.showMessageDialog(menuFrame, "Failed to set player color", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }

        Color finalColor = playerColor;
        while (response.getType() != StreamData.Type.OK) {
            if (response.getType() == StreamData.Type.COLOR_ERROR) {
                JOptionPane.showMessageDialog(menuFrame, "Color already picked", "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                error("Failed to set player color. Got [" + response.getType() + "] -> " + response.getData());
            }

            finalColor = JColorChooser.showDialog(null, "Select a player color", new Color((int) (255 * Math.random()), (int) (255 * Math.random()), (int) (255 * Math.random())));
            response = packetManager.sendPacket(new Packet(StreamData.Type.JOIN, finalColor.getRed() + ";" + finalColor.getGreen() + ";" + finalColor.getBlue()));
        }

        info("Player color set successfully");

        return finalColor;
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
        menuFrame.showMainMenu();
        System.gc();
    }
}
