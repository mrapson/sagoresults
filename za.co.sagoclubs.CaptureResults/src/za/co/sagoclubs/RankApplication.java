package za.co.sagoclubs;

import android.app.Application;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RankApplication extends Application {
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);

    private static RankApplication instance;

    public RankApplication() {}

    @Override
    public void onCreate()
    {
        super.onCreate();
        instance = this;
    }

    public static RankApplication getApp() { return instance; }

    public ExecutorService getExecutorService() {
        return executorService;
    }
}
