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
public class ZhihuUtil {
    public static final String userAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0";
    private static Logger log=Logger.getLogger(Captcha.class);
    private Date date = new Date();

    private Map<String,String> captchaCookies =new HashMap<>();
    private Map<String,String> loginCookies =new HashMap<>();
    private Map<String,String> updateNews=new HashMap<>();

    private boolean recentNewsTimeInit=false;
    private boolean isLogin = false;
    private boolean getXsrf=false;

    private Scanner sc=new Scanner(System.in);

    private String _xsrf;
    private String recentNewsTime="0";
    private StringBuffer emailContent=new StringBuffer();
    private Connection con;
    private Connection.Response rs;
    private Document doc;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private String traverseStartSign = "";
    private int getXsrfTimes=0;
    private int loginBySavedCookiesTimes=0;

    public boolean getXsrf() {
        con = Jsoup.connect("http://www.zhihu.com").timeout(30000);
        con.header("User-Agent", userAgent);
        try {
            getXsrfTimes++;
            rs = con.execute();
        } catch (Exception e) {
            log.info("获得Xsrf失败");
            if(getXsrfTimes<10)
                return getXsrf();
            getXsrf=false;
            return false;
        }
        Document doc=Jsoup.parse(rs.body());
        _xsrf=doc.select(".view.view-signin [name=\"_xsrf\"]").attr("value");
        log.info("已获得xsrf");
        getXsrf=true;
        return true;
    }

