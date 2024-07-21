package za.co.sagoclubs;

import static java.lang.Thread.sleep;
import static za.co.sagoclubs.Constants.TAG;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.util.Base64;
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
import java.util.regex.Pattern;

public class InternetActions {

    private static Player[] playerData = null;
    private static List<PlayerRating> playerRatingData = null;

    public static void forcePlayerArrayReload() {
        Log.d(TAG, "Clearing playerData to force reload");
        //playerData = null;
        playerData = getTempPlayerArray();
    }

    public static Player[] getTempPlayerArray() {
        ArrayList<Player> list = new ArrayList<>();
        list.add(new Player("testone", "Test One"));
        list.add(new Player("testtwo", "Test Two"));
        Player[] template = new Player[]{};
        return list.toArray(template);
    }

    public static Player[] getPlayerArray() {
        // TODO: check whether playerData can become stale
        if (playerData != null) {
            return playerData;
        }
        ArrayList<Player> list = new ArrayList<>();
        for (String item : getRawPlayerList()) {
            if (item.startsWith("s/,")) {
                String[] parts = item.split(",");
                String id = parts[1];
                String name = parts[3];
                list.add(new Player(id, name));
            }
        }

        Player[] template = new Player[]{};
        playerData = list.toArray(template);
        Arrays.sort(playerData);
        return playerData;
    }

    public static PlayerRating[] getPlayerRatingsArray(PlayerSortOrder order) {
        try {
            sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (playerRatingData == null) {
            playerRatingData = getRawPlayerRatingsList();
        }

        return playerRatingData.stream()
                .sorted(PlayerSortOrder.SORT_BY_NAME == order
                        ? new PlayerSortByName()
                        : new PlayerSortByRating())
                .toArray(PlayerRating[]::new);
    }

    public static Player[] getFavouritePlayers(SharedPreferences preferences) {
        String save = preferences.getString("favourite_players", "");
        List<String> items = Arrays.asList(save.split(","));
        Player[] allPlayers = getPlayerArray();
        List<Player> list = new ArrayList<>();
        for (Player player : allPlayers) {
            if (items.contains(player.getId())) {
                list.add(player);
            }
        }
        Player[] template = new Player[]{};
        return list.toArray(template);
    }

    public static String openPage(String url) {
        HttpURLConnection c = openConnection(url);
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
            } catch (IOException logOrIgnore) {
            }
            c.disconnect();
        }
        return result.toString();
    }

    public static String getPreBlock(String url) {
        Log.d(TAG, "opening " + url);
        HttpURLConnection c = openConnection(url);
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
            } catch (IOException logOrIgnore) {
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

    private static HttpURLConnection openConnection(String url) {
        UserData userData = UserData.getInstance();
        String username = userData.getUsername();
        String password = userData.getUsername();

        HttpURLConnection c = null;
        try {
            URL u = new URL(url);
            c = (HttpURLConnection) u.openConnection();
            Log.d(TAG, "openConnection: url=" + url + ", username=" + username + ", password=" + password);
            String basicAuth = "Basic " + new String(Base64.encode((username + ":" + password).getBytes(), Base64.NO_WRAP));
            c.setRequestProperty("Authorization", basicAuth);
            c.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return c;
    }

    public static HttpURLConnection openApiGatewayConnection(String url) {
        CognitoIdToken idToken = UserData.getInstance().getIdToken();
        // TODO handle idToken null
        HttpURLConnection c = null;
        try {
            URL u = new URL(url);
            c = (HttpURLConnection) u.openConnection();
            Log.d(TAG, "openConnection: url=" + url + ", idToken expiry=" + idToken.getExpiration());
            String bearerAuth = "Bearer " + UserData.getInstance().getIdToken().getJWTToken();
            c.setRequestProperty("Authorization", bearerAuth);
            c.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return c;
    }

    private static HttpURLConnection openUnsecuredConnection(String url) {
        HttpURLConnection c = null;
        try {
            URL u = new URL(url);
            c = (HttpURLConnection) u.openConnection();
            c.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return c;
    }

    private static List<String> getRawPlayerList() {
        HttpURLConnection c = openConnection(Constants.SED_SCRIPT);
        BufferedReader reader = null;
        ArrayList<String> list = new ArrayList<>();
        try {
            reader = new BufferedReader(new InputStreamReader(c.getInputStream(), StandardCharsets.UTF_8), 8192);
            for (String line; (line = reader.readLine()) != null; ) {
                list.add(line.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) try {
                reader.close();
            } catch (IOException logOrIgnore) {
            }
            c.disconnect();
        }
        return list;
    }

    private static List<PlayerRating> getRawPlayerRatingsList() {
        HttpURLConnection c = openUnsecuredConnection(Constants.PLAYER_RATINGS);
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
                    PlayerRating playerRating = getPlayerFromJsonRow(playerArray.getJSONObject(i));
                    list.add(playerRating);
                }
            }
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) try {
                reader.close();
            } catch (IOException logOrIgnore) {
            }
            c.disconnect();
        }
        return list;
    }

    private static PlayerRating getPlayerFromJsonRow(JSONObject row) throws JSONException, IOException {
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
