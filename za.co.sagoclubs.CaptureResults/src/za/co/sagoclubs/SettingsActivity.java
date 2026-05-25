package za.co.sagoclubs;

import static za.co.sagoclubs.Constants.TAG;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

public class SettingsActivity extends AppCompatActivity {

    private TextView txtUsername;
    private TextView txtPassword;
    private CheckBox chkUseTestSite;
    private Button btnSelectFavouritePlayers;
    private boolean changed = false;
    private final UserData userData = UserData.getInstance();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        txtUsername = findViewById(R.id.txtUsername);
        txtPassword = findViewById(R.id.txtPassword);
        chkUseTestSite = findViewById(R.id.chkUseTestSite);

        loadSettings();

        btnSelectFavouritePlayers = findViewById(R.id.btnSelectFavouritePlayers);
        PlayerUseCase.getInstance()
                .getPlayerData()
                .observe(this, this::setupSelectFavouritesButton);

        txtUsername.setOnKeyListener((v, keyCode, event) -> {
            Log.d(TAG, "SettingsActivity.txtUsername.OnClick");
            changed = true;
            return false;
        });

        CheckBox showPassword = findViewById(R.id.chkShowPassword);
        showPassword.setOnCheckedChangeListener((arg0, arg1) -> {
            if (!arg1) {
                txtPassword.setTransformationMethod(new PasswordTransformationMethod());
            } else {
                txtPassword.setTransformationMethod(null);
            }
            txtPassword.refreshDrawableState();
        });

        chkUseTestSite.setOnCheckedChangeListener((arg0, arg1) -> {
            clearPreviousUser();
            changed = true;
            saveSettings();
            changed = false;
        });

        Button doneButton = findViewById(R.id.DoneButton);
        doneButton.setOnClickListener(arg0 -> {
            changed = true;
            saveSettings();
            changed = false;
        });
    }

    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        saveSettings();
        changed = false;
        super.onPause();
    }

    private void clearPreviousUser() {
        userData.clearAuthorization();
        PlayerUseCase.getInstance().clearPlayerData();
    }

    @SuppressLint("ApplySharedPref")
    private void saveSettings() {
        if (changed) {
            SharedPreferences preferences = getSharedPreferences("SETTINGS", MODE_PRIVATE);
            Editor editor = preferences.edit();
            editor.putString("username", txtUsername.getText().toString().trim());
            editor.putString("password", txtPassword.getText().toString().trim());
            editor.putString("use_test", String.valueOf(chkUseTestSite.isChecked()));
            editor.commit();
            userData.setUsername(preferences.getString("username", UserData.GUEST_USER));
            userData.setPassword(preferences.getString("password", UserData.GUEST_PASS));
            userData.setTestSiteUser(parseUseTest(preferences));

            if (!userData.isGuestUser()) {
                RankApplication.getApp()
                        .getAuthentication()
                        .settingsLogin();
            }

            setupSelectFavouritesButton(PlayerUseCase.getInstance().getPlayerData().getValue());
        }
    }

    private void loadSettings() {
        SharedPreferences preferences = getSharedPreferences("SETTINGS", MODE_PRIVATE);
        txtUsername.setText(preferences.getString("username", UserData.GUEST_USER));
        txtPassword.setText(preferences.getString("password", UserData.GUEST_PASS));
        chkUseTestSite.setChecked(parseUseTest(preferences));
    }

    private boolean parseUseTest(SharedPreferences preferences) {
        String useTest = preferences.getString("use_test", String.valueOf(false));
        return Boolean.parseBoolean(useTest);
    }

    private void setupSelectFavouritesButton(@Nullable PlayerUseCase.PlayerData playerData) {
        int showButton = userData.isGuestUser() ? View.INVISIBLE : View.VISIBLE;
        btnSelectFavouritePlayers.setVisibility(showButton);

        if (playerData != null && playerData.hasData()) {
            Drawable blueButton = AppCompatResources.getDrawable(
                    this,
                    R.drawable.blue_button);
            btnSelectFavouritePlayers.setBackground(blueButton);

            btnSelectFavouritePlayers.setOnClickListener(v -> {
                Intent myIntent = new Intent(v.getContext(),
                        SelectFavouritePlayersActivity.class);
                startActivity(myIntent);
            });
        } else {
            Drawable greyButton = AppCompatResources.getDrawable(
                    this,
                    R.drawable.grey_button);
            btnSelectFavouritePlayers.setBackground(greyButton);
            btnSelectFavouritePlayers.setClickable(false);
        }
    }
}
