package entity;

import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.JsoupUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Cabeza on 2016/4/25.
 */
public class LoginInfo {
    public static final Logger log= LoggerFactory.getLogger(LoginInfo.class);
    private String email;
    private String password;
    private String xsrf;
    private String captcha;
    private String remeberMe;
    //最大递归次数
    private int maxRecursiveTimes=5;
    private Map<String,String> captchaCookies =new HashMap<>();
    private Map<String,String> loginCookies =new HashMap<>();

    public boolean getXsrf(int times){
        if(times>maxRecursiveTimes)
            return false;
        Connection con= JsoupUtil.getGetCon("http://www.zhihu.com");
        Response rs=null;
        try {
            rs=con.execute();
        } catch (IOException e) {
            e.printStackTrace();
            log.info("获取_xsrf第"+times+"次失败");
            return getXsrf(++times);
        }
        Document doc= Jsoup.parse(rs.body());
        xsrf=doc.select(".view.view-signin [name=\"_xsrf\"]").attr("value");
        log.info("已获得xsrf:"+xsrf);
        return true;
    }

}
