package thread;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import util.JedisUtil;
import util.JsoupUtil;

import java.io.IOException;
import java.util.Map;

/**
 * Created by Cabeza on 2016/4/26.
 */
public class FollowingTask implements Runnable{
    private static Logger log= LoggerFactory.getLogger(FollowingTask.class);
    private static Jedis jedis=JedisUtil.getJedis();
    private String params;
    private String xsrf;
    private Map<String,String> loginCookies;

    public FollowingTask(String params, String xsrf, Map<String, String> loginCookies) {
        this.params = params;
        this.xsrf = xsrf;
        this.loginCookies = loginCookies;
    }

    @Override
    public void run() {
        Connection con= JsoupUtil.getPostCon("https://www.zhihu.com/node/ProfileFolloweesListV2");
        Response rs=null;
        try {
            rs=con.ignoreContentType(true)
                    .cookies(loginCookies)
                    .cookie("_xsrf", xsrf)
                    .data("method", "next")
                    .data("_xsrf", xsrf)
                    .data("params", params)
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
            log.info(params+"拉取失败");
            return;
        }
        JSONObject jsonObject=new JSONObject(rs.body());
        JSONArray jsonArray=jsonObject.getJSONArray("msg");
        for(int i=0;i<jsonArray.length();i++){
            Document doc= Jsoup.parse(jsonArray.get(i).toString());
            String name = doc.select(".zm-list-content-title a").attr("title");
            String shortUrl = doc.select(".zm-list-content-title a").attr("href").replaceAll("https://www.zhihu.com/people/", "");
            log.info(name+"|"+shortUrl);
            jedis.sadd("queue",shortUrl);
        }
    }
}
