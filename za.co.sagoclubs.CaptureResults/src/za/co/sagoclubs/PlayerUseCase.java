package za.co.sagoclubs;

import static za.co.sagoclubs.InternetActions.getRawPlayerList;

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

    private final static MutableLiveData<List<Player>> playerData =
            new MutableLiveData<>(List.of());
    private final static MutableLiveData<Player[]> allPlayers =
            new MutableLiveData<>(new Player[0]);
    private final static MutableLiveData<Player[]> localPlayers =
            new MutableLiveData<>(new Player[0]);

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
                List<Player> playerList = getRawPlayerList();
                playerData.postValue(playerList);
                allPlayers.postValue(toAllPlayerArray(playerList));
                localPlayers.postValue(toLocalPlayerArray(playerList));
            } catch (IOException | JSONException e) {
                playerData.postValue(List.of());
                allPlayers.postValue(new Player[0]);
                localPlayers.postValue(new Player[0]);
            }
        });

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

    public static Player[] getFavouritePlayers(SharedPreferences preferences) {
        List<Player> allPlayers = playerData.getValue();
        if (allPlayers == null) {
            return new Player[0];
        }

        Set<String> saved = Arrays.stream(preferences.getString("favourite_players", "")
                .split(",")).collect(Collectors.toSet());
        List<Player> list = new ArrayList<>();
        for (Player player : allPlayers) {
            if (saved.contains(player.getId())) {
                list.add(player);
            }
        }
        Player[] template = new Player[]{};
        return list.toArray(template);
    }

    public static Player[] playersToShow(boolean showFavourites, SharedPreferences preferences) {
        if (showFavourites) {
            return getFavouritePlayers(preferences);
        } else {
            Player[] players = localPlayers.getValue();
            return Objects.requireNonNullElseGet(players, () -> new Player[0]);
        }
    }

    public static Player[] playersForFavorites(Player[] currentFavorites,
                                               boolean showInternational) {
        if (showInternational) {
            return Objects.requireNonNullElseGet(allPlayers.getValue(), () -> new Player[0]);
        } else {
            return Stream.concat(
                    Arrays.stream(
                            Objects.requireNonNullElseGet(
                                    localPlayers.getValue(),
                                    () -> new Player[0])),
                            Arrays.stream(currentFavorites))
                    .distinct()
                    .sorted(new PlayerSortByName())
                    .toArray(Player[]::new);
        }
    }
}
