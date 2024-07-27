package za.co.sagoclubs;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

enum ResultState {
    Enter, Confirm, Undo, Complete
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

    public static Player logfile;

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

    public static void setLogFile(Player log) {
        Result.logfile = log;
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
                : "0";
    }

    public static String constructResultUri() {
        String uri = Constants.LOGGAME_CGI + "?";
        uri = uri + "whitename=" + Result.white.getId();
        uri = uri + "&blackname=" + Result.black.getId();
        uri = uri + "&GSF=" + Result.weight;
        uri = uri + "&result=" + Result.winner;
        uri = uri + "&komi=" + Result.komi;
        uri = uri + "&handicap=" + Result.handicapUriString();
        uri = uri + "&day=" + Result.date.getDayOfMonth();
        uri = uri + "&month=" + Result.date.getDayOfMonth() + 1;
        uri = uri + "&year=" + Result.date.getYear();
        uri = uri + "&notes=" + URLEncoder.encode(Result.notes, StandardCharsets.UTF_8);
        return uri;
    }

    public static String constructUndoUri() {
        String uri = Constants.UNDO_CGI + "?";
        uri += "a=" + white.getId();
        uri += "&b=" + black.getId();
        return uri;
    }
}
