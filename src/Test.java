import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by admin on 2016/1/12.
 */
public class Test {
    public static void main(String[] args)throws IOException {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Connection con = Jsoup.connect("https://www.zhihu.com/people/rednaxelafx");//获取连接
            con.header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0");//配置模拟浏览器
            con.cookie("_xsrf", "af8cfcba675ae98ae5a6e238b2844238");
            con.cookie("_za", "7699d7fa-905b-44bf-9a08-583ca27692b3");
            Map<String, String> cookie = new HashMap<>();
            cookie.put("__utma", "51854390.1095779136.1452616328.1452616328.1452616328.1");
            cookie.put("__utmb", "51854390.14.10.1452616328");
            cookie.put("__utmc", "51854390");
            con.cookies(cookie);
            Connection.Response rs = con.execute();
            Document doc = Jsoup.parse(rs.body());
            Elements elmts = doc.select(".zm-profile-section-item.zm-item.clearfix");
            for (Element elmt : elmts) {
                System.out.println(sdf.format(new java.util.Date(Long.parseLong(elmt.attr("data-time")) * 1000)));
                System.out.println(elmt.select(".zm-profile-activity-page-item-main").text() + "www.zhihu.com" + elmt.select(".question_link").attr("href"));
            }
    }

}
