package za.co.sagoclubs;

public class Constants {

	// Override SAGA_WP_SITE with local IP address when testing locally
	private static final String SAGA_WP_SITE = "http://sagoclubs.co.za/";
	public static final String TAG = "SAGO";
	public static final String REFRESH_HTML = "http://rank.sagoclubs.co.za:8080/refresh.html";
	public static final String LOGGAME_CGI = "http://rank.sagoclubs.co.za:8080/loggame.cgi";
	public static final String SED_SCRIPT = "http://rank.sagoclubs.co.za:8080/showsed_raw.cgi";

	public static final String PLAYER_RATINGS = SAGA_WP_SITE + "ranks/ranks.html";
	public static final String UNDO_CGI = "http://rank.sagoclubs.co.za:8080/undo.cgi";
	// Complete showlog path with "handle.html"
	public static final String SHOWLOG = SAGA_WP_SITE + "ranks/player_files/";
}
