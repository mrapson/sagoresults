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

import java.util.concurrent.CompletableFuture;

public class PlayerRatingsActivity extends Activity {
    private ListView listView;
    private PlayerSortOrder preferredOrder = PlayerSortOrder.SORT_BY_RANK;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player_ratings);

        Button btnSortByRank = findViewById(R.id.btnSortByRank);
        Button btnSortByName = findViewById(R.id.btnSortByName);
        listView = findViewById(R.id.listView);

        updateList();

        btnSortByName.setOnClickListener(v -> {
            if (preferredOrder == PlayerSortOrder.SORT_BY_RANK) {
                preferredOrder = PlayerSortOrder.SORT_BY_NAME;
                updateList();
            }
        });

        btnSortByRank.setOnClickListener(v -> {
            if (preferredOrder == PlayerSortOrder.SORT_BY_NAME) {
                preferredOrder = PlayerSortOrder.SORT_BY_RANK;
                updateList();
            }
        });
    }

    private void updateList() {
        CompletableFuture
                .supplyAsync(() -> InternetActions.getPlayerRatingsArray(preferredOrder))
                .thenAccept(playerRatings -> {
                    Handler listViewUpdaterHandler = new Handler(Looper.getMainLooper());
                    listViewUpdaterHandler.post(() -> {
                        PlayerRatingArrayAdapter adapter = new PlayerRatingArrayAdapter(
                                this,
                                R.layout.player_rating_list_item,
                                playerRatings);
                        listView.setAdapter(adapter);
                        listView.setOnItemClickListener(playerItemClickListener);
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
