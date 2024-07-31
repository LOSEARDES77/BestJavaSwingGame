package com.loseardes77.server;


import static com.loseardes77.common.Logger.error;

public class StreamData {

    public enum Type {
        JOIN,           // To join the game
        SET_COLOR,      // Change the color of the player
        COLOR_ERROR,    // If there is already a player with that color
        READY_UP,       // When the client is ready to start the match
        LEVEL_DATA,     // The details of the level (walls, enemy quantity, ...)
        START_GAME,     // Start the game
        MOVE,           // Move the player
        MATCH_ENDED,    // When the match has concluded
        SET_HEALTH,     // Change the health of any player
        ENEMY_CHANGE,   // A change on an enemy (direction or axis)
        ENEMY_TP,       // An enemy teleported
        EXIT,           // Finish
        UNKNOW_TYPE,    // Unknown type
        INVALID,        // Bad packet
    }


    public static Type getType(String typeName) {
        Type result = Type.UNKNOW_TYPE;

        try {
            result = Enum.valueOf(StreamData.Type.class, typeName);
        } catch (Exception e) {
            error("Unknown type" + e.getMessage());
        }

        return result;
    }

    public static Type getTypeFromData(String data) {
        String typeStr = data.split(" ")[0];
        return getType(typeStr);
    }
}

