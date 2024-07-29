package com.loseardes77.server;

import java.util.ArrayList;

public class ClientManager {

    ArrayList<Client> clients;

    public ClientManager() {
        clients = new ArrayList<>();
    }

    public boolean add(Client c) {
        if (!clients.contains(c)) {
            clients.add(c);
            return true;
        }
        return true;
    }

    public boolean remove(Client c) {
        if (clients.contains(c)) {
            clients.remove(c);
            return true;
        }
        return false;
    }

    public void broadcast(String msg) {
        clients.forEach((c) -> {
            c.sendData(msg);
        });
    }

    public Client findClientFindingMatch() {
        for (Client c : clients) {
            if (c.isReady()) {
                return c;
            }
        }

        return null;
    }

    public int getSize() {
        return clients.size();
    }
}

