package za.co.sagoclubs;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.ListView;

public class SelectLogPlayerActivity extends Activity {

    private ListView lsvSelectPlayer;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_log_player);

        lsvSelectPlayer = findViewById(R.id.lsvSelectPlayer);
        CheckBox chkFavourites = findViewById(R.id.chkFavourites);

        SharedPreferences preferences = getSharedPreferences("SETTINGS", MODE_PRIVATE);
        boolean showFavourites = preferences.getBoolean("show_favourites", false);
        chkFavourites.setChecked(showFavourites);

        showPlayers(showFavourites);

        lsvSelectPlayer.setOnItemClickListener((parent, view, position, id) -> {
            Player player = (Player) lsvSelectPlayer.getItemAtPosition(position);
            Result.setLogPlayer(player);
            Intent myIntent = new Intent(view.getContext(), LogFileActivity.class);
            startActivityForResult(myIntent, 0);
        });

        chkFavourites.setOnCheckedChangeListener((buttonView, isChecked) -> {
            showPlayers(isChecked);
            Editor editor = getSharedPreferences("SETTINGS", MODE_PRIVATE).edit();
            editor.putBoolean("show_favourites", isChecked);
            editor.apply();
        });
    }

    private void showPlayers(boolean showFavourites) {
        lsvSelectPlayer = findViewById(R.id.lsvSelectPlayer);
        PlayerArrayAdapter adapter = new PlayerArrayAdapter(this, R.layout.list_item, chooseWhatToShow(showFavourites));
        lsvSelectPlayer.setAdapter(adapter);
        lsvSelectPlayer.setFastScrollEnabled(true);
    }

    private Player[] chooseWhatToShow(boolean showFavourites) {
        if (showFavourites) {
            SharedPreferences preferences = getSharedPreferences("SETTINGS", MODE_PRIVATE);
            return InternetActions.getFavouritePlayers(preferences);
        } else {
            return InternetActions.getLocalPlayers();
        }
    }
}
