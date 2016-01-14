import net.sf.json.JSONObject;
import org.apache.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by Cabeza on 2016/1/13.
 */
public class Captcha {
    private Map<String,String> captchaCookies =new HashMap<>();
    private Map<String,String> loginCookies=new HashMap<>();
    private String _xsrf;
    private Connection.Response rs;
    private static Logger log=Logger.getLogger(Captcha.class);

    private boolean isLogin = false;
    public static void main(String[] args) throws IOException{
        String userHomeUrl="https://www.zhihu.com/people/excited-vczh";
        Captcha c=new Captcha();
        while (true) {
           // c.watchNews(userHomeUrl);
            c.traverseNews(userHomeUrl);
            try {
                Thread.sleep(2000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void getCaptchaCookies() {
        captchaCookies.clear();
        log.info(System.currentTimeMillis());
        Connection con = Jsoup.connect("https://www.zhihu.com/captcha.gif?r="+System.currentTimeMillis()).ignoreContentType(true);//获取连接
        con.header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0");
        try {
            rs = con.execute();
        } catch (Exception e) {
            log.info("获得验证码cookie失败");
            return;
        }
        File file = new File("cabeza.gif");
        try {
            FileOutputStream out = (new FileOutputStream(file));
            out.write(rs.bodyAsBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        captchaCookies.putAll(rs.cookies());
        log.info("验证码已保存" + ",路径为:" + file.getAbsolutePath());
    }

    public void getXsrf() {
        Connection con = Jsoup.connect("http://www.zhihu.com");
        con.header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0");
        try {
            rs = con.execute();
        } catch (Exception e) {
            log.info("获得Xsrf失败");
            return;
        }
        Document doc=Jsoup.parse(rs.body());
        _xsrf=doc.select(".view.view-signin [name=\"_xsrf\"]").attr("value");
        log.info("已获得xsrf");
    }

    public void getLoginCookies() {
        loginCookies.clear();
        Scanner sc=new Scanner(System.in);
        getXsrf();
        getCaptchaCookies();
        log.info("请输入帐号");
        String userName=sc.nextLine();
        log.info("请输入密码");
        String passWord=sc.nextLine();
        log.info("请打开工程路径查看验证码并输入");
        String captcha=sc.nextLine();
        Connection con = Jsoup.connect("https://www.zhihu.com/login/email");
        con.header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0");
        try {
            rs = con.ignoreContentType(true).method(Connection.Method.POST)
                    .data("_xsrf", _xsrf)
                    .data("email", userName)
                    .data("password", passWord)
                    .data("captcha", captcha).cookies(captchaCookies).execute();
        } catch (Exception e) {
            log.info("获得loginCookies失败");
            return;
        }
        loginCookies.putAll(rs.cookies());
    }

    public void saveCookies(String fileName, Map<String, String> cookies) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedOutputStream bos
                = new BufferedOutputStream(fos);
        PrintWriter pw = new PrintWriter(bos);
        for (Map.Entry<String, String> entry : cookies.entrySet()) {
            pw.println(entry.getKey() + "=" + entry.getValue().replace("\"",""));
        }
        pw.close();
        log.info("cookies已保存");
    }

    public void readCookies(String filename) {
        loginCookies.clear();
        try {
            FileInputStream fis
                    = new FileInputStream(filename);
            InputStreamReader isr
                    = new InputStreamReader(fis);
            BufferedReader br
                    = new BufferedReader(isr);
            String str = null;
            while ((str = br.readLine()) != null) {
                int index = str.indexOf("=");
                loginCookies.put(
                        str.substring(0, index),
                        str.substring(index + 1, str.length())
                );
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.info(loginCookies);
    }

    public boolean login() {
        getLoginCookies();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Connection con = Jsoup.connect("https://www.zhihu.com");//获取连接
        con.header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0");//配置模拟浏览器
        con.cookies(loginCookies);
        try {
            rs = con.execute();
        } catch (Exception e) {
            log.info("通过帐号密码验证码登录失败");
            return false;
        }
        Document doc = Jsoup.parse(rs.body());
        if (checkLogin(doc)) {
            saveCookies("zhihu_cookies.txt", loginCookies);
            return true;
        }
        log.info("登录失败");
        return false;

    }

    public boolean loginBySavedCookies() {
        readCookies("zhihu_cookies.txt");
        Connection con = Jsoup.connect("https://www.zhihu.com");//获取连接
        con.header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0");//配置模拟浏览器
        con.cookies(loginCookies);
        try {
            rs = con.execute();
        } catch (Exception e) {
            log.info("读取Cookie登录失败");
            return false;
        }
        Document doc = Jsoup.parse(rs.body());
        if (checkLogin(doc))
            return true;
        log.info("读取cookie登录失败,下面手动登录:");
        return login();
    }
    public boolean checkLogin(Document doc) {
        Elements elmts=doc.select(".zu-top-nav-userinfo");
        if(!elmts.isEmpty()){
            log.info("登录成功！" + "登录用户为:" + elmts.select(".name").text());
            isLogin = true;
            getXsrf();
            return true;
        }
        isLogin = false;
        return false;
    }

    public void watchNews(String url) {
        if (!isLogin) {
            if (!loginBySavedCookies()) {
                log.info("网络貌似不太好");
                return;
            }
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Connection con = Jsoup.connect(url).timeout(3000);//获取连接
        con.header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0");//配置模拟浏览器
        con.cookies(loginCookies);
        try {
            rs = con.execute();
        } catch (Exception e) {
            log.info("拉取动态失败");
            e.printStackTrace();
        }
        Document doc = Jsoup.parse(rs.body());
        checkLogin(doc);
        Elements elmts = doc.select(".zm-profile-section-item.zm-item.clearfix");
        for (Element elmt : elmts) {
            log.info(sdf.format(new java.util.Date(Long.parseLong(elmt.attr("data-time")) * 1000)));
            log.info(elmt.select(".zm-profile-activity-page-item-main").text() + "www.zhihu.com" + elmt.select(".question_link").attr("href"));
            log.info(elmt.elementSiblingIndex());
        }
        log.info("*************" + sdf.format(System.currentTimeMillis()) + "*************");
    }
    public void traverseNews(String url){
        String start=null;
        int lastIndex=0;
        if (!isLogin) {
            if (!loginBySavedCookies()) {
                log.info("网络貌似不太好");
                return;
            }
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Connection con = Jsoup.connect(url).timeout(3000);//获取连接
        con.header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0");//配置模拟浏览器
        //con.cookies(loginCookies);
        try {
            rs = con.execute();
        } catch (Exception e) {
            log.info("首次拉取动态失败");
            e.printStackTrace();
        }
        Document doc = Jsoup.parse(rs.body());
        checkLogin(doc);
        Elements elmts = doc.select(".zm-profile-section-item.zm-item.clearfix");
        while(!elmts.isEmpty()){
            for (Element elmt : elmts) {
                if(elmt.select(".zm-profile-activity-page-item-main").text().matches(".*(somekeyword|.*).*")){
                    log.info(sdf.format(new java.util.Date(Long.parseLong(elmt.attr("data-time")) * 1000)));
                    log.info(elmt.select(".zm-profile-activity-page-item-main").text() + "www.zhihu.com" + elmt.select(".question_link").attr("href"));
                }
                start=elmt.attr("data-time");
                lastIndex=elmt.elementSiblingIndex();
            }
            Connection con1 = Jsoup.connect(url+"/activities").method(Connection.Method.POST).timeout(3000).ignoreContentType(true);//获取连接
            con1.header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0");//配置模拟浏览器
            con1.data("start",start).data("_xsrf", _xsrf);
            con1.cookies(loginCookies);
            con1.cookie("_xsrf",_xsrf);
            try {
                rs = con1.execute();
            } catch (Exception e) {
                log.info("-------拉取更多动态失败");
                e.printStackTrace();
                return;
            }
            JSONObject jsonObj=JSONObject.fromString(rs.body());
            doc=Jsoup.parse(jsonObj.getJSONArray("msg").get(1).toString());
            elmts = doc.select(".zm-profile-section-item.zm-item.clearfix");
        }
        log.info("拉取完毕");
        System.exit(0);

    }
}
