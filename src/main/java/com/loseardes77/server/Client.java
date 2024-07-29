package com.loseardes77.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import static com.loseardes77.common.Logger.error;

public class Client implements Runnable {

    private final Socket s;
    private final DataInputStream dis;
    private final DataOutputStream dos;

    // Game data
    private boolean ready = false;

    public Client(Socket s) throws IOException {
        this.s = s;
        this.dis = new DataInputStream(s.getInputStream());
        this.dos = new DataOutputStream(s.getOutputStream());
    }

    public boolean sendData(String data) {
        try {
            this.dos.writeUTF(data);
            return true;
        } catch (IOException e) {
            error("Failed to send data to the client");
        }
        return false;
    }

    private boolean running = true;

    @Override
    public void run() {
        String received;

        while (!Server.isShutDown) {
            try {

                received = dis.readUTF();


                StreamData.Type type = StreamData.getTypeFromData(received);

                switch (type) {
                    case JOIN -> {
                    }

                    case SET_COLOR -> {
                    }

                    case COLOR_ERROR -> {
                    }

                    case READY_UP -> {

                    }

                    case LEVEL_DATA -> {
                    }

                    case START_GAME -> {
                    }

                    case MOVE -> {
                    }

                    case MATCH_ENDED -> {
                    }

                    case SET_HEALTH -> {
                    }

                    case ENEMY_CHANGE -> {
                    }

                    case ENEMY_TP -> {
                    }

                    case UNKNOW_TYPE -> {
                    }

                    case EXIT -> onExit();
                }

            } catch (IOException ex) {
                error("Connection lost with " + s.getPort());

                onExit();

                break;
            }
        }
    }

    private void onReady() {
        ready = true;
    }

    public boolean isReady() {
        return ready;
    }

    private void onExit() {
        running = false;
    }
}
