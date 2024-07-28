package za.co.sagoclubs;

public class Constants {

    // Override SAGA_WP_SITE with local IP address when testing locally
    private static final String SAGA_WP_SITE = "https://sagoclubs.co.za/";
    private static final String RANK_SITE = "https://rank-test.sagoclubs.co.za/";
    public static final String TAG = "SAGO";
    public static final String REFRESH_HTML = RANK_SITE + "refresh.html";
    public static final String LOGGAME_CGI = RANK_SITE + "loggame";
    public static final String SHOW_HANDLES = RANK_SITE + "showhandles";
    public static final String PLAYER_RATINGS = SAGA_WP_SITE + "ranks/ranks.json";
    public static final String UNDO_CGI = RANK_SITE + "undo";
    // Complete showlog path with "handle.html"
    public static final String SHOWLOG = SAGA_WP_SITE + "ranks/player_files/";
    public static final String SHOW_LOG_DIRECT = RANK_SITE + "showlog";
}
