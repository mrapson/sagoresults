package za.co.sagoclubs;

import static za.co.sagoclubs.Constants.TAG;
import static za.co.sagoclubs.InternetActions.getPlayerLog;
import static za.co.sagoclubs.InternetActions.getRatingsPlayerLog;

import android.app.Activity;
import android.content.ComponentName;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class LogFileActivity extends Activity {

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

        if (savedInstanceState != null) {
            restoreProgress(savedInstanceState);
        } else {
            Log.d(TAG, "Calling server to get player logfile");
            txtPlayer.setText(Result.getLogPlayer().getName());
            loadingStatusView.setText(getString(R.string.loading_message));
            loadingStatusView.setVisibility(View.VISIBLE);

            // TODO deal with thread running when we change views
            CompletableFuture<String> completableFuture = new CompletableFuture<>();
            Runnable runnable = fetchLogfile(completableFuture, getCallingActivity());
            completableFuture.whenComplete(this::onPostExecute);
            new Thread(runnable).start();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle saveState) {
        super.onSaveInstanceState(saveState);
        saveState.putString("player", txtPlayer.getText().toString());
        saveState.putString("output", txtOutput.getText().toString());
    }

    private void restoreProgress(Bundle savedInstanceState) {
        String player = savedInstanceState.getString("player");
        if (player != null) {
            txtPlayer.setText(player);
        }
        String output = savedInstanceState.getString("output");
        if (output != null) {
            txtOutput.setMovementMethod(new ScrollingMovementMethod());
            txtOutput.setText("");
            txtOutput.append(output);
            scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
        }
    }

    private static Runnable fetchLogfile(CompletableFuture<String> completableFuture,
                                         ComponentName callingActivity) {
        return () -> {
            if (callingActivity != null
                    && PlayerRatingsActivity.class.getName().equals(callingActivity.getClassName())) {
                completableFuture.complete(getRatingsPlayerLog(Result.getLogPlayer().getId()));
            } else {
                completableFuture.complete(getPlayerLog(Result.getLogPlayer().getId()));
            }
        };
    }

    protected void onPostExecute(String result, Throwable e) {
        Handler logUpdateHandler = new Handler(Looper.getMainLooper());
        if (e == null) {
            logUpdateHandler.post(() -> handleSuccess(result));
        } else {
            String error_message = e.getCause() instanceof IOException
                    ? getString(R.string.network_error_message)
                    : getString(R.string.site_error_message);
            logUpdateHandler.post(() -> handleError(error_message));
        }
    }

    private void handleError(String message) {
        loadingStatusView.setText(message);
    }

    private void handleSuccess(String log) {
        txtOutput.setMovementMethod(new ScrollingMovementMethod());
        txtOutput.setText("");
        txtOutput.append(log);
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
        loadingStatusView.setVisibility(View.GONE);
    }
}
