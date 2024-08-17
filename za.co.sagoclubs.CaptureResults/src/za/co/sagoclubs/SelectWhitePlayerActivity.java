package za.co.sagoclubs;

import static za.co.sagoclubs.PlayerUseCase.playersToShow;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

public class SelectWhitePlayerActivity extends AppCompatActivity {
    private ListView lsvSelectWhitePlayer;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_white_player);

        lsvSelectWhitePlayer = findViewById(R.id.lsvSelectWhitePlayer);
        CheckBox chkFavourites = findViewById(R.id.chkFavourites);

        SharedPreferences preferences = getSharedPreferences("SETTINGS", MODE_PRIVATE);
        boolean showFavourites = preferences.getBoolean("show_favourites", false);
        chkFavourites.setChecked(showFavourites);
        PlayerUseCase.getInstance()
                .getPlayerData()
                .observe(this, (playerData) -> showPlayers(showFavourites));

        lsvSelectWhitePlayer.setOnItemClickListener((parent, view, position, id) -> {
            Player player = (Player) lsvSelectWhitePlayer.getItemAtPosition(position);
            ResultUseCase.getInstance().setWhite(player);
            Intent myIntent = new Intent(view.getContext(), SelectBlackPlayerActivity.class);
            startActivity(myIntent);
        });

        chkFavourites.setOnCheckedChangeListener((buttonView, isChecked) -> {
            showPlayers(isChecked);
            Editor editor = getSharedPreferences("SETTINGS", MODE_PRIVATE).edit();
            editor.putBoolean("show_favourites", isChecked);
            editor.apply();
        });
    }

    private void showPlayers(boolean showFavourites) {
        lsvSelectWhitePlayer = findViewById(R.id.lsvSelectWhitePlayer);
        PlayerArrayAdapter adapter = new PlayerArrayAdapter(
                this,
                R.layout.list_item,
                playersToShow(
                        showFavourites,
                        getSharedPreferences("SETTINGS", MODE_PRIVATE)));
        lsvSelectWhitePlayer.setAdapter(adapter);
        lsvSelectWhitePlayer.setFastScrollEnabled(true);
    }
}
