package za.co.sagoclubs;

import java.util.Calendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;

public class CaptureDetailActivity extends Activity {
	private static final int DATE_DIALOG_ID = 999;
	
    private EditText txtDate;
    private EditText txtKomi;
    private EditText txtNotes;
    private Spinner spinnerWeight; 
    private Spinner spinnerHandicap; 
	private Button btnChangeDate;
	private Button btnSaveResult;
	private Button btnPrevious;
	private RadioButton radioWhite;
	private RadioButton radioBlack;
    
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail);
        
		btnChangeDate = findViewById(R.id.btnChangeDate);
		btnSaveResult = findViewById(R.id.btnSaveResult);
		btnPrevious = findViewById(R.id.btnPrevious);
        txtKomi = findViewById(R.id.txtKomi);
        txtDate = findViewById(R.id.txtDate);
        txtNotes = findViewById(R.id.txtNotes);
        radioWhite = findViewById(R.id.radioWhite);
        radioBlack = findViewById(R.id.radioBlack);
        spinnerWeight = findViewById(R.id.spinnerWeight);
        spinnerHandicap = findViewById(R.id.spinnerHandicap);

        addChangeButtonListener();
        addPreviousButtonListener();
        addSaveResultButtonListener();
        addSpinnerHandicapOnItemSelectedListener();
        
        txtKomi.setText("6.5");
        
        radioWhite.setText(getString(R.string.white_radio, Result.white.getName()));
		radioBlack.setText(getString(R.string.black_radio, Result.black.getName()));

        spinnerWeight.setSelection(2);

		final Calendar c = Calendar.getInstance();
		Result.year = String.valueOf(c.get(Calendar.YEAR));
		Result.month = addLeadingZero(String.valueOf(c.get(Calendar.MONTH) + 1));
		Result.day = addLeadingZero(String.valueOf(c.get(Calendar.DAY_OF_MONTH)));
 
		txtDate.setText(new StringBuilder()
		.append(Result.day).append("-").append(Result.month).append("-")
		.append(Result.year).append(" "));
        
    }

	public void addChangeButtonListener() {
		btnChangeDate.setOnClickListener(v -> showDialog(DATE_DIALOG_ID));
	}

	public void addPreviousButtonListener() {
		btnPrevious.setOnClickListener(v -> {
			SharedPreferences preferences = getSharedPreferences("SETTINGS", MODE_PRIVATE);
			String notes = preferences.getString("notes", "");
			if (notes.length()>0) {
				txtNotes.setText(notes);
			}
		});
	}

	public void addSpinnerHandicapOnItemSelectedListener() {
		spinnerHandicap.setOnItemSelectedListener(new OnItemSelectedListener() {
		    @Override
		    public void onItemSelected(AdapterView<?> parentView,
									   View selectedItemView,
									   int position,
									   long id) {
				if (spinnerHandicap.getSelectedItemPosition()>0) {
					txtKomi.setText("0.5");
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
			Result.result = "W";
		} else if (radioBlack.isChecked()) {
			Result.result = "B";
		} else {
			Result.result = "D";
		}
		Result.komi = txtKomi.getText().toString().trim();
		if (spinnerWeight.getSelectedItemPosition()==0) {
			Result.weight = "0";
		} else if (spinnerWeight.getSelectedItemPosition()==1) {
			Result.weight = "0.5";
		} else if (spinnerWeight.getSelectedItemPosition()==2) {
			Result.weight = "1.0";
		} else {
			Result.weight = "1.5";
		}
		Result.handicap = String.valueOf(spinnerHandicap.getSelectedItemPosition()+1);
		Result.notes = txtNotes.getText().toString();
		if (Result.notes.length() > 0) {
			SharedPreferences preferences = getSharedPreferences("SETTINGS", MODE_PRIVATE);
			Editor editor = preferences.edit();
			editor.putString("notes", Result.notes);
			editor.commit();
		}
	}
	
	private String addLeadingZero(String input) {
		if (input.length() == 1) {
			return "0"+input;
		}
		return input;
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DATE_DIALOG_ID:
		   return new DatePickerDialog(
				   this,
				   datePickerListener,
				   Integer.parseInt(Result.year),
				   Integer.parseInt(Result.month) - 1,
				   Integer.parseInt(Result.day));
	    }

		return null;
	}
 
	private final DatePickerDialog.OnDateSetListener datePickerListener
			= new DatePickerDialog.OnDateSetListener() {
		// when dialog box is closed, below method will be called.
		public void onDateSet(DatePicker view, int selectedYear,
				int selectedMonth, int selectedDay) {
			Result.year = String.valueOf(selectedYear);
			Result.month = addLeadingZero(String.valueOf(selectedMonth+1));
			Result.day = addLeadingZero(String.valueOf(selectedDay));
 
			txtDate.setText(new StringBuilder()
			.append(Result.day).append("-").append(Result.month).append("-")
			.append(Result.year).append(" "));
		}
	};
}
