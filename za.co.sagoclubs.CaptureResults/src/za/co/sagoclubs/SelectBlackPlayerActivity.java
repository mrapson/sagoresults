package za.co.sagoclubs;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;

public class SelectBlackPlayerActivity extends Activity {

    private ListView lsvSelectBlackPlayer;
    private CheckBox chkFavourites;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_black_player);

        lsvSelectBlackPlayer = findViewById(R.id.lsvSelectBlackPlayer);
        chkFavourites = findViewById(R.id.chkFavourites);

        SharedPreferences preferences = getSharedPreferences("SETTINGS", MODE_PRIVATE);
        boolean showFavourites = preferences.getBoolean("show_favourites", false);
        chkFavourites.setChecked(showFavourites);
        chooseWhatToShow(showFavourites);

        lsvSelectBlackPlayer.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Player player = (Player) lsvSelectBlackPlayer.getItemAtPosition(position);
                Result.setBlack(player);
                Intent myIntent = new Intent(view.getContext(), CaptureDetailActivity.class);
                startActivityForResult(myIntent, 0);
            }
        });

        chkFavourites.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                chooseWhatToShow(isChecked);
                SharedPreferences preferences = getSharedPreferences("SETTINGS", MODE_PRIVATE);
                Editor editor = preferences.edit();
                editor.putBoolean("show_favourites", isChecked);
                editor.commit();
            }
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
        lsvSelectBlackPlayer = findViewById(R.id.lsvSelectBlackPlayer);
        PlayerArrayAdapter adapter = new PlayerArrayAdapter(this, R.layout.list_item, InternetActions.getPlayerArray());
        lsvSelectBlackPlayer.setAdapter(adapter);
        lsvSelectBlackPlayer.setFastScrollEnabled(true);
    }

    private void showFavourites() {
        lsvSelectBlackPlayer = findViewById(R.id.lsvSelectBlackPlayer);
        SharedPreferences preferences = getSharedPreferences("SETTINGS", MODE_PRIVATE);
        PlayerArrayAdapter adapter = new PlayerArrayAdapter(this, R.layout.list_item, InternetActions.getFavouritePlayers(preferences));
        lsvSelectBlackPlayer.setAdapter(adapter);
        lsvSelectBlackPlayer.setFastScrollEnabled(true);
    }
}
