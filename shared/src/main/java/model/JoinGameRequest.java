package model;

public record JoinGameRequest(String playerColor, int gameID) {
    public int getGameID() {
        return gameID;
    }

    public String getPlayerColor() {
        return playerColor;
    }
}
