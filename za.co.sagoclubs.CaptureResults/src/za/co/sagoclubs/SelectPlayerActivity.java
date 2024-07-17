package za.co.sagoclubs;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.ListView;

public class SelectPlayerActivity extends Activity {

    private ListView lsvSelectPlayer;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_player);

        lsvSelectPlayer = findViewById(R.id.lsvSelectPlayer);
        CheckBox chkFavourites = findViewById(R.id.chkFavourites);

        SharedPreferences preferences = getSharedPreferences("SETTINGS", MODE_PRIVATE);
        boolean showFavourites = preferences.getBoolean("show_favourites", false);
        chkFavourites.setChecked(showFavourites);
        chooseWhatToShow(showFavourites);

        lsvSelectPlayer.setOnItemClickListener((parent, view, position, id) -> {
            Player player = (Player) lsvSelectPlayer.getItemAtPosition(position);
            Result.setLogFile(player);
            Intent myIntent = new Intent(view.getContext(), LogFileActivity.class);
            startActivityForResult(myIntent, 0);
        });

        chkFavourites.setOnCheckedChangeListener((buttonView, isChecked) -> {
            chooseWhatToShow(isChecked);
            SharedPreferences preferences1 = getSharedPreferences("SETTINGS", MODE_PRIVATE);
            Editor editor = preferences1.edit();
            editor.putBoolean("show_favourites", isChecked);
            editor.commit();
        });
    }

    private void chooseWhatToShow(boolean showFavourites) {
        if (showFavourites) {
            showFavourites();
        } else {
            showAll();
        }
    }

    private void showAll() {
        lsvSelectPlayer = findViewById(R.id.lsvSelectPlayer);
        PlayerArrayAdapter adapter = new PlayerArrayAdapter(this, R.layout.list_item, InternetActions.getPlayerArray());
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