    public void getCaptchaCookies() {
        captchaCookies.clear();
        con = Jsoup.connect("https://www.zhihu.com/captcha.gif?r=" + System.currentTimeMillis())
                .ignoreContentType(true)
                .timeout(30000);//获取连接
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

    public boolean getLoginCookies() {
        loginCookies.clear();
        getXsrf();
        getCaptchaCookies();
        log.info("请输入帐号");
        String userName=sc.nextLine();
        log.info("请输入密码");
        String passWord=sc.nextLine();
        log.info("请打开工程路径查看验证码并输入");
        String captcha=sc.nextLine();
        con = Jsoup.connect("https://www.zhihu.com/login/email").timeout(30000);
        con.header("User-Agent", userAgent);
        try {
            rs = con.ignoreContentType(true).method(Connection.Method.POST)
                    .data("_xsrf", _xsrf)
                    .data("email", userName)
                    .data("password", passWord)
                    .data("captcha", captcha).cookies(captchaCookies).execute();
        } catch (Exception e) {
            log.info("获得loginCookies失败");
            return getLoginCookies();
        }
        loginCookies.putAll(rs.cookies());
        return true;
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
    }

    public boolean loginBySavedCookies() {
        if(!getXsrf)
            getXsrf();
        readCookies("zhihu_cookies.txt");
        con = Jsoup.connect("https://www.zhihu.com").timeout(30000);//获取连接
        con.header("User-Agent", userAgent);//配置模拟浏览器
        con.cookies(loginCookies);
        try {
            rs = con.execute();
        } catch (Exception e) {
            log.info("读取cookie登录失败");
            return false;
        }
        return checkLogin(Jsoup.parse(rs.body()));
    }

    public boolean loginByEmailAndPwd() {
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

    public boolean login(){
        log.info("登录状态:"+isLogin);
        if(isLogin){
            return true;
        }
        if(!loginBySavedCookies()){
            log.info("读取cookie登录失败,下面手动登录");
            return loginByEmailAndPwd();
        }
        return true;
    }

    public boolean checkLogin(Document doc) {
        Elements elmts=doc.select(".zu-top-nav-userinfo");
        if(!elmts.isEmpty()){
            log.info("登录成功！" + "登录用户为:" + elmts.select(".name").text());
            isLogin = true;
            return true;
        }
        log.info("check over,no login");
        isLogin = false;
        return false;
    }

    public void watchNews(String url) {
        login();
        con = Jsoup.connect(url).timeout(30000);//获取连接
        con.header("User-Agent", userAgent);//配置模拟浏览器
        con.cookies(loginCookies);
        con.cookie("_xsrf",_xsrf);
        try {
            rs = con.execute();
        } catch (Exception e) {
            log.info("拉取动态失败");
            e.printStackTrace();
            return;
        }
        doc = Jsoup.parse(rs.body());
        doc.setBaseUri("https://www.zhihu.com");
        updateNews.clear();
        Elements elmts = doc.select(".zm-profile-section-item.zm-item.clearfix");
        for (Element elmt : elmts) {
            if(elmt.attr("data-time").compareTo(recentNewsTime)>0&&recentNewsTimeInit){
                updateNews.put(elmt.attr("data-time"),elmt.select(".zm-profile-activity-page-item-main").text()
                        + "   " + elmt.select(".zm-profile-activity-page-item-main > a").last().attr("abs:href"));
            }
        }
        if(updateNews.isEmpty())
            log.info("无新动态");
        else{
            log.info("新动态: "+updateNews);
            sendMail(updateNews);
        }
        recentNewsTime=elmts.get(0).attr("data-time");
        recentNewsTimeInit=true;
        log.info("*************" + sdf.format(System.currentTimeMillis()) + "*************");
    }

    public void traverseNews(String url){
        Elements elmts;
        login();
        if (traverseStartSign.isEmpty()) {
            con = Jsoup.connect(url).timeout(30000);//获取连接
            con.header("User-Agent", userAgent);//配置模拟浏览器
            con.cookies(loginCookies);
            try {
                rs = con.execute();
            } catch (Exception e) {
                log.info("首次拉取动态失败");
                e.printStackTrace();
                return;
            }
            doc = Jsoup.parse(rs.body());
        } else {
            date.setTime((Long.parseLong(traverseStartSign) * 1000));
            log.info(sdf.format(date));
            log.info("爬取因网络原因中断,现从该用户" + sdf.format(date) + "处重新爬取");
            con = Jsoup.connect(url + "/activities").method(Connection.Method.POST).timeout(3000).ignoreContentType(true);
            con.header("User-Agent", userAgent);
            con.data("start", traverseStartSign).data("_xsrf", _xsrf);
            con.cookies(loginCookies);
            con.cookie("_xsrf", _xsrf);
            con.timeout(30000);
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
            }
            con = Jsoup.connect(url + "/activities").method(Connection.Method.POST).timeout(3000).ignoreContentType(true);//获取连接
            con.header("User-Agent", userAgent);//配置模拟浏览器
            con.data("start", traverseStartSign).data("_xsrf", _xsrf);
            con.cookies(loginCookies);
            con.cookie("_xsrf", _xsrf);
            con.timeout(30000);
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

    public void startWatch(String url,long millis){
        while(true){
            watchNews(url);
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void startTraverse(String url){
        while(true){
            traverseNews(url);
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendMail(Map<String,String> news) {
        Properties props=new Properties();
        try {
            ClassLoader loader = ZhihuUtil.class.getClassLoader();
            InputStream in = loader.getResourceAsStream("mailConfig.properties");
            props.load(in);
            System.out.println(props);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Session session = Session.getInstance(props);
        Transport ts = null;
        try {
            ts = session.getTransport();
            ts.connect(props.getProperty("mail.smtp.host"), props.getProperty("username"), props.getProperty("password"));
            MimeMessage message = new MimeMessage(session);
            //发件人
            message.setFrom(new InternetAddress("makpia@163.com"));
            //收件人
            message.setRecipient(Message.RecipientType.TO, new InternetAddress("makpia@163.com"));
            message.setSubject(sdf.format(System.currentTimeMillis()) + "有新动态");
            emailContent.setLength(0);
            for (Map.Entry<String, String> entry : news.entrySet()) {
                date.setTime(Long.parseLong(entry.getKey()) * 1000);
                emailContent.append(sdf.format(date)+entry.getValue()+"<br>");
            }
            message.setContent(emailContent.toString(), "text/html;charset=UTF-8");
            ts.sendMessage(message, message.getAllRecipients());
            log.info("邮件发送成功!");
        } catch (Exception e) {
            log.info("发送邮件失败,请检查邮件配置");
            e.printStackTrace();
        } finally {
            try {
                ts.close();
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        }
    }

    public void peopleAnswer2Epub(boolean orderyByVoteNum,String url){

    }
    public void topicAnswer2Epub(String url){

    }
}
