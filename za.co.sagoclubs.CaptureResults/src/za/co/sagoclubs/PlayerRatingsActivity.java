package za.co.sagoclubs;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

public class PlayerRatingsActivity extends Activity {
	private ProgressDialog dialog;
	private ListView listView;
	private Button btnSortByRank;
	private Button btnSortByName;
	private PlayerSortOrder preferredOrder = PlayerSortOrder.SORT_BY_RANK;

	@Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player_ratings);

        btnSortByRank = findViewById(R.id.btnSortByRank);
        btnSortByName = findViewById(R.id.btnSortByName);
        listView = findViewById(R.id.listView);

		updateList();

		btnSortByName.setOnClickListener(v -> {
			if (preferredOrder == PlayerSortOrder.SORT_BY_RANK) {
				preferredOrder = PlayerSortOrder.SORT_BY_NAME;
				updateList();
			}
		});

		btnSortByRank.setOnClickListener(v -> {
			if (preferredOrder == PlayerSortOrder.SORT_BY_NAME) {
				preferredOrder = PlayerSortOrder.SORT_BY_RANK;
				updateList();
			}
		});
	}

	private void updateList() {
		dialog = new ProgressDialog(this);
		dialog.setMessage("Retrieving player ratings...");
		dialog.setIndeterminate(true);
		dialog.setCancelable(true);
		dialog.show();
    	new PlayerRatingsTask().execute(this);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		if (dialog!=null) {
			dialog.dismiss();
		}
	}
	
	private class PlayerRatingsTask extends AsyncTask<Context, Void, Player[]> {
		private Context context;
		
		protected Player[] doInBackground(Context... v) {
			context = v[0];
			setProgressBarIndeterminateVisibility(true);
			return InternetActions.getPlayerRatingsArray(PlayerSortOrder.SORT_BY_NAME);
	    }

		public OnItemClickListener playerItemClickListener = new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Player p = (Player)listView.getItemAtPosition(position);
				Result.setLogFile(p);
	            Intent myIntent = new Intent(view.getContext(), LogFileActivity.class);
	            startActivityForResult(myIntent, 0);
			}
		};
		
	    protected void onPostExecute(Player[] players) {
	    	setProgressBarIndeterminateVisibility(false);
	    	dialog.hide();
	        PlayerRatingArrayAdapter adapter = new PlayerRatingArrayAdapter(context,
	                R.layout.player_rating_list_item, players);
	        listView.setAdapter(adapter);
	        listView.setOnItemClickListener(playerItemClickListener);
	        
	    }
	}
}
