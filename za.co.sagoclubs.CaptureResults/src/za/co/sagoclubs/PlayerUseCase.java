package za.co.sagoclubs;

import static za.co.sagoclubs.InternetActions.getPlayerList;

import android.content.SharedPreferences;

import androidx.lifecycle.MutableLiveData;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class PlayerUseCase {
    private static volatile PlayerUseCase INSTANCE = null;

    private final static PlayerData NO_DATA = new PlayerData(new Player[0], new Player[0]);
    private final static MutableLiveData<PlayerData> playerData = new MutableLiveData<>(NO_DATA);

    private PlayerUseCase() {
    }

    public static PlayerUseCase getInstance() {
        if (INSTANCE == null) {
            synchronized (PlayerUseCase.class) {
                if (INSTANCE == null) {
                    INSTANCE = new PlayerUseCase();
                }
            }
        }
        return INSTANCE;
    }

    public void updatePlayerData() {
        ExecutorService executorService = RankApplication.getApp().getExecutorService();
        executorService.execute(() -> {
            try {
                List<Player> playerList = getPlayerList();
                Player[] allPlayers = toAllPlayerArray(playerList);
                Player[] localPlayers = toLocalPlayerArray(playerList);
                playerData.postValue(new PlayerData(allPlayers, localPlayers));
            } catch (IOException | JSONException e) {
                playerData.postValue(NO_DATA);
            }
        });

    }

    public MutableLiveData<PlayerData> getPlayerData() {
        return playerData;
    }

    public static Player[] getFavouritePlayers(SharedPreferences preferences) {
        PlayerData data = Objects.requireNonNullElse(playerData.getValue(), NO_DATA);

        Set<String> saved = Arrays.stream(preferences.getString("favourite_players", "")
                .split(",")).collect(Collectors.toSet());
        List<Player> list = new ArrayList<>();
        for (Player player : data.allPlayers) {
            if (saved.contains(player.getId())) {
                list.add(player);
            }
        }
        Player[] template = new Player[]{};
        return list.toArray(template);
    }

    public static Player[] playersForFavorites(Player[] currentFavorites,
                                               boolean showInternational) {
        PlayerData data = Objects.requireNonNullElse(playerData.getValue(), NO_DATA);
        if (showInternational) {
            return data.allPlayers;
        } else {
            return Stream.concat(
                            Arrays.stream(data.localPlayers),
                            Arrays.stream(currentFavorites))
                    .distinct()
                    .sorted(new PlayerSortByName())
                    .toArray(Player[]::new);
        }
    }

    public static Player[] playersToShow(boolean showFavourites, SharedPreferences preferences) {
        if (showFavourites) {
            return getFavouritePlayers(preferences);
        } else {
            PlayerData data = Objects.requireNonNullElse(playerData.getValue(), NO_DATA);
            return data.localPlayers;
        }
    }

    private static Player[] toAllPlayerArray(List<Player> playerData) {
        return playerData.stream()
                .sorted(new PlayerSortByName())
                .toArray(Player[]::new);
    }

    private static Player[] toLocalPlayerArray(List<Player> playerData) {
        return playerData.stream()
                .sorted(new PlayerSortByName())
                .filter(player -> !player.isInternational())
                .toArray(Player[]::new);
    }

    public record PlayerData(Player[] allPlayers, Player[] localPlayers) {
        public boolean hasData() {
            return allPlayers != null && allPlayers.length > 0;
        }
    }
}
