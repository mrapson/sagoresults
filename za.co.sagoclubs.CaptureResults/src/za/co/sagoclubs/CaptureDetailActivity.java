package za.co.sagoclubs;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

import za.co.sagoclubs.ResultUseCase.GameDetails;
import za.co.sagoclubs.ResultUseCase.Weight;
import za.co.sagoclubs.ResultUseCase.Winner;

public class CaptureDetailActivity extends FragmentActivity
        implements DatePickerDialog.OnDateSetListener {
    private EditText txtDate;
    private EditText txtKomi;
    private EditText txtNotes;
    private Spinner spinnerWeight;
    private Spinner spinnerHandicap;
    private Button btnChangeDate;
    private Button btnSaveResult;
    private Button btnPreviousNotes;
    private RadioButton radioWhite;
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail);

        btnChangeDate = findViewById(R.id.btnChangeDate);
        btnSaveResult = findViewById(R.id.btnSaveResult);
        btnPreviousNotes = findViewById(R.id.btnPreviousNotes);
        txtKomi = findViewById(R.id.txtKomi);
        txtDate = findViewById(R.id.txtDate);
        txtNotes = findViewById(R.id.txtNotes);
        radioWhite = findViewById(R.id.radioWhite);
        RadioButton radioBlack = findViewById(R.id.radioBlack);
        spinnerWeight = findViewById(R.id.spinnerWeight);
        spinnerHandicap = findViewById(R.id.spinnerHandicap);

        addChangeDateButtonListener();
        addPreviousNotesButtonListener();
        addSaveResultButtonListener();
        addSpinnerHandicapOnItemSelectedListener();

        txtKomi.setText("6.5");

        ResultUseCase resultUseCase = ResultUseCase.getInstance();
        radioWhite.setText(getString(R.string.white_radio, resultUseCase.getWhite().getName()));
        radioBlack.setText(getString(R.string.black_radio, resultUseCase.getBlack().getName()));

        spinnerWeight.setSelection(Weight.club.ordinal());

        final LocalDate date = LocalDate.now();
        ResultUseCase.getInstance().setDate(date);
        txtDate.setText(date.format(dateTimeFormatter));
    }

    public void addChangeDateButtonListener() {
        btnChangeDate.setOnClickListener(v -> new DatePickerFragment()
                .show(getSupportFragmentManager(), "datePicker"));
    }

    public void addPreviousNotesButtonListener() {
        btnPreviousNotes.setOnClickListener(v -> {
            String notes = getSharedPreferences("SETTINGS", MODE_PRIVATE)
                    .getString("notes", "");
            if (notes.length() > 0) {
                txtNotes.setText(notes);
            }
        });
    }

    public void addSpinnerHandicapOnItemSelectedListener() {
        spinnerHandicap.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (spinnerHandicap.getSelectedItemPosition() > 0) {
                    txtKomi.setText("0.5");
                } else {
                    txtKomi.setText("6.5");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }
        });
    }

    public void addSaveResultButtonListener() {
        btnSaveResult.setOnClickListener(v -> {
            saveResult();
            Intent myIntent = new Intent(v.getContext(), ResultConfirmActivity.class);
            startActivity(myIntent);
            finish();
        });
    }

    private void saveResult() {
        String notes = txtNotes.getText().toString();
        if (notes.length() > 0) {
            getSharedPreferences("SETTINGS", MODE_PRIVATE).edit()
                    .putString("notes", notes)
                    .apply();
        }
        GameDetails gameDetails = new GameDetails(
                radioWhite.isChecked() ? Winner.white : Winner.black,
                Weight.get(spinnerWeight.getSelectedItemPosition()),
                txtKomi.getText().toString(),
                spinnerHandicap.getSelectedItemPosition(),
                notes);
        ResultUseCase.getInstance().prepareGame(gameDetails);
    }

    public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
        LocalDate date = LocalDate.of(selectedYear, selectedMonth + 1, selectedDay);
        ResultUseCase.getInstance().setDate(date);
        txtDate.setText(date.format(dateTimeFormatter));
    }

    public static class DatePickerFragment extends DialogFragment {
        private DatePickerDialog.OnDateSetListener dateListener;

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            LocalDate date = ResultUseCase.getInstance().getDate();
            return new DatePickerDialog(requireContext(), dateListener,
                    date.getYear(), date.getMonthValue() - 1, date.getDayOfMonth());
        }

        @Override
        public void onAttach(@NonNull Context context) {
            super.onAttach(context);

            dateListener = (DatePickerDialog.OnDateSetListener) getActivity();
        }
    }
}
