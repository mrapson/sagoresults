package za.co.sagoclubs;

import static za.co.sagoclubs.InternetActions.getPlayerLog;
import static za.co.sagoclubs.InternetActions.getRatingsPlayerLog;

import androidx.lifecycle.MutableLiveData;

import java.util.concurrent.ExecutorService;

public final class LogFileUseCase {
    enum Requester {
        None, RatingsLookup, HandleLookup
    }

    enum Status {
        None, Prepared, Processing, Done, Error
    }

    private static volatile LogFileUseCase INSTANCE = null;

    private static Player player;
    private final static MutableLiveData<LogRecord> logRecord =
            new MutableLiveData<>(new LogRecord("", Status.None));
    private static Requester requester = Requester.None;

    private LogFileUseCase() {
    }

    public static LogFileUseCase getInstance() {
        if (INSTANCE == null) {
            synchronized (LogFileUseCase.class) {
                if (INSTANCE == null) {
                    INSTANCE = new LogFileUseCase();
                }
            }
        }
        return INSTANCE;
    }

    public void prepareRequest(Player player, Requester requester) {
        LogFileUseCase.requester = requester;
        LogFileUseCase.player = player;
        logRecord.postValue(new LogRecord("", Status.Prepared));
    }

    public Player getPlayer() {
        return player;
    }

    public void fetchLogFile() {
        String id = player.getId();
        ExecutorService executorService = RankApplication.getApp().getExecutorService();
        logRecord.postValue(new LogRecord("", Status.Processing));

        switch (requester) {
            case RatingsLookup -> executorService.execute(() -> {
                String logFile = getRatingsPlayerLog(id);
                logRecord.postValue(new LogRecord(logFile, Status.Done));
            });
            case HandleLookup -> executorService.execute(() -> {
                String logFile = getPlayerLog(id);
                logRecord.postValue(new LogRecord(logFile, Status.Done));
            });
            case None -> logRecord.postValue(new LogRecord("", Status.Error));
        }
    }

    public MutableLiveData<LogRecord> getLogRecord() {
        return logRecord;
    }

    public record LogRecord(String logFile, Status status) {}
}
