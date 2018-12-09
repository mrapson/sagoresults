package za.co.sagoclubs;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.widget.TextView;

import static za.co.sagoclubs.Constants.SHOWLOG_CGI;
import static za.co.sagoclubs.Constants.TAG;

public class GridProgressActivity extends Activity {

	private TextView txtOutput;
	
	//private Spinner spnPlayer;
	private TextView txtPlayer;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.logfile);
        
        txtOutput = (TextView)findViewById(R.id.txtOutput);
        txtOutput.setEnabled(false);
        txtPlayer = (TextView)findViewById(R.id.txtPlayer);
        
    	if(savedInstanceState!=null) {
            restoreProgress(savedInstanceState);
        } else {
            Log.d(TAG, "Calling server to get player logfile");
            txtPlayer.setText(Result.logfile.getName());
        	String result = InternetActions.getPreBlock(SHOWLOG_CGI + "?name="+Result.logfile.getId());
        	txtOutput.setMovementMethod(new ScrollingMovementMethod());
        	txtOutput.setText(result);
        }

	}

	@Override
    protected void onSaveInstanceState(Bundle saveState) {
        super.onSaveInstanceState(saveState);
        saveState.putString("output", txtOutput.getText().toString());
    }
	
	private void restoreProgress(Bundle savedInstanceState) {
        String output = savedInstanceState.getString("output");
        if (output!=null) {
	    	txtOutput.setText(output);
        }
	}
	
}
