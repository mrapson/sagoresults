package za.co.sagoclubs;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends Activity {
	private EditText etUsername;
	private EditText etPassword;
	private Button btnLogin;
	private TextView txtForgetPass;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);

		etUsername = findViewById(R.id.etUsername);
		etPassword = findViewById(R.id.etPassword);
		btnLogin = findViewById(R.id.btnLogin);
		txtForgetPass= findViewById(R.id.txtForgotPass);

		loadUserData();

		btnLogin.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				String username = etUsername.getText().toString().trim();
				String password = etPassword.getText().toString();

				setUserData(username, password);

				Cognito authentication = new Cognito(getApplicationContext());
				authentication.userLogin();
			}
		});
	}

	private void loadUserData() {
		UserData userData = UserData.getInstance();
		etUsername.setText(userData.getUsername());
		etPassword.setText(userData.getPassword());
	}

	private void setUserData(String username, String password) {
		UserData userData = UserData.getInstance();
		userData.setUsername(username);
		userData.setPassword(password);
	}
}
