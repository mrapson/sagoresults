package za.co.sagoclubs;

import static za.co.sagoclubs.Constants.TAG;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class LogFileActivity extends AppCompatActivity {

    private TextView txtOutput;
    private TextView txtPlayer;
    private ScrollView scrollView;
    private TextView loadingStatusView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.logfile);

        txtOutput = findViewById(R.id.txtOutput);
        txtOutput.setEnabled(false);
        txtPlayer = findViewById(R.id.txtPlayer);

        scrollView = findViewById(R.id.SCROLLER_ID);

        loadingStatusView = findViewById(R.id.txtLoadingStatus);

        LogFileUseCase.getInstance().getLogRecord().observe(this, logRecord -> {
                    switch (logRecord.status()) {
                        case Processing -> {
                            loadingStatusView.setText(getString(R.string.loading_message));
                            loadingStatusView.setVisibility(View.VISIBLE);
                            txtOutput.setText(logRecord.logFile());
                        }
                        case Error -> {
                            loadingStatusView.setText(getString(R.string.network_error_message));
                            loadingStatusView.setVisibility(View.VISIBLE);
                            txtOutput.setText(logRecord.logFile());
                        }
                        case Done -> {
                            txtOutput.setMovementMethod(new ScrollingMovementMethod());
                            txtOutput.setText(logRecord.logFile());
                            scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
                            loadingStatusView.setVisibility(View.GONE);
                        }
                        default -> {
                            loadingStatusView.setVisibility(View.GONE);
                            txtOutput.setText("");
                        }
                    }
                }
        );

        if (savedInstanceState != null) {
            restoreProgress(savedInstanceState);
        } else {
            Log.d(TAG, "Calling server to get player logfile");
            txtPlayer.setText(LogFileUseCase.getInstance().getPlayer().getName());

            LogFileUseCase.getInstance().fetchLogFile();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle saveState) {
        super.onSaveInstanceState(saveState);
        saveState.putString("player", txtPlayer.getText().toString());
    }

    private void restoreProgress(Bundle savedInstanceState) {
        String player = savedInstanceState.getString("player");
        if (player != null) {
            txtPlayer.setText(player);
        }
    }
}
