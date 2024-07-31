package com.loseardes77.client;

import com.loseardes77.server.StreamData;

import static com.loseardes77.common.Logger.error;

public class Packet {
    private final String data;
    private final boolean valid;

    public Packet(StreamData.Type type, String data) {
        this.data = buildData(type, data);

        if (this.data.getBytes().length > 512) {
            error("Data is too long");
            this.valid = false;
            return;
        }

        if (StreamData.getTypeFromData(this.data) != type) {
            error("Failed to create packet");
            this.valid = false;
            return;
        }

        this.valid = true;
    }

    private static String buildData(StreamData.Type type, String data) {
        StringBuilder dataBuilder = new StringBuilder(type.toString());

        if (dataBuilder.toString().length() < 16)
            dataBuilder.append(" ".repeat(16 - dataBuilder.toString().length()));

        if (data != null)
            dataBuilder.append(data);

        return dataBuilder.toString();
    }

    public String getData() {
        if (valid)
            return data;

        return buildData(StreamData.Type.INVALID, null);

    }

    public boolean isValid() {
        return valid;
    }
}
