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

        updateList(PlayerSortOrder.SORT_BY_RANK, "OnCreate");

        Button btnSortByName = findViewById(R.id.btnSortByName);
        btnSortByName.setOnClickListener(v -> updateList(PlayerSortOrder.SORT_BY_NAME, "ByName"));
        Button btnSortByRank = findViewById(R.id.btnSortByRank);
        btnSortByRank.setOnClickListener(v -> updateList(PlayerSortOrder.SORT_BY_RANK, "ByRank"));
    }

    private void updateList(PlayerSortOrder newSortOrder, String caller) {
        if (updateLock.get() || newSortOrder == currentSortOrder) {
            return;
        }
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
