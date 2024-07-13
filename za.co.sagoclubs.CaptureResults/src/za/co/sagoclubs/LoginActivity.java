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

		btnLogin.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Cognito authentication = new Cognito(getApplicationContext());
				authentication.userLogin(
						etUsername.getText().toString().trim(),
						etPassword.getText().toString());
			}
		});
	}
}
