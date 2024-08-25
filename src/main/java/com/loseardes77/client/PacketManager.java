package com.loseardes77.client;

import com.loseardes77.common.StreamData;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import static com.loseardes77.common.Logger.error;
import static com.loseardes77.common.Logger.info;

public class PacketManager {
    private final int serverPort;
    private final InetAddress address;
    private final DatagramSocket clientSocket;

	/**
	 * Creates a new client socket for the specified address
	 * @param address an {@link InetAddress} to connect to
	 * @throws {@link SocketException} when the {@link DatagramSocket} creation fails
	 */
    public PacketManager(InetAddress address) throws SocketException {
        try {
            this.serverPort = 5784; // FIXME Hardcoded port
            this.clientSocket = new DatagramSocket();
            this.address = address;
        } catch (SocketException e) {
            error("Failed to create socket (" + e.getMessage() + ")");
            throw e;
        }
    }

	/**
	 * Sends a {@link Packet} through the net
	 * @param p The {@link Packet} to send
	 * @return the {@link Packet} that is sent as a respone or null if the packet was invalid or if it wasn't able to send it
	 */
    public Packet sendPacket(Packet p) {
        info("Sending [" + p.getType() + "] packet: " + p.getData());
        if (!p.isValid()) {		// Checks for invalid packets 
            error("Invalid packet : " + p.getRawData());
            return null;
        }
        try {

            byte[] sendData = p.getRawData().getBytes();
            DatagramPacket packet = new DatagramPacket(sendData, sendData.length, address, serverPort);
            clientSocket.send(packet);

        } catch (IOException e) {
            error("Failed to send packet (" + e.getMessage() + ")");
            return null;
        }

        info("Packet sent successfully");

        return receivePacket();
    }
	
	/**
	 * Waits to recive a {@link Packet}
	 * @return The recived {@link Packet} or {@code null} if there was an error while reciving the {@link Packet}
	 */
    public Packet receivePacket() {
        info("Awaiting to receive packet");
        try {									//  FIXME Gigantic try block
            byte[] receiveData = new byte[512]; // KLUDGE Hard coded 512 byte max packet size
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            clientSocket.receive(receivePacket);

            String data = new String(receivePacket.getData(), 0, receivePacket.getLength());
            info("Received data: " + data);
            StreamData.Type type = StreamData.getTypeFromData(data);
            Packet p;
            if (data.length() <= 16) { // Diferentes between pakcets with a dataStr or without it
                p = new Packet(type, null);
            } else {
                String dataStr = data.substring(16);
                p = new Packet(type, dataStr);
            }
            info("Received [" + p.getType() + "] packet: " + p.getData());
            return p;
        } catch (IOException e) {
            error("Failed to receive packet (" + e.getMessage() + ")");
            return null;
        }
    }

    public void closeSocket() {
        clientSocket.close();
    }

	/**
	 * Pings the server
	 * @return A vector, with the first number being the serverPing and the second being the clientPing
	 */
    public int[] ping() {
        Packet p = new Packet(StreamData.Type.PING, String.valueOf(System.currentTimeMillis()));
        Packet response = sendPacket(p);
        if (response == null) {	// MAYBE throw an exception
            error("Failed to ping server");
            return null;
        }

        long serverPing = System.currentTimeMillis() - Long.parseLong(response.getData().split(" ")[1]);
        long clientPing = Long.parseLong(response.getData().split(" ")[0]);

        info("To Server Ping: " + serverPing + "ms");
        info("From Server Ping: " + clientPing + "ms");

        return new int[]{(int) serverPing, (int) clientPing};

    }
}
