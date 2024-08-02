package za.co.sagoclubs;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

enum ResultState {
    Enter, Confirm, Complete
}

public class Result {

    private static Player white;
    private static Player black;
    private static String weight;
    private static String winner; // B, W
    private static String komi;
    private static int handicap;
    private static LocalDate date;
    private static String notes;

    public static ResultState resultState;

    public static void setWhite(Player white) {
        Result.white = white;
    }

    public static Player getWhite() {
        return Result.white;
    }

    public static void setBlack(Player black) {
        Result.black = black;
    }

    public static Player getBlack() {
        return Result.black;
    }

    public static void setKomi(String komi) {
        Result.komi = komi;
    }

    public static void setHandicap(int handicap) {
        Result.handicap = handicap;
    }

    public static void setWeight(String weight) {
        Result.weight = weight;
    }

    public static void setWinner(String winner) {
        Result.winner = winner;
    }

    public static void setDate(LocalDate date) {
        Result.date = date;
    }

    public static LocalDate getDate() {
        return Result.date;
    }

    public static void setNotes(String notes) {
        Result.notes = notes;
    }

    public static String getNotes() {
        return Result.notes;
    }

    public static void setResultState(ResultState r) {
        Result.resultState = r;
    }

    private static String handicapUriString() {
        return Result.handicap != 0
                ? Integer.toString(Result.handicap)
                : "1";
    }

    public static String constructConfirmUriOptions() {
        return "whitename=" + Result.white.getId()
                + "&blackname=" + Result.black.getId()
                + "&GSF=" + Result.weight
                + "&result=" + Result.winner
                + "&komi=" + Result.komi
                + "&handicap=" + Result.handicapUriString()
                + "&day=" + Result.date.getDayOfMonth()
                + "&month=" + Result.date.getMonthValue()
                + "&year=" + Result.date.getYear()
                + "&notes=" + URLEncoder.encode(Result.notes, StandardCharsets.UTF_8);
    }

    public static String constructUndoUriOptions() {
        return "a=" + white.getId() + "&b=" + black.getId();
    }
}
