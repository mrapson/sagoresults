package za.co.sagoclubs;

import static za.co.sagoclubs.Constants.SHOWLOG;
import static za.co.sagoclubs.Constants.SHOW_LOG_DIRECT;
import static za.co.sagoclubs.Constants.TAG;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.util.Log;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.tokens.CognitoIdToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class InternetActions {

    private static List<Player> playerData = null;
    private static List<PlayerRating> playerRatingData = null;

    public static void forcePlayerArrayReload() {
        Log.d(TAG, "Clearing playerData to force reload");
        playerData = null;
    }

    public static List<Player> getPlayerList() {
        // TODO: check whether playerData can become stale
        if (playerData != null) {
            return playerData;
        }

        try {
            playerData = getRawPlayerList();
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            playerData = new ArrayList<>();
        }
        return playerData;
    }

    public static PlayerRating[] getPlayerRatingsArray(PlayerSortOrder order)
            throws IOException, JSONException {
        if (playerRatingData == null) {
            playerRatingData = getRawPlayerRatingsList();
        }

        return playerRatingData.stream()
                .sorted(PlayerSortOrder.SORT_BY_NAME == order
                        ? new PlayerSortByName()
                        : new PlayerSortByRating())
                .toArray(PlayerRating[]::new);
    }

    public static String getRatingsPlayerLog(String id) {
        try {
            String url = SHOWLOG + id + ".html";
            HttpURLConnection c = openConnection(url);
            return getPreBlock(c);
        } catch (IOException e) {
            e.printStackTrace();
            return "Exception swallowed!";
        }
    }

    public static String getPlayerLog(String id) {
        String url = SHOW_LOG_DIRECT + "?name=" + id;
        HttpURLConnection c = openApiGatewayConnection(url);
        return getPreBlock(c);
    }

    public static Player[] getAllPlayers() {
        List<Player> list = getPlayerList();

        return list.stream()
                .sorted(new PlayerSortByName())
                .toArray(Player[]::new);
    }

    public static Player[] getLocalPlayers() {
        List<Player> list = getPlayerList();

        return list.stream()
                .sorted(new PlayerSortByName())
                .filter(player -> !player.isInternational())
                .toArray(Player[]::new);
    }

    public static Player[] getFavouritePlayers(SharedPreferences preferences) {
        Set<String> saved = Arrays.stream(preferences.getString("favourite_players", "")
                .split(",")).collect(Collectors.toSet());
        List<Player> allPlayers = getPlayerList();
        List<Player> list = new ArrayList<>();
        for (Player player : allPlayers) {
            if (saved.contains(player.getId())) {
                list.add(player);
            }
        }
        Player[] template = new Player[]{};
        return list.toArray(template);
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
        return getPreBlock(Constants.REFRESH_HTML);
    }

    public static String undoResult(String undoOptions) {
        openPage(Constants.UNDO_CGI + "?" + undoOptions);
        return getPreBlock(Constants.REFRESH_HTML);
    }

    public static String getPreBlock(String url) {
        Log.d(TAG, "opening " + url);
        HttpURLConnection c = openApiGatewayConnection(url);
        return getPreBlock(c);
    }

    public static String getPreBlock(HttpURLConnection c) {
        BufferedReader reader = null;
        StringBuilder work = new StringBuilder();
        try {
            reader = new BufferedReader(new InputStreamReader(c.getInputStream(), StandardCharsets.UTF_8), 8192);
            for (String line; (line = reader.readLine()) != null; ) {
                work.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) try {
                reader.close();
            } catch (IOException ignored) {
            }
            c.disconnect();
        }
        String result = work.toString();
        int index = result.toLowerCase().indexOf("<pre>");
        if (index > 0) {
            result = result.substring(index + 6);
            index = result.toLowerCase().indexOf("</pre>");
            if (index > 0) {
                result = result.substring(0, index);
            }
        }
        return result;
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

    private static HttpURLConnection openConnection(String url) throws IOException {
        URL u = new URL(url);
        HttpURLConnection c = (HttpURLConnection) u.openConnection();
        c.connect();
        return c;
    }

    private static List<Player> getRawPlayerList() throws IOException, JSONException {
        HttpURLConnection c = openApiGatewayConnection(Constants.SHOW_HANDLES);
        BufferedReader reader = null;
        List<Player> list = new ArrayList<>();
        try {
            reader = new BufferedReader(new InputStreamReader(c.getInputStream(), StandardCharsets.UTF_8), 8192);
            StringBuilder jsonStringBuilder = new StringBuilder();
            for (String line; (line = reader.readLine()) != null; ) {
                jsonStringBuilder.append(line);
            }
            JSONObject json = new JSONObject(jsonStringBuilder.toString());
            if (json.has("players")) {
                JSONArray playerArray = json.getJSONArray("players");
                for (int i = 0; i < playerArray.length(); i++) {
                    Player player = getPlayerFromJsonRow(playerArray.getJSONObject(i));
                    list.add(player);
                }
            }
        } finally {
            if (reader != null) try {
                reader.close();
            } catch (IOException ignored) {
            }
            c.disconnect();
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

    private static List<PlayerRating> getRawPlayerRatingsList() throws IOException, JSONException {
        HttpURLConnection c = openConnection(Constants.PLAYER_RATINGS);
        BufferedReader reader = null;
        List<PlayerRating> list = new ArrayList<>();
        try {
            reader = new BufferedReader(new InputStreamReader(c.getInputStream(), StandardCharsets.UTF_8), 8192);
            StringBuilder jsonStringBuilder = new StringBuilder();
            for (String line; (line = reader.readLine()) != null; ) {
                jsonStringBuilder.append(line);
            }
            JSONObject json = new JSONObject(jsonStringBuilder.toString());
            if (json.has("players")) {
                JSONArray playerArray = json.getJSONArray("players");
                for (int i = 0; i < playerArray.length(); i++) {
                    PlayerRating playerRating = getPlayerRatingFromJsonRow(playerArray.getJSONObject(i));
                    list.add(playerRating);
                }
            }
        } finally {
            if (reader != null) try {
                reader.close();
            } catch (IOException ignored) {
            }
            c.disconnect();
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
}
