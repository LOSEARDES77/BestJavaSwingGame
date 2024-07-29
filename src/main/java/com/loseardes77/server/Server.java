package com.loseardes77.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.loseardes77.common.Logger.error;
import static com.loseardes77.common.Logger.info;

public class Server {

    public static volatile ClientManager clientManager;
    public static boolean isShutDown = false;
    public static ServerSocket ss;

    public Server() {
        try {
            int port = 5056;

            ss = new ServerSocket(port);
            System.out.println("Created Server at port " + port + ".");


            clientManager = new ClientManager();


            ThreadPoolExecutor executor = new ThreadPoolExecutor(
                    10, // corePoolSize
                    100, // maximumPoolSize
                    10, // thread timeout
                    TimeUnit.SECONDS,
                    new ArrayBlockingQueue<>(16) // queueCapacity
            );

            // admin
            executor.execute(new Admin());

            // server main loop - listen to client's connection
            while (!isShutDown) {
                try {
                    // socket object to receive incoming client requests
                    Socket s = ss.accept();
                    // System.out.println("+ New Client connected: " + s);

                    // create new client runnable object
                    Client c = new Client(s);
                    clientManager.add(c);

                    // execute client runnable
                    executor.execute(c);

                } catch (IOException ex) {
                    isShutDown = true;
                }
            }

            info("Shutting down executor");
            executor.shutdownNow();

        } catch (IOException ex) {
            error("Server crashed");
        }
    }
}
