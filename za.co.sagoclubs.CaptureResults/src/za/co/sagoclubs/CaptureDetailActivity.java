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

        radioWhite.setText(getString(R.string.white_radio, Result.getWhite().getName()));
        radioBlack.setText(getString(R.string.black_radio, Result.getBlack().getName()));

        spinnerWeight.setSelection(2);

        final LocalDate date = LocalDate.now();
        Result.setDate(date);
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
            startActivityForResult(myIntent, 0);
        });
    }

    private void saveResult() {
        if (radioWhite.isChecked()) {
            Result.setWinner("W");
        } else {
            Result.setWinner("B");
        }

        Result.setKomi(txtKomi.getText().toString().strip());

        Result.setWeight(
                switch (spinnerWeight.getSelectedItemPosition()) {
                    case 0 -> "0";
                    case 1 -> "0.5";
                    default -> "1.0";
                    case 3 -> "1.5";
                });

        Result.setHandicap(spinnerHandicap.getSelectedItemPosition());

        Result.setNotes(txtNotes.getText().toString());
        if (Result.getNotes().length() > 0) {
            getSharedPreferences("SETTINGS", MODE_PRIVATE).edit()
                    .putString("notes", Result.getNotes())
                    .apply();
        }
    }

    public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
        LocalDate date = LocalDate.of(selectedYear, selectedMonth + 1, selectedDay);
        Result.setDate(date);
        txtDate.setText(date.format(dateTimeFormatter));
    }

    public static class DatePickerFragment extends DialogFragment {
        private DatePickerDialog.OnDateSetListener dateListener;

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            LocalDate date = Result.getDate();
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
