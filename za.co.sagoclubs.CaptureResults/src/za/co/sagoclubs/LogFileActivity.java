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
                        case Processing -> showStatus(logRecord,
                                getString(R.string.loading_message));
                        case Done -> {
                            loadingStatusView.setVisibility(View.GONE);
                            txtPlayer.setText(logRecord.player().getName());
                            txtOutput.setMovementMethod(new ScrollingMovementMethod());
                            txtOutput.setText(logRecord.logFile());
                            scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
                        }
                        case NetworkError -> showStatus(logRecord,
                                getString(R.string.network_error_message));
                        case PlayerError -> showStatus(logRecord,
                                getString(R.string.player_error_message));
                        case AuthorizationError -> showStatus(logRecord,
                                getString(R.string.authorization_error_message));
                        default -> {
                            loadingStatusView.setVisibility(View.GONE);
                            txtPlayer.setText("");
                            txtOutput.setText("");
                        }
                    }
                }
        );

        if (savedInstanceState == null) {
            Log.d(TAG, "Calling server to get player logfile");
            LogFileUseCase.getInstance().fetchLogFile();
        }
    }

    private void showStatus(LogFileUseCase.LogRecord logRecord, String message) {
        loadingStatusView.setText(message);
        loadingStatusView.setVisibility(View.VISIBLE);
        txtPlayer.setText(logRecord.player().getName());
        txtOutput.setText(logRecord.logFile());
    }
}
