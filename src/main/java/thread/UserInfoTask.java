package thread;

import entity.User;
import filter.RedisFilter;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.JsoupUtil;

import java.io.IOException;
import java.util.Map;

/**
 * Created by Cabeza on 2016/4/26.
 */
public class UserInfoTask implements Runnable{
    private static Logger log= LoggerFactory.getLogger(UserInfoTask.class);
    private String xsrf;
    private Map<String,String> loginCookies;
    private String userId;
    private ThreadPool threadPool;

    public UserInfoTask(String xsrf, Map<String, String> loginCookies, String userId, ThreadPool threadPool) {
        this.xsrf = xsrf;
        this.loginCookies = loginCookies;
        this.userId = userId;
        this.threadPool = threadPool;
    }

    @Override
    public void run() {
        Connection con = JsoupUtil.getGetCon("https://www.zhihu.com/people/" + userId);
        Connection.Response rs = null;
        try {
            rs = con.cookies(loginCookies).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Document doc = Jsoup.parse(rs.body());
        String name = doc.select(".zm-profile-header-main .name").text();
        String bio = doc.select(".zm-profile-header-main .bio").text();
        String location = doc.select(".zm-profile-header-main .location").text();
        String business = doc.select(".zm-profile-header-main .business").text();
        String gender = doc.select(".zm-profile-header-main .gender i").hasClass("icon-profile-male") ? "男" : "女";
        String education = doc.select(".zm-profile-header-main .education").text();
        String educationExtra = doc.select(".zm-profile-header-main .education-extra").text();
        String description = doc.select(".zm-profile-header-main .content").text();
        String agree = doc.select(".zm-profile-header-user-agree strong").text();
        String thanks = doc.select(".zm-profile-header-user-thanks strong").text();
        String asks = doc.select(".profile-navbar a .num").eq(0).text();
        String answers = doc.select(".profile-navbar a .num").eq(1).text();
        String posts = doc.select(".profile-navbar a .num").eq(2).text();
        String collections = doc.select(".profile-navbar a .num").eq(3).text();
        String logs = doc.select(".profile-navbar a .num").eq(4).text();
        String following = doc.select(".zm-profile-side-following strong").eq(0).text();
        String followers = doc.select(".zm-profile-side-following strong").eq(1).text();
        String hashId=doc.select(".zm-profile-header-op-btns button").attr("data-id");
        if(hashId.isEmpty()||hashId==null){
            hashId=doc.select("script[data-name=ga_vars]").toString().replaceAll("(.*?user_hash\":\")|(\"}.*?)|<(/{0,1})script>","");
            log.info("与登录用户一致,从js脚本中获得hash_id:"+hashId);
        }
        User user = new User(userId, name, bio, location, business, gender, education, educationExtra,
                description, agree, thanks, asks, answers, posts, collections, logs, following, followers,hashId);
        log.info(user.toString());
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("offset", 0);
        jsonObject.put("order_by", "created");
        jsonObject.put("hash_id",hashId);
        for(int i=0;i<Integer.parseInt(following);i+=20){
            jsonObject.put("offset",i);
            threadPool.execute(new FollowingTask(jsonObject.toString(),xsrf,loginCookies));
        }
    }
}
