package za.co.sagoclubs;

import android.content.Context;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;
import com.amazonaws.regions.Regions;

public class Cognito {
	private String poolID = "us-east-2_lQ07vxzvD";
	private String clientID = "6pqaqv2diseq4dq1pt3o72mcn6";
	private String clientSecret = null;
	private Regions awsRegion = Regions.US_EAST_2;
	private CognitoUserPool userPool;
	private Context appContext;
	private String userPassword;

	public Cognito(Context context){
		appContext = context;
		userPool = new CognitoUserPool(context, this.poolID, this.clientID, this.clientSecret, this.awsRegion);
	}

	public void userLogin(String userId, String password){
		CognitoUser cognitoUser =  userPool.getUser(userId);
		this.userPassword = password;
		cognitoUser.getSessionInBackground(authenticationHandler);
	}

	// Callback handler for the sign-in process
	AuthenticationHandler authenticationHandler = new AuthenticationHandler() {
		@Override
		public void authenticationChallenge(ChallengeContinuation continuation) {

		}

		@Override
		public void onSuccess(CognitoUserSession userSession, CognitoDevice newDevice) {
			// Sign-in was successful, cognitoUserSession will contain tokens for the user
			Toast.makeText(appContext, "Sign in success", Toast.LENGTH_LONG).show();
		}

		@Override
		public void getAuthenticationDetails(AuthenticationContinuation authenticationContinuation, String userId) {
			// The API needs user sign-in credentials to continue
			AuthenticationDetails authenticationDetails = new AuthenticationDetails(userId, userPassword, null);
			// Pass the user sign-in credentials to the continuation
			authenticationContinuation.setAuthenticationDetails(authenticationDetails);
			// Allow the sign-in to continue
			authenticationContinuation.continueTask();
		}

		@Override
		public void getMFACode(MultiFactorAuthenticationContinuation multiFactorAuthenticationContinuation) {
			// Not used. See the link below for an implementation example.
			// https://aws.amazon.com/blogs/mobile/using-android-sdk-with-amazon-cognito-your-user-pools/
		}

		@Override
		public void onFailure(Exception exception) {
			// Sign-in failed, check exception for the cause
			Toast.makeText(appContext,"Sign in Failure", Toast.LENGTH_LONG).show();
		}
	};
}
