package za.co.sagoclubs;

import static za.co.sagoclubs.PlayerRatingsUseCase.getPlayerRatingsArray;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import za.co.sagoclubs.LogFileUseCase.Requester;

public class PlayerRatingsActivity extends AppCompatActivity {
    private ListView listView;
    private TextView loadingStatusView;
    private PlayerSortOrder currentSortOrder = PlayerSortOrder.SORT_BY_RANK;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player_ratings);

        listView = findViewById(R.id.listView);
        loadingStatusView = findViewById(R.id.txtLoadingStatus);

        if (savedInstanceState != null) {
            String savedSortOrder = savedInstanceState.getString("currentSortOrder");
            if (savedSortOrder != null) {
                currentSortOrder = PlayerSortOrder.valueOf(savedSortOrder);
            }
        }

        PlayerRatingsUseCase playerRatingsUseCase = PlayerRatingsUseCase.getInstance();
        playerRatingsUseCase.updatePlayerRatingsData();
        playerRatingsUseCase.getPlayerRatingsRecord()
                .observe(this, playerRatingsRecord ->
                        populateList(playerRatingsRecord, currentSortOrder));

        Button btnSortByName = findViewById(R.id.btnSortByName);
        btnSortByName.setOnClickListener(v -> updateList(PlayerSortOrder.SORT_BY_NAME));
        Button btnSortByRank = findViewById(R.id.btnSortByRank);
        btnSortByRank.setOnClickListener(v -> updateList(PlayerSortOrder.SORT_BY_RANK));
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle saveState) {
        super.onSaveInstanceState(saveState);
        saveState.putString("currentSortOrder", currentSortOrder.toString());
    }

    private void updateList(PlayerSortOrder newSortOrder) {
        if (newSortOrder == currentSortOrder) {
            return;
        }
        PlayerRatingsUseCase.PlayerRatingsRecord ratingsRecord =
                PlayerRatingsUseCase.getInstance().getPlayerRatingsRecord().getValue();
        if (ratingsRecord == null) {
            return;
        }
        populateList(ratingsRecord, newSortOrder);
    }

    private void populateList(PlayerRatingsUseCase.PlayerRatingsRecord ratingsRecord,
                               PlayerSortOrder newSortOrder) {
        currentSortOrder = newSortOrder;
        switch (ratingsRecord.status()) {
            case Processing -> showStatus(ratingsRecord,
                    getString(R.string.loading_message),
                    newSortOrder);
            default -> {
                loadingStatusView.setVisibility(View.GONE);
                setPlayerRatings(getPlayerRatingsArray(ratingsRecord,  newSortOrder));
            }
            case NetworkError -> showStatus(ratingsRecord,
                    getString(R.string.network_error_message),
                    newSortOrder);
            case FileError -> showStatus(ratingsRecord,
                    getString(R.string.ratings_file_error_message),
                    newSortOrder);
        }
    }

    private void showStatus(PlayerRatingsUseCase.PlayerRatingsRecord ratingsRecord,
                            String message,
                            PlayerSortOrder newSortOrder) {
        loadingStatusView.setText(message);
        loadingStatusView.setVisibility(View.VISIBLE);
        setPlayerRatings(getPlayerRatingsArray(ratingsRecord, newSortOrder));
    }

    private void setPlayerRatings(PlayerRating[] playerRatings) {
        Handler listViewUpdaterHandler = new Handler(Looper.getMainLooper());
        listViewUpdaterHandler.post(() -> {
            PlayerRatingArrayAdapter adapter = new PlayerRatingArrayAdapter(
                    this,
                    R.layout.player_rating_list_item,
                    playerRatings);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(playerItemClickListener);
        });
    }

    private final OnItemClickListener playerItemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Player player = (Player) listView.getItemAtPosition(position);

            LogFileUseCase.getInstance().prepareRequest(player, Requester.RatingsLookup);
            Intent myIntent = new Intent(view.getContext(), LogFileActivity.class);
            startActivity(myIntent);
        }
    };
}
