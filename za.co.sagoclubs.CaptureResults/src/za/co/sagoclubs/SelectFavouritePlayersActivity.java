package za.co.sagoclubs;

import static za.co.sagoclubs.PlayerUseCase.playersForFavorites;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class SelectFavouritePlayersActivity extends AppCompatActivity {
    private ListView lsvSelectFavouritePlayers;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_favourite_players);

        lsvSelectFavouritePlayers = findViewById(R.id.lsvSelectFavouritePlayers);

        CheckBox chkInternational = findViewById(R.id.chkInternational);
        boolean showInternational = getSharedPreferences("SETTINGS", MODE_PRIVATE)
                .getBoolean("show_international", false);
        chkInternational.setChecked(showInternational);

        showFavouriteOptions(showInternational);

        chkInternational.setOnCheckedChangeListener((buttonView, isChecked) -> {
            saveSelection();
            getSharedPreferences("SETTINGS", MODE_PRIVATE).edit()
                    .putBoolean("show_international", isChecked)
                    .apply();
            showFavouriteOptions(isChecked);
        });

        Button btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(v -> {
            saveSelection();
            finish();
        });
    }

    @Override
    public void onPause() {
        saveSelection();
        super.onPause();
    }

    private void saveSelection() {
        StringBuilder save = new StringBuilder();
        int count = lsvSelectFavouritePlayers.getAdapter().getCount();
        for (int i = 0; i < count; i++) {
            if (lsvSelectFavouritePlayers.isItemChecked(i)) {
                Player player = (Player) lsvSelectFavouritePlayers.getItemAtPosition(i);
                if (save.length() > 0) {
                    save.append(",").append(player.getId());
                } else {
                    save = new StringBuilder(player.getId());
                }
            }
        }
        getSharedPreferences("SETTINGS", MODE_PRIVATE).edit()
                .putString("favourite_players", save.toString())
                .apply();
    }

    private void showFavouriteOptions(boolean showInternational) {
        SharedPreferences preferences = getSharedPreferences("SETTINGS", MODE_PRIVATE);
        Player[] currentFavourites = PlayerUseCase.getFavouritePlayers(preferences);

        PlayerArrayAdapter adapter = new PlayerArrayAdapter(
                this,
                R.layout.multi_select_list_item,
                playersForFavorites(currentFavourites, showInternational));
        lsvSelectFavouritePlayers.setAdapter(adapter);
        lsvSelectFavouritePlayers.setFastScrollEnabled(true);

        loadSelection(currentFavourites);
    }

    private void loadSelection(Player[] currentFavorites) {
        Set<String> currentFavouritesIds = Arrays.stream(currentFavorites)
                .map(Player::getId)
                .collect(Collectors.toSet());

        int count = lsvSelectFavouritePlayers.getAdapter().getCount();
        for (int i = 0; i < count; i++) {
            Player player = (Player) lsvSelectFavouritePlayers.getItemAtPosition(i);
            if (currentFavouritesIds.contains(player.getId())) {
                lsvSelectFavouritePlayers.setItemChecked(i, true);
            }
        }
    }
}
