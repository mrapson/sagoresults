package za.co.sagoclubs;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.ListView;

public class SelectWhitePlayerActivity extends Activity {

    private ListView lsvSelectWhitePlayer;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.select_white_player);

        lsvSelectWhitePlayer = findViewById(R.id.lsvSelectWhitePlayer);
        CheckBox chkFavourites = findViewById(R.id.chkFavourites);

        SharedPreferences preferences = getSharedPreferences("SETTINGS", MODE_PRIVATE);
        boolean showFavourites = preferences.getBoolean("show_favourites", false);
        chkFavourites.setChecked(showFavourites);
        chooseWhatToShow(showFavourites);

        lsvSelectWhitePlayer.setOnItemClickListener((parent, view, position, id) -> {
            Player player = (Player) lsvSelectWhitePlayer.getItemAtPosition(position);
            Result.setWhite(player);
            Intent myIntent = new Intent(view.getContext(), SelectBlackPlayerActivity.class);
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
        lsvSelectWhitePlayer = findViewById(R.id.lsvSelectWhitePlayer);
        PlayerArrayAdapter adapter = new PlayerArrayAdapter(this, R.layout.list_item, InternetActions.getPlayerArray());
        lsvSelectWhitePlayer.setAdapter(adapter);
        lsvSelectWhitePlayer.setFastScrollEnabled(true);
    }

    private void showFavourites() {
        lsvSelectWhitePlayer = findViewById(R.id.lsvSelectWhitePlayer);
        SharedPreferences preferences = getSharedPreferences("SETTINGS", MODE_PRIVATE);
        PlayerArrayAdapter adapter = new PlayerArrayAdapter(this, R.layout.list_item, InternetActions.getFavouritePlayers(preferences));
        lsvSelectWhitePlayer.setAdapter(adapter);
        lsvSelectWhitePlayer.setFastScrollEnabled(true);
    }

}

