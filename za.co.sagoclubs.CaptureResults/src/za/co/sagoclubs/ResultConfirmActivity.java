package za.co.sagoclubs;

import static za.co.sagoclubs.Constants.TAG;
import static za.co.sagoclubs.InternetActions.getRefreshPage;
import static za.co.sagoclubs.InternetActions.sendResult;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.io.IOException;

import za.co.sagoclubs.ResultUseCase.Status;

public class ResultConfirmActivity extends Activity {
    private TextView txtOutput;
    private ScrollView scrollView;
    private Button btnUndo;
    private Button btnNewResult;
    private Button btnReturnToStart;
    private ProgressDialog dialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "ResultConfirmActivity.onCreate");

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        dialog = new ProgressDialog(this);

        setContentView(R.layout.result_confirm);

        txtOutput = findViewById(R.id.txtOutput);
        txtOutput.setEnabled(false);

        TextView txtWhitePlayer = findViewById(R.id.txtWhitePlayer);
        txtWhitePlayer.setText(ResultUseCase.getInstance().getWhite().getName());
        TextView txtBlackPlayer = findViewById(R.id.txtBlackPlayer);
        txtBlackPlayer.setText(ResultUseCase.getInstance().getBlack().getName());

        scrollView = findViewById(R.id.scrollerConfirm);

        btnUndo = findViewById(R.id.btnUndo);
        btnNewResult = findViewById(R.id.btnNewResult);
        btnReturnToStart = findViewById(R.id.btnReturnToStart);


        if (ResultUseCase.getInstance().getStatus() == Status.Enter) {
            btnUndo.setVisibility(View.INVISIBLE);
            btnNewResult.setVisibility(View.INVISIBLE);
            btnReturnToStart.setVisibility(View.INVISIBLE);
            dialog.setMessage("Sending result to server...");
            dialog.setIndeterminate(true);
            dialog.setCancelable(false);
            dialog.show();
            new SaveResultTask().execute();
        }

        btnUndo.setOnClickListener(v -> {
            ResultUseCase.getInstance().setStatus(Status.Enter);
            Intent myIntent = new Intent(v.getContext(), UndoActivity.class);
            startActivityForResult(myIntent, 0);
        });

        btnNewResult.setOnClickListener(v -> {
            ResultUseCase.getInstance().setStatus(Status.Enter);
            Intent myIntent = new Intent(v.getContext(), SelectWhitePlayerActivity.class);
            myIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivityForResult(myIntent, 0);
        });


        btnReturnToStart.setOnClickListener(v -> {
            Intent myIntent = new Intent(v.getContext(), MainActivity.class);
            myIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivityForResult(myIntent, 0);
        });

        if (savedInstanceState != null) {
            restoreProgress(savedInstanceState);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Toast.makeText(this, "BACK is ambiguous. Home or Undo?", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle saveState) {
        super.onSaveInstanceState(saveState);
        saveState.putString("output", txtOutput.getText().toString());
    }

    private void restoreProgress(Bundle savedInstanceState) {
        String output = savedInstanceState.getString("output");
        if (output != null) {
            txtOutput.setMovementMethod(new ScrollingMovementMethod());
            txtOutput.setText(output);
            scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
        }
    }

    private class SaveResultTask extends AsyncTask<Void, Void, String> {
        protected String doInBackground(Void... v) {
            setProgressBarIndeterminateVisibility(true);
            try {
                sendResult(ResultUseCase.getInstance().constructConfirmUriOptions());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            return getRefreshPage();
        }

        protected void onPostExecute(String result) {
            setProgressBarIndeterminateVisibility(false);
            dialog.dismiss();

            txtOutput.setMovementMethod(new ScrollingMovementMethod());
            txtOutput.setText(result);
            scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));

            ResultUseCase.getInstance().setStatus(ResultUseCase.Status.Complete);
            btnUndo.setVisibility(View.VISIBLE);
            btnNewResult.setVisibility(View.VISIBLE);
            btnReturnToStart.setVisibility(View.VISIBLE);
        }
    }
}
