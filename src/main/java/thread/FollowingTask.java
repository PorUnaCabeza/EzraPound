package thread;

import dao.ZhihuDao;
import entity.UserRelation;
import filter.RedisFilter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.JsoupUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Cabeza on 2016/4/26.
 */
public class FollowingTask implements Runnable{
    private static Logger log= LoggerFactory.getLogger(FollowingTask.class);
    private String params;
    private String xsrf;
    private Map<String,String> loginCookies;
    private String userId;
    private String userName;

    public FollowingTask(String params, String xsrf, Map<String, String> loginCookies, String userId, String userName) {
        this.params = params;
        this.xsrf = xsrf;
        this.loginCookies = loginCookies;
        this.userId = userId;
        this.userName = userName;
    }

    @Override
    public void run() {
        getFollowingInfo(0);
    }
    public boolean getFollowingInfo(int times){
        if(times>2){
            return false;
        }
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
            log.info(params+"第"+times+"次拉取失败");
            return getFollowingInfo(++times);
        }
        JSONObject jsonObject=new JSONObject(rs.body());
        JSONArray jsonArray=jsonObject.getJSONArray("msg");
        List<UserRelation> list=new ArrayList<>();
        for(int i=0;i<jsonArray.length();i++){
            Document doc= Jsoup.parse(jsonArray.get(i).toString());
            String name = doc.select(".zm-list-content-title a").attr("title");
            String shortUrl = doc.select(".zm-list-content-title a").attr("href").replaceAll("https://www.zhihu.com/people/", "");
            log.info(name+"|"+shortUrl);
            RedisFilter.addQueue(shortUrl);
            list.add(new UserRelation(userId,userName,shortUrl,name));
        }
        ZhihuDao.saveUserRelations(list);
        return true;
    }
}
