package model;

public record GameData(
        int gameID,
        String whiteUsername,
        String blackUsername,
        String gameName,
        String gameData
) {
    public String gameData() {
        return gameName;
    }

    public Object getWhiteUsername() {
        return whiteUsername;
    }

    public Object getBlackUsername() {
        return blackUsername;
    }
}
