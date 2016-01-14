import net.sf.json.JSONObject;
import org.apache.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Cabeza on 2016/1/13.
 *
 * The apparition of these faces in the crowd;
 * Petals on a wet, black bough.
 *         --------In a Station of the Metro
 */
public class Captcha {
    private String userAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0";
    private Date date = new Date();

    private Map<String,String> captchaCookies =new HashMap<>();
    private Map<String,String> loginCookies=new HashMap<>();
    private String _xsrf;
    private Connection.Response rs;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private String traverseStartSign = "";
    private static Logger log=Logger.getLogger(Captcha.class);
    private Connection con;

    private boolean isLogin = false;
    public static void main(String[] args) throws IOException{
        String userHomeUrl = "https://www.zhihu.com/people/lin-shen-shi-jian-lu";
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
        con = Jsoup.connect("https://www.zhihu.com/captcha.gif?r=" + System.currentTimeMillis()).ignoreContentType(true);//获取连接
        con.header("User-Agent", userAgent);
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
        con = Jsoup.connect("http://www.zhihu.com");
        con.header("User-Agent", userAgent);
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
        con = Jsoup.connect("https://www.zhihu.com/login/email");
        con.header("User-Agent", userAgent);
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
        con = Jsoup.connect("https://www.zhihu.com");//获取连接
        con.header("User-Agent", userAgent);//配置模拟浏览器
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
        con = Jsoup.connect("https://www.zhihu.com");//获取连接
        con.header("User-Agent", userAgent);//配置模拟浏览器
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
        con = Jsoup.connect(url).timeout(3000);//获取连接
        con.header("User-Agent", userAgent);//配置模拟浏览器
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
            date.setTime((Long.parseLong(elmt.attr("data-time")) * 1000));
            log.info(sdf.format(date));
            log.info(elmt.select(".zm-profile-activity-page-item-main").text() + "www.zhihu.com" + elmt.select(".question_link").attr("href"));
            log.info(elmt.elementSiblingIndex());
        }
        log.info("*************" + sdf.format(System.currentTimeMillis()) + "*************");
    }
    public void traverseNews(String url){
        Document doc;
        Elements elmts;
        String start=null;
        int lastIndex=0;
        if (!isLogin) {
            if (!loginBySavedCookies()) {
                log.info("网络貌似不太好");
                return;
            }
        }
        if (traverseStartSign.isEmpty()) {
            con = Jsoup.connect(url).timeout(3000);//获取连接
            con.header("User-Agent", userAgent);//配置模拟浏览器
            //con.cookies(loginCookies);       //注:对于有些用户(如vczh),携带登录cookie爬取会失败，原因不明
            try {
                rs = con.execute();
            } catch (Exception e) {
                log.info("首次拉取动态失败");
                e.printStackTrace();
                return;
            }
            doc = Jsoup.parse(rs.body());
            checkLogin(doc);
        } else {
            date.setTime((Long.parseLong(traverseStartSign) * 1000));
            log.info(sdf.format(date));
            log.info("爬取因网络原因中断,现从该用户" + sdf.format(date) + "处重新爬取");
            con = Jsoup.connect(url + "/activities").method(Connection.Method.POST).timeout(3000).ignoreContentType(true);
            con.header("User-Agent", userAgent);
            con.data("start", traverseStartSign).data("_xsrf", _xsrf);
            con.cookies(loginCookies);
            con.cookie("_xsrf", _xsrf);
            try {
                rs = con.execute();
            } catch (Exception e) {
                log.info("-------拉取更多动态失败");
                e.printStackTrace();
                return;
            }
            JSONObject jsonObj = JSONObject.fromString(rs.body());
            doc = Jsoup.parse(jsonObj.getJSONArray("msg").get(1).toString());
        }
        elmts = doc.select(".zm-profile-section-item.zm-item.clearfix");
        while(!elmts.isEmpty()){
            for (Element elmt : elmts) {
                if (elmt.select(".zm-profile-activity-page-item-main").text().matches(".*(keyword1|.*).*")) {
                    date.setTime(Long.parseLong(elmt.attr("data-time")) * 1000);
                    log.info(sdf.format(date));
                    log.info(elmt.select(".zm-profile-activity-page-item-main").text() + "www.zhihu.com" + elmt.select(".question_link").attr("href"));
                }
                traverseStartSign = elmt.attr("data-time");
                lastIndex=elmt.elementSiblingIndex();
            }
            con = Jsoup.connect(url + "/activities").method(Connection.Method.POST).timeout(3000).ignoreContentType(true);//获取连接
            con.header("User-Agent", userAgent);//配置模拟浏览器
            con.data("start", traverseStartSign).data("_xsrf", _xsrf);
            con.cookies(loginCookies);
            con.cookie("_xsrf", _xsrf);
            try {
                rs = con.execute();
            } catch (Exception e) {
                log.info("-------爬取更多动态失败--------");
                return;
            }
            JSONObject jsonObj=JSONObject.fromString(rs.body());
            doc=Jsoup.parse(jsonObj.getJSONArray("msg").get(1).toString());
            elmts = doc.select(".zm-profile-section-item.zm-item.clearfix");
        }
        log.info("拉取完毕");
        System.exit(0);

    }

    public void sendMail(String title, String content) {
        Properties props = new Properties();
        props.setProperty("mail.smtp.auth", "true");
        props.setProperty("mail.smtp.host", "smtp.163.com");
        props.setProperty("mail.transport.protocol", "smtp");
        Session session = Session.getInstance(props);
        Transport ts = null;
        try {
            ts = session.getTransport();
            ts.connect("smtp.163.com", "makpia@163.com", "password");
            MimeMessage message = new MimeMessage(session);
            //发件人
            message.setFrom(new InternetAddress("makpia@163.com"));
            //收件人
            message.setRecipient(Message.RecipientType.TO, new InternetAddress("makpia@163.com"));
            message.setSubject(title);
            message.setContent(content, "text/html;charset=UTF-8");
            ts.sendMessage(message, message.getAllRecipients());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                ts.close();
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        }
    }
}
