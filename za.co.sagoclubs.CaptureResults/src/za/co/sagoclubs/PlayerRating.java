package za.co.sagoclubs;

import androidx.annotation.NonNull;

public class PlayerRating extends Player {
    private final String rank;
    private final String index;
    private final String lastPlayedDate;


    public PlayerRating(String id, String name, String rank, String index, String lastPlayedDate) {
        super(id, name);
        this.rank = rank;
        this.index = index;
        this.lastPlayedDate = lastPlayedDate;
    }

    public String getRank() {
        return rank;
    }

    public String getIndex() {
        return index;
    }

    public String getLastPlayedDate() {
        return lastPlayedDate;
    }
}
