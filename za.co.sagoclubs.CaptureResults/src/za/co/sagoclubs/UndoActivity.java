package za.co.sagoclubs;

import static za.co.sagoclubs.Constants.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import za.co.sagoclubs.ResultUseCase.ResultState;

public class UndoActivity extends AppCompatActivity {
    private TextView loadingStatusView;
    private TextView txtOutput;
    private ScrollView scrollView;
    private Button btnNewResult;
    private Button btnReturnToStart;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "UndoActivity.onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.undo);

        loadingStatusView = findViewById(R.id.txtLoadingStatus);

        TextView txtWhitePlayer = findViewById(R.id.txtWhitePlayer);
        txtWhitePlayer.setText(ResultUseCase.getInstance().getWhite().getName());
        TextView txtBlackPlayer = findViewById(R.id.txtBlackPlayer);
        txtBlackPlayer.setText(ResultUseCase.getInstance().getBlack().getName());

        txtOutput = findViewById(R.id.txtOutput);
        txtOutput.setEnabled(false);
        scrollView = findViewById(R.id.scrollerUndo);

        btnReturnToStart = findViewById(R.id.btnReturnToStart);
        btnNewResult = findViewById(R.id.btnNewResult);

        ResultUseCase.getInstance().getUndoState().observe(this, resultState -> {
            switch (resultState.status()) {
                case Ready -> {
                    showWaitingStatus(resultState,
                            getString(R.string.sending_message));
                    ResultUseCase.getInstance().undoGame();
                }
                case Sending -> showWaitingStatus(resultState,
                        getString(R.string.sending_message));
                case Sent, Fetching -> showPartialStatus(resultState,
                        getString(R.string.partial_message));
                case Complete -> showSuccessStatus(resultState);
                case SendingClientError -> showSendErrorStatus(resultState,
                        getString(R.string.send_client_error_message));
                case SendingAuthorizationError -> showSendErrorStatus(resultState,
                        getString(R.string.send_authorization_error_message));
                case SendingNetworkError -> showSendErrorStatus(resultState,
                        getString(R.string.send_network_error_message));
                case FetchingNetworkError -> showPartialStatus(resultState,
                        getString(R.string.fetch_network_error_message));
                case FetchingAuthorizationError -> showPartialStatus(resultState,
                        getString(R.string.fetch_authorization_error_message));
            }
        });

        btnNewResult.setOnClickListener(v -> {
            ResultUseCase.getInstance().readyForPlayers();
            Intent myIntent = new Intent(v.getContext(), SelectWhitePlayerActivity.class);
            myIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(myIntent);
        });

        btnReturnToStart.setOnClickListener(v -> {
            Intent myIntent = new Intent(v.getContext(), MainActivity.class);
            myIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(myIntent);
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Toast.makeText(this,"BACK is ambiguous. Home?", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void showWaitingStatus(ResultState state, String message) {
        showMessage(message);
        txtOutput.setText(state.output());
        disableContinue();
    }

    private void showPartialStatus(ResultState state, String message) {
        showMessage(message);
        txtOutput.setText(state.output());
        enableContinue();
    }

    private void showSuccessStatus(ResultState state) {
        loadingStatusView.setVisibility(View.GONE);
        txtOutput.setMovementMethod(new ScrollingMovementMethod());
        txtOutput.setText(state.output());
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
        enableContinue();
    }

    private void showSendErrorStatus(ResultState state, String message) {
        showMessage(message);
        txtOutput.setText(state.output());
        enableContinue();
    }

    private void showMessage(String message) {
        loadingStatusView.setText(message);
        loadingStatusView.setVisibility(View.VISIBLE);
    }

    private void disableContinue() {
        btnNewResult.setVisibility(View.INVISIBLE);
        btnReturnToStart.setVisibility(View.INVISIBLE);
    }

    private void enableContinue() {
        btnNewResult.setVisibility(View.VISIBLE);
        btnReturnToStart.setVisibility(View.VISIBLE);
    }
}
