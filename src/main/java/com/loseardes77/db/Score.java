package com.loseardes77.db;

import java.io.OutputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static com.loseardes77.common.Logger.error;

public class Score implements Serializable {
    private String name;
    private int score;

    public Score() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void sendScore() {
        HttpURLConnection connection = null;
        try {
            URL url = new URI("http://vps.mariol03.es:8081/set-score").toURL();
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            try (OutputStream os = connection.getOutputStream()) {
                byte[] postData = ("name=" + name + "&score=" + score).getBytes(StandardCharsets.UTF_8);
                os.write(postData, 0, postData.length);
            }

            connection.getResponseCode();
        } catch (Exception e) {
            error("Couldn't connect to the database");
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    @Override
    public String toString() {
        return "Score{" +
                "name='" + name + '\'' +
                ", score=" + score +
                '}';
    }
}