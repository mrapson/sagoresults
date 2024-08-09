package za.co.sagoclubs;

import static za.co.sagoclubs.InternetActions.getPlayerLog;
import static za.co.sagoclubs.InternetActions.getRatingsPlayerLog;

import androidx.lifecycle.MutableLiveData;

import java.io.IOException;
import java.util.concurrent.ExecutorService;

public final class LogFileUseCase {
    enum Requester {
        None, RatingsLookup, HandleLookup
    }

    enum Status {
        None, Prepared, Processing, Done, PlayerError, AuthorizationError, NetworkError
    }

    private static volatile LogFileUseCase INSTANCE = null;

    private static Player player;
    private final static MutableLiveData<LogRecord> logRecord =
            new MutableLiveData<>(new LogRecord(null, "", Status.None));
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
        logRecord.postValue(new LogRecord(player, "", Status.Prepared));
    }

    public Player getPlayer() {
        return player;
    }

    public void fetchLogFile() {
        String id = player.getId();
        Player requestPlayer = player;
        ExecutorService executorService = RankApplication.getApp().getExecutorService();
        logRecord.postValue(new LogRecord(requestPlayer, "", Status.Processing));

        switch (requester) {
            case RatingsLookup -> executorService.execute(() -> {
                try {
                    String logFile = getRatingsPlayerLog(id);
                    logRecord.postValue(new LogRecord(requestPlayer, logFile, Status.Done));
                } catch (PlayerNotFoundException e) {
                    logRecord.postValue(new LogRecord(requestPlayer, "", Status.PlayerError));
                } catch (IOException e) {
                    logRecord.postValue(new LogRecord(requestPlayer, "", Status.NetworkError));
                }
            });
            case HandleLookup -> executorService.execute(() -> {
                try {
                    String logFile = getPlayerLog(id);
                    logRecord.postValue(new LogRecord(requestPlayer, logFile, Status.Done));
                }  catch (PlayerNotFoundException e) {
                    logRecord.postValue(new LogRecord(requestPlayer, "", Status.PlayerError));
                }  catch (InternetActions.AuthorizationException e) {
                    logRecord.postValue(new LogRecord(requestPlayer, "", Status.AuthorizationError));
                } catch (IOException e) {
                    logRecord.postValue(new LogRecord(requestPlayer, "", Status.NetworkError));
                }
            });
            case None -> logRecord.postValue(new LogRecord(requestPlayer, "", Status.NetworkError));
        }
    }

    public MutableLiveData<LogRecord> getLogRecord() {
        return logRecord;
    }

    public record LogRecord(Player player, String logFile, Status status) {}

    public static class PlayerNotFoundException extends IOException {
        public PlayerNotFoundException(String id) {
            super("No player found with handle " + id);
        }
    }
}
