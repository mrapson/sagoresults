package za.co.sagoclubs;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Player {
    @NonNull
    private final String id;
    @NonNull
    private final String name;
    @Nullable
    private final Boolean international;

    public Player(@NonNull String id, @NonNull String name) {
        this.id = id;
        this.name = name;
        this.international = null;
    }

    public Player(@NonNull String id, @NonNull String name, boolean international) {
        this.id = id;
        this.name = name;
        this.international = international;
    }

    @NonNull
    public String getId() {
        return id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public Boolean isInternational() {
        return international;
    }

    @NonNull
    public String toString() {
        return name;
    }
}
