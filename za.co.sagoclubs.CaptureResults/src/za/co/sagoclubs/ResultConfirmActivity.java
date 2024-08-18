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

public class ResultConfirmActivity extends AppCompatActivity {
    private TextView loadingStatusView;
    private TextView txtOutput;
    private ScrollView scrollView;
    private Button btnUndo;
    private Button btnNewResult;
    private Button btnReturnToStart;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "ResultConfirmActivity.onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.result_confirm);

        loadingStatusView = findViewById(R.id.txtLoadingStatus);

        TextView txtWhitePlayer = findViewById(R.id.txtWhitePlayer);
        txtWhitePlayer.setText(ResultUseCase.getInstance().getWhite().getName());
        TextView txtBlackPlayer = findViewById(R.id.txtBlackPlayer);
        txtBlackPlayer.setText(ResultUseCase.getInstance().getBlack().getName());

        txtOutput = findViewById(R.id.txtOutput);
        txtOutput.setEnabled(false);
        scrollView = findViewById(R.id.scrollerConfirm);

        btnUndo = findViewById(R.id.btnUndo);
        btnNewResult = findViewById(R.id.btnNewResult);
        btnReturnToStart = findViewById(R.id.btnReturnToStart);

        ResultUseCase.getInstance().getSubmitState().observe(this, resultState -> {
            switch (resultState.status()) {
                case Ready -> {
                    showWaitingStatus(resultState,
                            getString(R.string.sending_message));
                    ResultUseCase.getInstance().submitGame();
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

        btnUndo.setOnClickListener(v -> {
            ResultUseCase.getInstance().prepareUndo();
            Intent myIntent = new Intent(v.getContext(), UndoActivity.class);
            startActivity(myIntent);
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
            Toast.makeText(this, "BACK is ambiguous. Home or Undo?", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void showWaitingStatus(ResultUseCase.ResultState state, String message) {
        showMessage(message);
        txtOutput.setText(state.output());
        disableUndo();
        disableContinue();
    }

    private void showPartialStatus(ResultUseCase.ResultState state, String message) {
        showMessage(message);
        txtOutput.setText(state.output());
        enableUndo();
        enableContinue();
    }

    private void showSuccessStatus(ResultUseCase.ResultState state) {
        loadingStatusView.setVisibility(View.GONE);
        txtOutput.setMovementMethod(new ScrollingMovementMethod());
        txtOutput.setText(state.output());
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
        enableUndo();
        enableContinue();
    }

    private void showSendErrorStatus(ResultUseCase.ResultState state, String message) {
        showMessage(message);
        txtOutput.setText(state.output());
        disableUndo();
        enableContinue();
    }

    private void showMessage(String message) {
        loadingStatusView.setText(message);
        loadingStatusView.setVisibility(View.VISIBLE);
    }

    private void disableUndo() {
        btnUndo.setVisibility(View.INVISIBLE);
    }

    private void enableUndo() {
        btnUndo.setVisibility(View.VISIBLE);
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
