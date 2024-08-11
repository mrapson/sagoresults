package za.co.sagoclubs;

import static za.co.sagoclubs.Constants.SHOWLOG;
import static za.co.sagoclubs.Constants.SHOW_LOG_DIRECT;
import static za.co.sagoclubs.Constants.TAG;

import android.app.AlertDialog;
import android.util.Log;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.tokens.CognitoIdToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class InternetActions {
    private static List<PlayerRating> playerRatingData = null;

    public static PlayerRating[] getPlayerRatingsArray(PlayerSortOrder order)
            throws IOException, JSONException {
        if (playerRatingData == null) {
            playerRatingData = getPlayerRatingsList();
        }

        return playerRatingData.stream()
                .sorted(PlayerSortOrder.SORT_BY_NAME == order
                        ? new PlayerSortByName()
                        : new PlayerSortByRating())
                .toArray(PlayerRating[]::new);
    }

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

    public static String openPage(String url) {
        HttpURLConnection c = openApiGatewayConnection(url);
        BufferedReader reader = null;
        StringBuilder result = new StringBuilder();
        try {
            reader = new BufferedReader(new InputStreamReader(c.getInputStream(), StandardCharsets.UTF_8), 8192);
            for (String line; (line = reader.readLine()) != null; ) {
                result.append(line).append("\n");
            }
        } catch (FileNotFoundException fnfe) {
            AlertDialog.Builder dlgAlert = new AlertDialog.Builder(null);

            dlgAlert.setMessage("Unable to open connection to server. Please check the user name and password configured in Settings.");
            dlgAlert.setTitle("Connection Failure");
            dlgAlert.setPositiveButton("Ok", (dialog, id) -> {
                // User clicked OK button
            });
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) try {
                reader.close();
            } catch (IOException ignored) {
            }
            c.disconnect();
        }
        return result.toString();
    }

    public static String confirmResult(String confirmOptions) {
        openPage(Constants.LOGGAME_CGI + "?" + confirmOptions);
        return getRefreshPage();
    }

    public static String undoResult(String undoOptions) {
        openPage(Constants.UNDO_CGI + "?" + undoOptions);
        return getRefreshPage();
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

    public static HttpURLConnection openApiGatewayConnection(String url) {
        CognitoIdToken idToken = UserData.getInstance().getIdToken();
        // TODO handle idToken null
        HttpURLConnection c = null;
        try {
            URL u = new URL(url);
            c = (HttpURLConnection) u.openConnection();
            Log.d(TAG, "openConnection: url=" + url);
            if (idToken != null) {
                Log.d(TAG, "idToken expiry=" + idToken.getExpiration());
                String bearerAuth = "Bearer " + UserData.getInstance().getIdToken().getJWTToken();
                c.setRequestProperty("Authorization", bearerAuth);
            }
            c.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return c;
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

    private static List<PlayerRating> getPlayerRatingsList() throws IOException, JSONException {
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
}
