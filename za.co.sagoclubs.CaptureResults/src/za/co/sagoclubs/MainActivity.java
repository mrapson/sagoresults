package za.co.sagoclubs;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

public class MainActivity extends AppCompatActivity {
    private final UserData userData = UserData.getInstance();

    private Button btnCaptureResult;
    private Button btnDisplayLogFile;

    @Override
    public void onResume() {
        super.onResume();
        int visibility = userData.isGuestUser() ? View.INVISIBLE : View.VISIBLE;
        findViewById(R.id.btnDisplayLogFile).setVisibility(visibility);
        findViewById(R.id.btnCaptureResult).setVisibility(visibility);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        Result.setResultState(ResultState.Complete);

        Button btnPlayerRatings = findViewById(R.id.btnPlayerRatings);
        btnPlayerRatings.setEnabled(true);
        btnPlayerRatings.setOnClickListener(v -> {
            Intent myIntent = new Intent(v.getContext(), PlayerRatingsActivity.class);
            startActivity(myIntent);
        });

        btnCaptureResult = findViewById(R.id.btnCaptureResult);
        btnDisplayLogFile = findViewById(R.id.btnDisplayLogFile);

        PlayerUseCase.getInstance()
                .getPlayerData()
                .observe(this, (playerData -> {
                    boolean showButtons = !userData.isGuestUser();
                    boolean clickable = playerData != null && playerData.hasData();
                    setupButton(
                            btnCaptureResult,
                            AppCompatResources.getDrawable(this, R.drawable.green_button),
                            v -> {
                                Result.setResultState(ResultState.Enter);
                                Intent myIntent = new Intent(v.getContext(),
                                        SelectWhitePlayerActivity.class);
                                startActivity(myIntent);
                            },
                            showButtons,
                            clickable
                    );
                    setupButton(
                            btnDisplayLogFile,
                            AppCompatResources.getDrawable(this, R.drawable.purple_button),
                            v -> {
                                Intent myIntent = new Intent(v.getContext(),
                                        SelectLogPlayerActivity.class);
                                startActivity(myIntent);
                            },
                            showButtons,
                            clickable
                    );
                }));

        Button btnSettings = findViewById(R.id.btnSettings);
        btnSettings.setOnClickListener(v -> {
            Intent myIntent = new Intent(v.getContext(), SettingsActivity.class);
            startActivity(myIntent);
        });
    }

    private void setupButton(Button button, Drawable activeBackground,
                             View.OnClickListener listener,
                             boolean visible, boolean clickable) {
        button.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);

        if (clickable) {
            button.setBackground(activeBackground);
            button.setOnClickListener(listener);
        } else {
            Drawable greyButton = AppCompatResources.getDrawable(
                    this,
                    R.drawable.grey_button);
            button.setBackground(greyButton);
            button.setClickable(false);
        }
    }
}
