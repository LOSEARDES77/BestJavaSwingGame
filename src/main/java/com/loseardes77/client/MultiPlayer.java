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
	
	/**
	 * Creates a new {@link MultiPlayer} object, displaying an error with a {@link JOptionPane}
	 * @param frame the target frame to display the game and error
	 * @param InetAddress the host address to connect
	 * @return the {@link MultiPlayer} object if the connection is sucessfull, null if it isn't
	 */
    public static MultiPlayer build(MainMenu frame, InetAddress host) {
        try {
            return new MultiPlayer(frame, host);
        } catch (SocketException e) {
            JOptionPane.showMessageDialog(frame, "Failed to connect to the server", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

	/**
	 * Creates a new {@link MultiPlayer} object
	 * @param frame the target frame to display the game
	 * @param InetAddress the host address to connect
	 * @return the {@link MultiPlayer} object
	 * @throws {@link SocketException} when the connection is unsuccessful
	 */
    public MultiPlayer(MainMenu frame, InetAddress host) throws SocketException {
        this.menuFrame = frame;
        this.packetManager = new PacketManager(host);

        packetManager.ping();	// The first thing we should do when connecting to a host is pinging it

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

        setSize(1900, 1060); //  KLUDGE Hard-coded res
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
	
	/**
	 * Starts the game menu
	 *
	 */
    public void showGameScreen() {
        Game.exitThreads = false;
        menuFrame.hideMenus();
        setVisible(true);

        JButton readyButton = new JButton("Not Ready");
        readyButton.setFont(new Font("Arial", Font.BOLD, 40));
        readyButton.setBackground(new Color(100, 0, 0));
        gamePanel.addObjectWithoutCollision(readyButton, new Rectangle(622, 380, 656, 300));
        readyButton.addActionListener(e -> { //  FIXME This should not have ever been written
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
                    pingLabel.setText(String.format("Ping: %02dms", averagePing));
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

	/**
	 * Starts the game event listener
	 *
	 */
    public void startListener() {
        new Thread(() -> {
            while (!Game.exitThreads) {
                Packet p = packetManager.receivePacket();
                switch (p.getType()) {
                    case MOVE -> { // Movent of a player
                        String[] data = p.getData().split(";");
                        String[] movedPlayerParts = data[0].split(",");
                        Color movedPlayerColor = new Color(Integer.parseInt(movedPlayerParts[0]), Integer.parseInt(movedPlayerParts[1]), Integer.parseInt(movedPlayerParts[2])); // Gets the player color of the moved player (used as an uuid)

                        if (gamePanel.hasPlayer(movedPlayerColor)) {
                            Player movedPlayer = gamePanel.getPlayer(movedPlayerColor);
                            movedPlayer.teleport(Integer.parseInt(data[1]), Integer.parseInt(data[2]));
                        } else {
                            warning("Player not found (" + movedPlayerColor + ")");
                        }

                    }
                    case START_GAME -> { // Starts the game
                        startGame();
                    }
                    case MATCH_ENDED -> { // When the match ends
                        JOptionPane.showMessageDialog(this, "Game ended", "Game Over", JOptionPane.INFORMATION_MESSAGE);
                        dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
                        destroy();

                    }
                    case PLAYER_JOINED -> { // When a new player joins
                        String[] data = p.getData().split(";");
                        Color newPlayerColor = new Color(Integer.parseInt(data[0]), Integer.parseInt(data[1]), Integer.parseInt(data[2]));
                        if (!gamePanel.hasMatchStarted()) {
                            Player newPlayer = new Player(false, gamePanel, newPlayerColor);
                        }

                    }
                    default -> { // MAYBE Send a packet to the server telling it to rentransfer last packet
                    }
                }
            }
        }).start();

    }

	/**
	 * Starts the main game loop
	 *
	 */
    public void startGame() {
        gamePanel.startGame();
        selfPlayer.startMovingPLayer();
    }

	/**
	 * Joins game by changing color
	 * @param playerColor The color to use for the player (used as an uuid)
	 * @return the {@link Color} of the newly created player
	 *
	 */
    public Color setPlayerColor(Color playerColor) { // FIXME Refactor this mess
        Packet response = packetManager.sendPacket(new Packet(StreamData.Type.JOIN, playerColor.getRed() + ";" + playerColor.getGreen() + ";" + playerColor.getBlue())); // There is no change color packet so joining and changing the color is the same

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

	/**
	 * Stops the game
	 *
	 */
    public void destroy() {
        packetManager.sendPacket(new Packet(StreamData.Type.DISCONNECT, ""));
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
