package za.co.sagoclubs;

import static za.co.sagoclubs.InternetActions.getRefreshPage;
import static za.co.sagoclubs.InternetActions.undoResult;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.io.IOException;

import za.co.sagoclubs.ResultUseCase.Status;

public class UndoActivity extends Activity {

    private TextView txtOutput;
    private ScrollView scrollView;
    private Button btnNewResult;
    private Button btnReturnToStart;
    private ProgressDialog dialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.undo);

        dialog = new ProgressDialog(this);

        txtOutput = findViewById(R.id.txtOutput);
        txtOutput.setEnabled(false);

        scrollView = findViewById(R.id.scrollerUndo);

        btnReturnToStart = findViewById(R.id.btnReturnToStart);
        btnNewResult = findViewById(R.id.btnNewResult);

        if (ResultUseCase.getInstance().getStatus() == Status.Enter) {
            btnNewResult.setVisibility(View.INVISIBLE);
            btnReturnToStart.setVisibility(View.INVISIBLE);
            dialog.setMessage("Sending undo to server...");
            dialog.setIndeterminate(true);
            dialog.setCancelable(false);
            dialog.show();
            new UndoResultTask().execute();
        }

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
            Toast.makeText(this, "BACK is ambiguous. Home?", Toast.LENGTH_SHORT).show();
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

    private class UndoResultTask extends AsyncTask<Void, Void, String> {
        protected String doInBackground(Void... v) {
            setProgressBarIndeterminateVisibility(true);

            try {
                undoResult(ResultUseCase.getInstance().constructUndoUriOptions());
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
            btnNewResult.setVisibility(View.VISIBLE);
            btnReturnToStart.setVisibility(View.VISIBLE);
        }
    }
}
