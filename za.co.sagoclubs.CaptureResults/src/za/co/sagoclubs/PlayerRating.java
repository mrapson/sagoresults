package za.co.sagoclubs;

public class PlayerRating extends Player {
    private final String rank;
    private final String index;
    private final String lastPlayedDate;


    public PlayerRating(String id, String name, String rank, String index, String lastPlayedDate) {
        super(id, name);
        this.rank = rank.strip();
        this.index = index;
        this.lastPlayedDate = lastPlayedDate;
    }

    public String getRank() {
        return rank;
    }

    public String getIndex() {
        return index;
    }

    public String getRatingString() {
        return rank + " (" + index + ")";
    }

    public String getLastPlayedDate() {
        return lastPlayedDate;
    }

    public static int getRankValue(PlayerRating player) {
        String rank = player.getRank();
        int rankValue = Integer.parseInt(rank.replaceAll("\\D", ""));
        if (rank.charAt(rank.length() - 1) == 'd') {
            return rankValue;
        } else {
            return 1 - rankValue;
        }
    }

    public static int getIndexValue(PlayerRating player) {
        return Integer.parseInt(player.getIndex());
    }
}
