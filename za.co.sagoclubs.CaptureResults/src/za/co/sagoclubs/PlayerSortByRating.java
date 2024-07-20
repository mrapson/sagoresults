package za.co.sagoclubs;

import java.util.Comparator;

public class PlayerSortByRating implements Comparator<PlayerRating> {
    Comparator<PlayerRating> comparator = Comparator
            .comparingInt(PlayerRating::getRankValue)
            .thenComparingInt(PlayerRating::getIndexValue)
            .thenComparing(PlayerRating::getName);

    public int compare(PlayerRating a, PlayerRating b) {
        // Return a descending sort by default
        return comparator.reversed().compare(a, b);
    }
}
