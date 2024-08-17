package za.co.sagoclubs;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

public class ResultUseCase {
    enum Status {
        Enter, Sending, Sent,
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

    private Status status = Status.Complete;
    private Player white;
    private Player black;
    private LocalDate date;
    private GameDetails gameDetails;

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

    public void setStatus(Status status) {
        this.status = status;
    }

    public Status getStatus() {
        return status;
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

    public void setGameDetails(GameDetails gameDetails) {
        this.gameDetails = gameDetails;
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
}
