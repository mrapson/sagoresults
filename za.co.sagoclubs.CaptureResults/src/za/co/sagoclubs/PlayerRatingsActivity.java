package za.co.sagoclubs;

import android.app.Activity;
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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public class PlayerRatingsActivity extends Activity {
    private ListView listView;
    private TextView loadingStatusView;
    private PlayerSortOrder currentSortOrder = null;
    private final AtomicBoolean updateLock = new AtomicBoolean(false);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player_ratings);

        listView = findViewById(R.id.listView);
        loadingStatusView = findViewById(R.id.txtLoadingStatus);

        if (savedInstanceState == null) {
            populateList(PlayerSortOrder.SORT_BY_RANK, "OnCreate");
        } else {
            restoreProgress(savedInstanceState);
        }

        Button btnSortByName = findViewById(R.id.btnSortByName);
        btnSortByName.setOnClickListener(v -> updateList(PlayerSortOrder.SORT_BY_NAME, "ByName"));
        Button btnSortByRank = findViewById(R.id.btnSortByRank);
        btnSortByRank.setOnClickListener(v -> updateList(PlayerSortOrder.SORT_BY_RANK, "ByRank"));
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle saveState) {
        super.onSaveInstanceState(saveState);
        saveState.putString("currentSortOrder", currentSortOrder.toString());
    }

    private void restoreProgress(Bundle savedInstanceState) {
        String savedSortOrder = savedInstanceState.getString("currentSortOrder");
        if (savedSortOrder != null) {
            currentSortOrder = PlayerSortOrder.valueOf(savedSortOrder);
        }
        populateList(currentSortOrder, "Restore");
    }

    private void updateList(PlayerSortOrder newSortOrder, String caller) {
        if (updateLock.get() || newSortOrder == currentSortOrder) {
            return;
        }
        populateList(newSortOrder, caller);
    }

    private void populateList(PlayerSortOrder newSortOrder, String caller) {
        updateLock.set(true);
        currentSortOrder = newSortOrder;

        loadingStatusView.setText(getString(R.string.loading_message) + caller);
        loadingStatusView.setVisibility(View.VISIBLE);

        CompletableFuture
                .supplyAsync(() -> InternetActions.getPlayerRatingsArray(currentSortOrder))
                .thenAccept(playerRatings -> {
                    Handler listViewUpdaterHandler = new Handler(Looper.getMainLooper());
                    listViewUpdaterHandler.post(() -> {
                        PlayerRatingArrayAdapter adapter = new PlayerRatingArrayAdapter(
                                this,
                                R.layout.player_rating_list_item,
                                playerRatings);
                        listView.setAdapter(adapter);
                        listView.setOnItemClickListener(playerItemClickListener);
                        loadingStatusView.setVisibility(View.GONE);
                        updateLock.set(false);
                    });
                });
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public OnItemClickListener playerItemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Player p = (Player) listView.getItemAtPosition(position);
            Result.setLogFile(p);
            Intent myIntent = new Intent(view.getContext(), LogFileActivity.class);
            startActivityForResult(myIntent, 0);
        }
    };
}
