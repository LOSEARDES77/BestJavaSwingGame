package com.loseardes77.common;


import static com.loseardes77.common.Logger.error;

public class StreamData {

    public enum Type {
        JOIN,           // To join the game
        COLOR_ERROR,    // If there is already a player with that color
        LEVEL_DATA,     // The details of the level (walls, enemy quantity, ...)
        READY,          // Marks the player as ready
        START_GAME,     // Start the game
        MOVE,           // Move the player
        MATCH_ENDED,    // When the match has concluded
        SET_HEALTH,     // Change the health of any player
        ENEMY_CHANGE,   // A change on an enemy (direction or axis)
        ENEMY_TP,       // An enemy teleported
        EXIT,           // Finish
        UNKNOWN_TYPE,   // Unknown type
        INVALID,        // Bad packet
        OK,             // Everything is fine
        ERROR,          // Something went wrong
        PING,           // Check the ping
        PLAYER_JOINED,    // A player joined
    }


    public static Type getType(String typeName) {
        if (!isUpperCase(typeName))
            typeName = camelCaseToUpperCase(typeName);

        Type result = Type.UNKNOWN_TYPE;

        try {
            result = Enum.valueOf(StreamData.Type.class, typeName);
        } catch (Exception e) {
            error("Unknown type (" + e.getMessage() + ")");
        }

        return result;
    }

    private static String camelCaseToUpperCase(String typeName) {
        StringBuilder result = new StringBuilder();
        result.append(typeName.charAt(0));
        for (int i = 1; i < typeName.length(); i++) {
            if (Character.isUpperCase(typeName.charAt(i))) {
                result.append("_");
            }
            result.append(Character.toUpperCase(typeName.charAt(i)));
        }
        return result.toString();
    }

    private static boolean isUpperCase(String w) {
        return w.equals(w.toUpperCase());
    }

    public static Type getTypeFromData(String data) {
        String typeStr = data.split(" ")[0];
        return getType(typeStr);
    }
}

