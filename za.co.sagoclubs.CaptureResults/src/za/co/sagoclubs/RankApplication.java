package za.co.sagoclubs;

import android.app.Application;
import android.content.SharedPreferences;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RankApplication extends Application {
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);
    private final UserData userData = UserData.getInstance();

    private static RankApplication instance;

    public RankApplication() {}

    @Override
    public void onCreate()
    {
        super.onCreate();
        instance = this;

        SharedPreferences preferences = getSharedPreferences("SETTINGS", MODE_PRIVATE);
        userData.setUsername(preferences.getString("username", UserData.GUEST_USER));
        userData.setPassword(preferences.getString("password", UserData.GUEST_PASS));

        if (!userData.isGuestUser()) {
            Cognito authentication = new Cognito(getApplicationContext());
            authentication.userLogin();
        }
        InternetActions.forcePlayerArrayReload();
    }

    public static RankApplication getApp() { return instance; }

    public ExecutorService getExecutorService() {
        return executorService;
    }
}
