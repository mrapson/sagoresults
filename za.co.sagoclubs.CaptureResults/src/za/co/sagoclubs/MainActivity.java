package za.co.sagoclubs;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;

public class MainActivity extends Activity {
	private Button btnCaptureResult;
	private Button btnDisplayLogFile;
	private boolean onCreateCalled = false;
	private final UserData userData = UserData.getInstance();

	@Override
	public void onResume() {
		super.onResume();
		if (onCreateCalled) {
			btnDisplayLogFile.setVisibility(userData.isGuestUser() ? View.INVISIBLE : View.VISIBLE);
			btnCaptureResult.setVisibility(userData.isGuestUser() ? View.INVISIBLE : View.VISIBLE);
		}
	}
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        //This is not recommended, but it's a quick way to avoid all the network exceptions
        //Android 10+ doesn't allow network stuff on the UI thread
        //This would require a significant re-architecture of the application
        //Not justified just at the present
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);
        
        Result.setResultState(ResultState.Complete);
        
		SharedPreferences preferences = getSharedPreferences("SETTINGS", MODE_PRIVATE);
		userData.setUsername(preferences.getString("username", ""));
		userData.setPassword(preferences.getString("password", ""));
		InternetActions.forcePlayerArrayReload();

		Button btnSettings = findViewById(R.id.btnSettings);
		btnSettings.setOnClickListener(v -> {
			Intent myIntent = new Intent(v.getContext(), SettingsActivity.class);
			startActivityForResult(myIntent, 0);
		});

        btnDisplayLogFile = findViewById(R.id.btnDisplayLogFile);
		btnDisplayLogFile.setVisibility(userData.isGuestUser() ? View.INVISIBLE : View.VISIBLE);
		btnDisplayLogFile.setOnClickListener(v -> {
			Intent myIntent = new Intent(v.getContext(), SelectPlayerActivity.class);
			startActivityForResult(myIntent, 0);
		});

		btnCaptureResult = findViewById(R.id.btnCaptureResult);
		btnCaptureResult.setVisibility(userData.isGuestUser() ? View.INVISIBLE : View.VISIBLE);
		btnCaptureResult.setOnClickListener(v -> {
			Result.setResultState(ResultState.Enter);
			Intent myIntent = new Intent(v.getContext(), SelectWhitePlayerActivity.class);
			startActivityForResult(myIntent, 0);
		});

		Button btnPlayerRatings = findViewById(R.id.btnPlayerRatings);
		btnPlayerRatings.setEnabled(true);
		btnPlayerRatings.setOnClickListener(v -> {
			Intent myIntent = new Intent(v.getContext(), PlayerRatingsActivity.class);
			startActivityForResult(myIntent, 0);
		});

		int width = btnCaptureResult.getWidth();
		if (btnDisplayLogFile.getWidth() > width) {
			width = btnDisplayLogFile.getWidth();
		}
		btnCaptureResult.setWidth(width);
		btnDisplayLogFile.setWidth(width);
		btnSettings.setWidth(width);
		
		onCreateCalled = true;
    }
}
