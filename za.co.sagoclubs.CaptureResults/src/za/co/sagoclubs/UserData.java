package za.co.sagoclubs;

public class UserData {
	private static volatile UserData INSTANCE = null;

	private static String username = "";
	private static String password = "";

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

}
