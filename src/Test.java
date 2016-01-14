import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by admin on 2016/1/12.
 */
public class Test {
    public static void main(String[] args)throws IOException {
        System.out.println(URLEncoder.encode("https://www.zhihu.com/people/excited-vczh", "utf-8"));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Connection con = Jsoup.connect("https://www.zhihu.com/people/excited-vczh");//获取连接
            Connection.Response rs = con.execute();
            Document doc = Jsoup.parse(rs.body());
            Elements elmts = doc.select(".zm-profile-section-item.zm-item.clearfix");
            for (Element elmt : elmts) {
                System.out.println(sdf.format(new java.util.Date(Long.parseLong(elmt.attr("data-time")) * 1000)));
                System.out.println(elmt.select(".zm-profile-activity-page-item-main").text() + "www.zhihu.com" + elmt.select(".question_link").attr("href"));
            }

    }

}
