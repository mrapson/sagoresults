package za.co.sagoclubs;

import static za.co.sagoclubs.PlayerUseCase.playersToShow;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

public class SelectLogPlayerActivity extends AppCompatActivity {

    private ListView lsvSelectPlayer;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_log_player);

        lsvSelectPlayer = findViewById(R.id.lsvSelectPlayer);
        CheckBox chkFavourites = findViewById(R.id.chkFavourites);

        SharedPreferences preferences = getSharedPreferences("SETTINGS", MODE_PRIVATE);
        boolean showFavourites = preferences.getBoolean("show_favourites", false);
        chkFavourites.setChecked(showFavourites);
        PlayerUseCase.getInstance()
                .getPlayerData()
                .observe(this, (playerData) -> showPlayers(showFavourites));

        lsvSelectPlayer.setOnItemClickListener((parent, view, position, id) -> {
            Player player = (Player) lsvSelectPlayer.getItemAtPosition(position);
            LogFileUseCase.getInstance().prepareRequest(player, LogFileUseCase.Requester.HandleLookup);
            Intent myIntent = new Intent(view.getContext(), LogFileActivity.class);
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
        lsvSelectPlayer = findViewById(R.id.lsvSelectPlayer);
        PlayerArrayAdapter adapter = new PlayerArrayAdapter(
                this,
                R.layout.list_item,
                playersToShow(
                        showFavourites,
                        getSharedPreferences("SETTINGS", MODE_PRIVATE)));
        lsvSelectPlayer.setAdapter(adapter);
        lsvSelectPlayer.setFastScrollEnabled(true);
    }
}
