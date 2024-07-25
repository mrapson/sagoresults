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

    private boolean showFavourites;
    private boolean showInternational;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_log_player);

        lsvSelectPlayer = findViewById(R.id.lsvSelectPlayer);
        CheckBox chkFavourites = findViewById(R.id.chkFavourites);
        CheckBox chkInternational = findViewById(R.id.chkInternational);

        SharedPreferences preferences = getSharedPreferences("SETTINGS", MODE_PRIVATE);
        showFavourites = preferences.getBoolean("show_favourites", false);
        chkFavourites.setChecked(showFavourites);
        showInternational = preferences.getBoolean("show_international", false);
        chkInternational.setChecked(showInternational);
        chooseWhatToShow(showFavourites, showInternational);

        lsvSelectPlayer.setOnItemClickListener((parent, view, position, id) -> {
            Player player = (Player) lsvSelectPlayer.getItemAtPosition(position);
            Result.setLogFile(player);
            Intent myIntent = new Intent(view.getContext(), LogFileActivity.class);
            startActivityForResult(myIntent, 0);
        });

        chkFavourites.setOnCheckedChangeListener((buttonView, isChecked) -> {
            chooseWhatToShow(isChecked, showInternational);
            Editor editor = getSharedPreferences("SETTINGS", MODE_PRIVATE).edit();
            editor.putBoolean("show_favourites", isChecked);
            editor.commit();
        });

        // TODO deal with checkbox interaction
        chkInternational.setOnCheckedChangeListener((buttonView, isChecked) -> {
            chooseWhatToShow(showFavourites, isChecked);
            Editor editor = getSharedPreferences("SETTINGS", MODE_PRIVATE).edit();
            editor.putBoolean("show_international", isChecked);
            editor.commit();
        });
    }

    private void chooseWhatToShow(boolean showFavourites, boolean showInternational) {
        if (showFavourites) {
            showFavourites();
        } else if (showInternational) {
            showAll();
        } else {
            showLocal();
        }
    }

    private void showAll() {
        lsvSelectPlayer = findViewById(R.id.lsvSelectPlayer);
        PlayerArrayAdapter adapter = new PlayerArrayAdapter(this, R.layout.list_item, InternetActions.getAllPlayers());
        lsvSelectPlayer.setAdapter(adapter);
        lsvSelectPlayer.setFastScrollEnabled(true);
    }

    private void showLocal() {
        lsvSelectPlayer = findViewById(R.id.lsvSelectPlayer);
        PlayerArrayAdapter adapter = new PlayerArrayAdapter(this, R.layout.list_item, InternetActions.getLocalPlayers());
        lsvSelectPlayer.setAdapter(adapter);
        lsvSelectPlayer.setFastScrollEnabled(true);
    }

    private void showFavourites() {
        lsvSelectPlayer = findViewById(R.id.lsvSelectPlayer);
        SharedPreferences preferences = getSharedPreferences("SETTINGS", MODE_PRIVATE);
        PlayerArrayAdapter adapter = new PlayerArrayAdapter(this, R.layout.list_item, InternetActions.getFavouritePlayers(preferences));
        lsvSelectPlayer.setAdapter(adapter);
        lsvSelectPlayer.setFastScrollEnabled(true);
    }
}
