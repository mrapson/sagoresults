package za.co.sagoclubs;

import static za.co.sagoclubs.PlayerUseCase.playersToShow;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

public class SelectBlackPlayerActivity extends AppCompatActivity {
    private ListView lsvSelectBlackPlayer;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_black_player);

        lsvSelectBlackPlayer = findViewById(R.id.lsvSelectBlackPlayer);
        CheckBox chkFavourites = findViewById(R.id.chkFavourites);

        SharedPreferences preferences = getSharedPreferences("SETTINGS", MODE_PRIVATE);
        boolean showFavourites = preferences.getBoolean("show_favourites", false);
        chkFavourites.setChecked(showFavourites);
        PlayerUseCase.getInstance()
                .getPlayerData()
                .observe(this, (playerData) -> showPlayers(showFavourites));

        lsvSelectBlackPlayer.setOnItemClickListener((parent, view, position, id) -> {
            Player player = (Player) lsvSelectBlackPlayer.getItemAtPosition(position);
            Result.setBlack(player);
            Intent myIntent = new Intent(view.getContext(), CaptureDetailActivity.class);
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
        lsvSelectBlackPlayer = findViewById(R.id.lsvSelectBlackPlayer);
        PlayerArrayAdapter adapter = new PlayerArrayAdapter(
                this,
                R.layout.list_item,
                playersToShow(
                        showFavourites,
                        getSharedPreferences("SETTINGS", MODE_PRIVATE)));
        lsvSelectBlackPlayer.setAdapter(adapter);
        lsvSelectBlackPlayer.setFastScrollEnabled(true);
    }
}
