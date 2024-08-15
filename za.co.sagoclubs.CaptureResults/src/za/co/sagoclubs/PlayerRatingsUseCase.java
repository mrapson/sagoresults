package za.co.sagoclubs;

import static za.co.sagoclubs.InternetActions.getPlayerRatingsList;

import androidx.lifecycle.MutableLiveData;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class PlayerRatingsUseCase {
    enum Status {
        None, Processing, Done, NetworkError, FileError
    }

    private static volatile PlayerRatingsUseCase INSTANCE = null;

    private final static MutableLiveData<PlayerRatingsRecord> playerRatingsRecord =
            new MutableLiveData<>(new PlayerRatingsRecord(List.of(), Status.None));

    private PlayerRatingsUseCase() {
    }

    public static PlayerRatingsUseCase getInstance() {
        if (INSTANCE == null) {
            synchronized (PlayerRatingsUseCase.class) {
                if (INSTANCE == null) {
                    INSTANCE = new PlayerRatingsUseCase();
                }
            }
        }
        return INSTANCE;
    }

    public void updatePlayerRatingsData() {
        PlayerRatingsRecord currentRatingsRecord = playerRatingsRecord.getValue();

        ExecutorService executorService = RankApplication.getApp().getExecutorService();
        executorService.execute(() -> {
            List<PlayerRating> currentRatings;
            if (currentRatingsRecord == null) {
                currentRatings = List.of();
            } else {
                currentRatings = currentRatingsRecord.ratings();
            }
            playerRatingsRecord.postValue(
                    new PlayerRatingsRecord(currentRatings, Status.Processing)
            );
            try {
                List<PlayerRating> playerRatingList = getPlayerRatingsList();
                playerRatingsRecord.postValue(
                        new PlayerRatingsRecord(playerRatingList, Status.Done)
                );
            } catch (JSONException e) {
                playerRatingsRecord.postValue(
                        new PlayerRatingsRecord(currentRatings, Status.FileError)
                );
            } catch (IOException e) {
                playerRatingsRecord.postValue(
                        new PlayerRatingsRecord(currentRatings, Status.NetworkError)
                );
            }
        });
    }

    public MutableLiveData<PlayerRatingsRecord> getPlayerRatingsRecord() {
        return playerRatingsRecord;
    }

    public static PlayerRating[] getPlayerRatingsArray(PlayerRatingsRecord playerRatingsRecord,
                                                       PlayerSortOrder sortOrder) {
        if (playerRatingsRecord == null) {
            return new PlayerRating[0];
        }

        return playerRatingsRecord.ratings.stream()
                .sorted(PlayerSortOrder.SORT_BY_NAME == sortOrder
                        ? new PlayerSortByName()
                        : new PlayerSortByRating())
                .toArray(PlayerRating[]::new);
    }

    public record PlayerRatingsRecord(List<PlayerRating> ratings, Status status) {}
}
