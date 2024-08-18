package za.co.sagoclubs;

import static za.co.sagoclubs.InternetActions.getRefreshPage;
import static za.co.sagoclubs.InternetActions.sendResult;
import static za.co.sagoclubs.InternetActions.undoResult;

import androidx.lifecycle.MutableLiveData;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.concurrent.ExecutorService;

import za.co.sagoclubs.InternetActions.AuthorizationException;
import za.co.sagoclubs.InternetActions.InvalidRequestException;

public class ResultUseCase {
    enum Status {
        Enter, Ready, Sending, Sent,
        SendingAuthorizationError,
        SendingNetworkError,
        SendingClientError,
        Fetching, Complete,
        FetchingAuthorizationError,
        FetchingNetworkError
    }

    enum Winner {
        black("B"),
        white("W");

        public final String encoding;

        Winner(String encoding) {
            this.encoding = encoding;
        }
    }

    enum Weight {
        free("0"),
        friendly("0.5"),
        club("1.0"),
        tournament("1.5");

        private static final Weight[] values = values();

        public final String encoding;

        Weight(String encoding) {
            this.encoding = encoding;
        }

        static Weight get(int ordinal) {
            return values[ordinal];
        }
    }

    private Player white;
    private Player black;
    private LocalDate date;
    private GameDetails gameDetails;
    private final MutableLiveData<ResultState> submitState =
            new MutableLiveData<>(new ResultState(Status.Complete, ""));
    private final MutableLiveData<ResultState> undoState =
            new MutableLiveData<>(new ResultState(Status.Complete, ""));


    private static volatile ResultUseCase INSTANCE = null;

    private ResultUseCase() {
    }

    public static ResultUseCase getInstance() {
        if (INSTANCE == null) {
            synchronized (ResultUseCase.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ResultUseCase();
                }
            }
        }
        return INSTANCE;
    }

    public void setWhite(Player white) {
        this.white = white;
    }

    public Player getWhite() {
        return  white;
    }

    public void setBlack(Player black) {
        this.black = black;
    }

    public Player getBlack() {
        return  black;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalDate getDate() {
        return date;
    }

    public MutableLiveData<ResultState> getSubmitState() {
        return submitState;
    }

    public MutableLiveData<ResultState> getUndoState() {
        return undoState;
    }

    public void readyForPlayers() {
        submitState.setValue(new ResultState(Status.Enter, ""));
    }

    public void prepareGame(GameDetails gameDetails) {
        this.gameDetails = gameDetails;
        submitState.setValue(new ResultState(Status.Ready, ""));
    }

    public void submitGame() {
        submitState.setValue(new ResultState(Status.Sending, ""));
        ExecutorService executorService = RankApplication.getApp().getExecutorService();
        executorService.execute(() -> {
            try {
                sendResult(constructConfirmUriOptions());
                submitState.postValue(new ResultState(Status.Sent, ""));
            } catch (InvalidRequestException e) {
                submitState.postValue(new ResultState(Status.SendingClientError, ""));
                return;
            } catch (AuthorizationException e) {
                submitState.postValue(new ResultState(Status.SendingAuthorizationError, ""));
                return;
            } catch (IOException e) {
                submitState.postValue(new ResultState(Status.SendingNetworkError, ""));
                return;
            }

            submitState.postValue(new ResultState(Status.Fetching, ""));
            try {
                String output = getRefreshPage();
                submitState.postValue(new ResultState(Status.Complete, output));
            } catch (AuthorizationException e) {
                submitState.postValue(new ResultState(Status.FetchingAuthorizationError, ""));
            } catch (IOException e) {
                submitState.postValue(new ResultState(Status.FetchingNetworkError, ""));
            }
        });
    }

    public void prepareUndo() {
        undoState.setValue(new ResultState(Status.Ready, ""));
    }

    public void undoGame() {
        undoState.setValue(new ResultState(Status.Sending, ""));
        ExecutorService executorService = RankApplication.getApp().getExecutorService();
        executorService.execute(() -> {
            try {
                undoResult(constructUndoUriOptions());
                undoState.postValue(new ResultState(Status.Sent, ""));
            } catch (InvalidRequestException e) {
                undoState.postValue(new ResultState(Status.SendingClientError, ""));
                return;
            } catch (AuthorizationException e) {
                undoState.postValue(new ResultState(Status.SendingAuthorizationError, ""));
                return;
            } catch (IOException e) {
                undoState.postValue(new ResultState(Status.SendingNetworkError, ""));
                return;
            }

            undoState.postValue(new ResultState(Status.Fetching, ""));
            try {
                String output = getRefreshPage();
                undoState.postValue(new ResultState(Status.Complete, output));
            } catch (AuthorizationException e) {
                undoState.postValue(new ResultState(Status.FetchingAuthorizationError, ""));
            } catch (IOException e) {
                undoState.postValue(new ResultState(Status.FetchingNetworkError, ""));
            }
        });
    }

    public void completeAll() {
        ResultState complete = new ResultState(Status.Complete, "");
        submitState.setValue(complete);
        undoState.setValue(complete);
    }


    public String constructConfirmUriOptions() {
        return "whitename=" + white.getId()
                + "&blackname=" + black.getId()
                + "&GSF=" + gameDetails.weight.encoding
                + "&result=" + gameDetails.winner.encoding
                + "&komi=" + gameDetails.komi
                + "&handicap=" + handicapString(gameDetails.handicap)
                + "&day=" + date.getDayOfMonth()
                + "&month=" + date.getMonthValue()
                + "&year=" + date.getYear()
                + "&notes=" + URLEncoder.encode(gameDetails.notes, StandardCharsets.UTF_8);
    }

    private static String handicapString(int handicap) {
        return handicap != 0 ? Integer.toString(handicap) : "1";
    }

    public String constructUndoUriOptions() {
        return "a=" + white.getId() + "&b=" + black.getId();
    }

    public record GameDetails(Winner winner,
                              Weight weight,
                              String komi,
                              int handicap,
                              String notes) {
        public GameDetails(Winner winner,
                           Weight weight,
                           String komi,
                           int handicap,
                           String notes) {
            this.winner = winner;
            this.weight = weight;
            this.komi = komi.strip().replaceAll("[^0-9.-]]","");
            if (handicap < 0) {
                handicap = 0;
            }
            if (handicap > 9) {
                handicap = 9;
            }
            this.handicap = handicap;
            this.notes = notes.strip().replaceAll("[^a-zA-Z0-9_.?-]", "");
        }
    }

    public record ResultState(Status status, String output) {}
}
