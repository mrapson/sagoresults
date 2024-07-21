package za.co.sagoclubs;

import static za.co.sagoclubs.Constants.TAG;
import static za.co.sagoclubs.InternetActions.getPlayerLog;
import static za.co.sagoclubs.InternetActions.getRatingsPlayerLog;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;

public class LogFileActivity extends Activity {

    private TextView txtOutput;
    private TextView txtPlayer;
    private ScrollView scrollView;

    private ProgressDialog dialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.logfile);

        dialog = new ProgressDialog(this);

        txtOutput = findViewById(R.id.txtOutput);
        txtOutput.setEnabled(false);
        txtPlayer = findViewById(R.id.txtPlayer);

        scrollView = findViewById(R.id.SCROLLER_ID);

        if (savedInstanceState != null) {
            restoreProgress(savedInstanceState);
        } else {
            Log.d(TAG, "Calling server to get player logfile");
            txtPlayer.setText(Result.logfile.getName());
            dialog.setMessage("Fetching log file...");
            dialog.setIndeterminate(true);
            dialog.setCancelable(false);
            dialog.show();
            new LogFileTask().execute();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle saveState) {
        super.onSaveInstanceState(saveState);
        saveState.putString("output", txtOutput.getText().toString());
        saveState.putString("player", txtPlayer.getText().toString());
    }

    private void restoreProgress(Bundle savedInstanceState) {
        String output = savedInstanceState.getString("output");
        if (output != null) {
            txtOutput.setMovementMethod(new ScrollingMovementMethod());
            txtOutput.setText("");
            txtOutput.append(output);
            scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
        }
        String player = savedInstanceState.getString("player");
        if (player != null) {
            txtPlayer.setText(player);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    private class LogFileTask extends AsyncTask<Void, Void, String> {
        protected String doInBackground(Void... v) {
            setProgressBarIndeterminateVisibility(true);

            ComponentName callingActivity = getCallingActivity();
            if (callingActivity != null
                    && PlayerRatingsActivity.class.getName().equals(callingActivity.getClassName())) {
                return getRatingsPlayerLog(Result.logfile.getId());
            } else {
                return getPlayerLog(Result.logfile.getId());
            }
        }

        protected void onPostExecute(String result) {
            setProgressBarIndeterminateVisibility(false);
            dialog.hide();
            txtOutput.setMovementMethod(new ScrollingMovementMethod());
            txtOutput.setText("");
            txtOutput.append(result);
            scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
        }
    }
}
