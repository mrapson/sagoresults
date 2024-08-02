package za.co.sagoclubs;

import static za.co.sagoclubs.InternetActions.getPlayerLog;
import static za.co.sagoclubs.InternetActions.getRatingsPlayerLog;

public final class LogFileUseCase {
    enum Requester {
        None, RatingsLookup, HandleLookup
    }

    enum Status {
        None, Prepared, Processing, Done, Error
    }

    private static volatile LogFileUseCase INSTANCE = null;

    private static Player player;
    private static String logFile = "";
    private static Requester requester = Requester.None;
    private static Status status = Status.None;

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
        LogFileUseCase.logFile = "";
        LogFileUseCase.status = Status.Prepared;
    }

    public Player getPlayer() {
        return player;
    }

    public void fetchLogFile() {
        String id = player.getId();
        LogFileUseCase.status = Status.Processing;
        switch (requester) {
            case RatingsLookup -> {
                LogFileUseCase.logFile = getRatingsPlayerLog(id);
                LogFileUseCase.status = Status.Done;
            }
            case HandleLookup -> {
                LogFileUseCase.logFile = getPlayerLog(id);
                LogFileUseCase.status = Status.Done;
            }
            case None -> LogFileUseCase.status = Status.Error;
        }
    }

    public String getLogFile() {
        return logFile;
    }
}
