package za.co.sagoclubs;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.tokens.CognitoIdToken;

public class UserData {
	private static volatile UserData INSTANCE = null;

	public final static String GUEST_USER = "guest";
	public final static String GUEST_PASS = "guest";

	private static String username = "";
	private static String password = "";
	private static CognitoIdToken idToken = null;

	private UserData() {}

	public static UserData getInstance() {
		if (INSTANCE == null) {
			synchronized (UserData.class) {
				if (INSTANCE == null) {
					INSTANCE = new UserData();
				}
			}
		}
		return INSTANCE;
	}

	public void setUsername(String username) {
		UserData.username = username;
	}

	public String getUsername() {
		return username;
	}

	public void setPassword(String password) {
		UserData.password = password;
	}

	public String getPassword() {
		return password;
	}

	public void setIdToken(CognitoIdToken idToken) {
		UserData.idToken = idToken;
	}

	public CognitoIdToken getIdToken() {
		return idToken;
	}

	public boolean isGuestUser() {
		return GUEST_USER.equals(username);
	}
}
