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
    private final CognitoUserPool userPool;
    private final Context appContext;

    private final UserData userData;

    public Cognito(Context context) {
        appContext = context;
        String poolID = "us-east-2_lQ07vxzvD";
        String clientID = "6pqaqv2diseq4dq1pt3o72mcn6";
        Regions awsRegion = Regions.US_EAST_2;
        String clientSecret = null;

        userPool = new CognitoUserPool(context, poolID, clientID, clientSecret, awsRegion);
        userData = UserData.getInstance();
    }

    public void settingsLogin() {
        CognitoUser cognitoUser = userPool.getUser(userData.getUsername());
        cognitoUser.getSessionInBackground(new SettingsAuthenticationHandler());
    }

    public void startupLogin() {
        CognitoUser cognitoUser = userPool.getUser(userData.getUsername());
        cognitoUser.getSessionInBackground(new StartupAuthenticationHandler());
    }

    public void actionLogin() {
        CognitoUser cognitoUser = userPool.getUser(userData.getUsername());
        cognitoUser.getSession(new QuietAuthenticationHandler());
    }

    class QuietAuthenticationHandler implements AuthenticationHandler {
        @Override
        public void onSuccess(CognitoUserSession userSession, CognitoDevice newDevice) {
            userData.setAuthorization(userSession);
        }

        @Override
        public void getAuthenticationDetails(AuthenticationContinuation authenticationContinuation,
                                             String userId) {
            // The API needs user sign-in credentials to continue
            AuthenticationDetails authenticationDetails = new AuthenticationDetails(
                    userId,
                    userData.getPassword(),
                    null);
            // Pass the user sign-in credentials to the continuation
            authenticationContinuation.setAuthenticationDetails(authenticationDetails);
            // Allow the sign-in to continue
            authenticationContinuation.continueTask();
        }

        @Override
        public void getMFACode(MultiFactorAuthenticationContinuation continuation) {}

        @Override
        public void authenticationChallenge(ChallengeContinuation continuation) {}

        @Override
        public void onFailure(Exception exception) {
            userData.setAuthorization(null);
        }
    }

    class StartupAuthenticationHandler extends QuietAuthenticationHandler {
        @Override
        public void onSuccess(CognitoUserSession userSession, CognitoDevice newDevice) {
            // Sign-in was successful, cognitoUserSession will contain tokens for the user
            userData.setAuthorization(userSession);
            PlayerUseCase.getInstance().updatePlayerData();
        }
    }

    class SettingsAuthenticationHandler extends QuietAuthenticationHandler {
        @Override
        public void onSuccess(CognitoUserSession userSession, CognitoDevice newDevice) {
            // Sign-in was successful, cognitoUserSession will contain tokens for the user
            userData.setAuthorization(userSession);
            Toast.makeText(appContext, "Sign in success", Toast.LENGTH_LONG).show();
            PlayerUseCase.getInstance().updatePlayerData();
        }

        @Override
        public void onFailure(Exception exception) {
            // Sign-in failed, check exception for the cause
            userData.setAuthorization(null);
            Toast.makeText(appContext, "Sign in Failure", Toast.LENGTH_LONG).show();
        }
    }
}
