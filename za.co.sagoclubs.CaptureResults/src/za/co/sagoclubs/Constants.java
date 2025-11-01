package za.co.sagoclubs;

public class Constants {

    // Set SAGA_WP_SITE and RANK_SITE in secret.properties file locally
    // Add secret.properties to .gitignore

    public static final String TAG = "SAGO";
    public static final String REFRESH_PATH = BuildConfig.RANK_SITE;
    public static final String LOGGAME_CGI = BuildConfig.RANK_SITE + "loggame.cgi";
    public static final String SHOW_HANDLES = BuildConfig.RANK_SITE + "showhandles.cgi";
    public static final String PLAYER_RATINGS = BuildConfig.SAGA_WP_SITE + "ranks/ranks.json";
    public static final String UNDO_CGI = BuildConfig.RANK_SITE + "undolatest.cgi";
    // Complete showlog path with "handle.html"
    public static final String SHOWLOG = BuildConfig.SAGA_WP_SITE + "ranks/player_files/";
    public static final String SHOW_LOG_DIRECT = BuildConfig.RANK_SITE + "showlog.cgi";
}
