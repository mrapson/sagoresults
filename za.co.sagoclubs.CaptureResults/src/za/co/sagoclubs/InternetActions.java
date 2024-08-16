package za.co.sagoclubs;

import static za.co.sagoclubs.Constants.SHOWLOG;
import static za.co.sagoclubs.Constants.SHOW_LOG_DIRECT;
import static za.co.sagoclubs.Constants.TAG;

import android.util.Log;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.tokens.CognitoIdToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class InternetActions {
    public static String getRatingsPlayerLog(String id) throws IOException {
        String url = SHOWLOG + id + ".html";
        Log.d(TAG, "getPlayerLog: url=" + url);
        try {
            Connection connection = Jsoup.connect(url);
            return connection.get().body().text();
        } catch (HttpStatusException e) {
            if (e.getStatusCode() == HttpURLConnection.HTTP_NOT_FOUND) {
                throw new LogFileUseCase.PlayerNotFoundException(id);
            }
            throw new IOException(e);
        }
    }

    public static String getPlayerLog(String id) throws IOException {
        String url = SHOW_LOG_DIRECT + "?name=" + id;
        Log.d(TAG, "getPlayerLog: url=" + url);
        try {
            Connection connection = Jsoup.connect(url);
            setAuthorization(connection);
            return connection.get().body().text();
        } catch (HttpStatusException e) {
            switch (e.getStatusCode()) {
                case HttpURLConnection.HTTP_NOT_FOUND -> throw new LogFileUseCase.PlayerNotFoundException(id);
                case HttpURLConnection.HTTP_UNAUTHORIZED -> throw new AuthorizationException("unauthorized response");
                default -> throw new IOException(e);
            }
        }
    }

    private static void setAuthorization(Connection connection) throws IOException {
        UserData userData = UserData.getInstance();
        if (userData.isGuestUser()) {
            throw new AuthorizationException("Authorization request for guest user!");
        }

        if (!userData.isAuthorized()) {
            RankApplication.getApp()
                    .getAuthentication()
                    .actionLogin();
        }

        CognitoIdToken idToken = UserData.getInstance().getIdToken();
        if (idToken == null) {
            throw new AuthorizationException("IdToken null after login attempt!");
        }

        connection.header("Authorization", idToken.getJWTToken());
    }

    public static void sendResult(String confirmOptions) throws IOException {
        String url = Constants.LOGGAME_CGI + "?" + confirmOptions;
        try {
            Connection connection = Jsoup.connect(url);
            setAuthorization(connection);
            connection.get();
        } catch (HttpStatusException e) {
            switch (e.getStatusCode()) {
                case HttpURLConnection.HTTP_BAD_REQUEST ->
                        throw new InvalidRequestException();
                case HttpURLConnection.HTTP_UNAUTHORIZED ->
                        throw new AuthorizationException("unauthorized response");
                default -> throw new IOException(e);
            }
        }
    }

    public static void undoResult(String undoOptions) throws IOException {
        String url = Constants.UNDO_CGI + "?" + undoOptions;
        try {
            Connection connection = Jsoup.connect(url);
            setAuthorization(connection);
            connection.get();
        } catch (HttpStatusException e) {
            switch (e.getStatusCode()) {
                case HttpURLConnection.HTTP_BAD_REQUEST ->
                        throw new InvalidRequestException();
                case HttpURLConnection.HTTP_UNAUTHORIZED ->
                        throw new AuthorizationException("unauthorized response");
                default -> throw new IOException(e);
            }
        }
    }

    public static String getRefreshPage() {
        try {
            Connection connection = Jsoup.connect(Constants.REFRESH_HTML);
            setAuthorization(connection);
            return connection.get().body().text();
        } catch (IOException e) {
            // TODO handle refresh exceptions better
            Log.d(TAG, "getRefreshPage IOException: " + e);
            return "Network exception while loading refresh.";
        }
    }

    public static List<Player> getPlayerList() throws IOException, JSONException {
        List<Player> list = new ArrayList<>();
        try {
            Connection connection = Jsoup.connect(Constants.SHOW_HANDLES);
            setAuthorization(connection);
            connection.ignoreContentType(true);
            String bodyText = connection.get().body().text();

            JSONObject json = new JSONObject(bodyText);
            if (json.has("players")) {
                JSONArray playerArray = json.getJSONArray("players");
                for (int i = 0; i < playerArray.length(); i++) {
                    Player player = getPlayerFromJsonRow(playerArray.getJSONObject(i));
                    list.add(player);
                }
            }
        } catch (IOException e) {
            Log.d(TAG, "getPlayerList IOException: " + e);
            throw new IOException(e);
        } catch (JSONException e) {
            Log.d(TAG, "getPlayerList JSONException: " + e);
            throw new JSONException(e);
        }

        list.sort(new PlayerSortByName());
        return list;
    }

    private static Player getPlayerFromJsonRow(JSONObject row) throws JSONException, IOException {
        String id = row.getString("id");
        Pattern idPattern = Pattern.compile("[^a-z]");
        if (idPattern.matcher(id).find()) {
            throw new IOException("Id does not match expected pattern");
        }

        String name = row.getString("name");
        Pattern namePattern = Pattern.compile("[^A-Za-z- ]");
        if (namePattern.matcher(name).find()) {
            throw new IOException("Name does not match expected pattern");
        }

        boolean international = row.getBoolean("international");

        return new Player(id, name, international);
    }

    public static List<PlayerRating> getPlayerRatingsList() throws IOException, JSONException {
        List<PlayerRating> list = new ArrayList<>();
        try {
            Connection connection = Jsoup.connect(Constants.PLAYER_RATINGS);
            setAuthorization(connection);
            connection.ignoreContentType(true);
            String bodyText = connection.get().body().text();

            JSONObject json = new JSONObject(bodyText);
            if (json.has("players")) {
                JSONArray playerArray = json.getJSONArray("players");
                for (int i = 0; i < playerArray.length(); i++) {
                    PlayerRating playerRating = getPlayerRatingFromJsonRow(playerArray.getJSONObject(i));
                    list.add(playerRating);
                }
            }
        } catch (IOException e) {
            Log.d(TAG, "getPlayerRatingsList IOException: " + e);
            throw new IOException(e);
        } catch (JSONException e) {
            Log.d(TAG, "getPlayerRatingsList JSONException: " + e);
            throw new JSONException(e);
        }
        return list;
    }

    private static PlayerRating getPlayerRatingFromJsonRow(JSONObject row) throws JSONException, IOException {
        String name = row.getString("name");
        Pattern namePattern = Pattern.compile("[^A-Za-z- ]");
        if (namePattern.matcher(name).find()) {
            throw new IOException("Name does not match expected pattern");
        }

        String rank = row.getString("rank");
        Pattern rankPattern = Pattern.compile("[^0-9kdp]");
        if (rankPattern.matcher(rank).find()) {
            throw new IOException("Rank does not match expected pattern");
        }

        String index = row.getString("index");
        Pattern indexPattern = Pattern.compile("[^-0-9]");
        if (indexPattern.matcher(index).find()) {
            throw new IOException("Index does not match expected pattern");
        }

        String lastPlayedDate = row.getString("date");
        Pattern datePattern = Pattern.compile("[^0-9/]");
        if (datePattern.matcher(lastPlayedDate).find()) {
            throw new IOException("Date does not match expected pattern");
        }

        String id = row.getString("id");
        Pattern idPattern = Pattern.compile("[^a-z]");
        if (idPattern.matcher(id).find()) {
            throw new IOException("Id does not match expected pattern");
        }

        return new PlayerRating(id, name, rank, index, lastPlayedDate);
    }

    public static class AuthorizationException extends IOException {
        public AuthorizationException(String message) {
            super("Authorization exception: " + message);
        }
    }

    public static class InvalidRequestException extends IOException {
        public InvalidRequestException() {
            super("Request rejected by server");
        }
    }
}
