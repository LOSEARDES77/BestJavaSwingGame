package com.loseardes77.client;

import com.loseardes77.server.StreamData;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import static com.loseardes77.common.Logger.error;

public class PacketManager {
    private final int serverPort;
    private final InetAddress address;
    private final DatagramSocket clientSocket;

    public PacketManager(String address) throws UnknownHostException, SocketException {
        String[] parts = address.split(":");
        String serverIp = parts[0];
        this.serverPort = Integer.parseInt(parts[1]);
        try {
            this.clientSocket = new DatagramSocket();
            this.address = InetAddress.getByName(serverIp);
        } catch (UnknownHostException e) {
            error("Failed to resolve server address (" + e.getMessage() + ")");
            throw e;
        } catch (SocketException e) {
            error("Failed to create socket (" + e.getMessage() + ")");
            throw e;
        }
    }

    public boolean sendPacket(Packet p) {
        if (!p.isValid()) {
            error("Invalid packet");
            return false;
        }
        try {

            byte[] sendData = p.getData().getBytes();
            DatagramPacket packet = new DatagramPacket(sendData, sendData.length, address, serverPort);
            clientSocket.send(packet);

        } catch (IOException e) {
            error("Failed to send packet (" + e.getMessage() + ")");
            return false;
        }

        return true;
    }

    public Packet receivePacket() {
        try {
            byte[] receiveData = new byte[512];
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            clientSocket.receive(receivePacket);

            String data = new String(receivePacket.getData(), 0, receivePacket.getLength());
            StreamData.Type type = StreamData.getTypeFromData(data);

            return new Packet(type, data.substring(16, data.length() - 1));
        } catch (IOException e) {
            error("Failed to receive packet (" + e.getMessage() + ")");
            return null;
        }
    }

    public void startListener() {
        new Thread(() -> {
            while (!Game.exitThreads) {
                Packet p = receivePacket();
                // TODO: Handle packets
            }
        }).start();

    }
}
