package za.co.sagoclubs;

public class Constants {

    // Set SAGA_WP_SITE and RANK_SITE in secret.properties file locally
    // Add secret.properties to .gitignore

    public static final String TAG = "SAGO";
    public static final String API = "/api/";
    public static final String LOG_GAME = "loggame";
    public static final String SHOW_HANDLES = "showhandles";
    public static final String PLAYER_RATINGS = "/ranks/ranks.json";
    public static final String UNDO_LATEST = "undolatest";
    // Complete show log path with "handle.html"
    public static final String SHOW_LOG = "/ranks/player_files/";
    public static final String SHOW_LOG_DIRECT = "showlog";
}
