package za.co.sagoclubs;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;

import java.util.Arrays;
import java.util.List;

public class SelectFavouritePlayersActivity extends Activity {

    private ListView lsvSelectFavouritePlayers;
    private Button btnSave;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_favourite_players);

        lsvSelectFavouritePlayers = findViewById(R.id.lsvSelectFavouritePlayers);
        btnSave = findViewById(R.id.btnSave);

        //PlayerArrayAdapter adapter = new PlayerArrayAdapter(this, R.layout.list_item, InternetActions.getPlayerArray());        
        PlayerArrayAdapter adapter = new PlayerArrayAdapter(this, R.layout.multi_select_list_item, InternetActions.getPlayerArray());
        lsvSelectFavouritePlayers.setAdapter(adapter);
        lsvSelectFavouritePlayers.setFastScrollEnabled(true);
        loadSelection();

        btnSave.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSelection();
                finish();
            }
        });
    }

    @Override
    public void onPause() {
        saveSelection();
        super.onPause();
    }

    private void saveSelection() {
        String save = "";
        int count = lsvSelectFavouritePlayers.getAdapter().getCount();
        for (int i = 0; i < count; i++) {
            if (lsvSelectFavouritePlayers.isItemChecked(i)) {
                Player player = (Player) lsvSelectFavouritePlayers.getItemAtPosition(i);
                if (save.length() > 0) {
                    save += "," + player.getId();
                } else {
                    save = player.getId();
                }
            }
        }
        SharedPreferences preferences = getSharedPreferences("SETTINGS", MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putString("favourite_players", save);
        editor.commit();
    }

    private void loadSelection() {
        SharedPreferences preferences = getSharedPreferences("SETTINGS", MODE_PRIVATE);
        String save = preferences.getString("favourite_players", "");
        List<String> items = Arrays.asList(save.split(","));
        int count = lsvSelectFavouritePlayers.getAdapter().getCount();
        for (int i = 0; i < count; i++) {
            Player player = (Player) lsvSelectFavouritePlayers.getItemAtPosition(i);
            if (items.contains(player.getId())) {
                lsvSelectFavouritePlayers.setItemChecked(i, true);
            }
        }
    }
}
