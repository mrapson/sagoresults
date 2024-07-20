package za.co.sagoclubs;

import java.util.Comparator;

public class PlayerSortByName implements Comparator<Player> {
    public int compare(Player a, Player b) {
        return a.getName().compareTo(b.getName());
    }
}
