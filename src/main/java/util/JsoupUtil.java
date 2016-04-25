package util;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

/**
 * Created by Cabeza on 2016/4/25.
 */
public class JsoupUtil {
    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/49.0.2623.87 Safari/537.36";
    public static final int TIME_OUT=30000;
    public static Connection getPostCon(String url){
        Connection con= Jsoup.connect(url).userAgent(USER_AGENT).timeout(TIME_OUT).method(Connection.Method.POST);
        return con;
    }
    public static Connection getGetCon(String url){
        Connection con= Jsoup.connect(url).userAgent(USER_AGENT).timeout(TIME_OUT).method(Connection.Method.GET);
        return con;
    }

    public static Connection getResourceCon(String url){
        Connection con=Jsoup.connect(url).userAgent(USER_AGENT).timeout(TIME_OUT).ignoreContentType(true).method(Connection.Method.GET);
        return con;
    }
}
