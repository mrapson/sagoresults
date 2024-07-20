package za.co.sagoclubs;

import androidx.annotation.NonNull;

public class Player implements Comparable<Player> {
    @NonNull
    private final String id;
    @NonNull
    private final String name;

    public Player(@NonNull String id, @NonNull String name) {
        this.id = id;
        this.name = name;
    }

    @NonNull
    public String getId() {
        return id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @NonNull
    public String toString() {
        return name;
    }

    @Override
    public int compareTo(Player other) {
        return this.name.compareTo(other.name);
    }
}
