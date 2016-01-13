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

    private boolean isLogin = false;
    public static void main(String[] args) throws IOException{
        Captcha c=new Captcha();
        while (true) {
            c.watchNews("https://www.zhihu.com/people/rednaxelafx");
            try {
                Thread.sleep(2000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void getCaptchaCookies() {
        captchaCookies.clear();
        System.out.println(System.currentTimeMillis());
        Connection con = Jsoup.connect("https://www.zhihu.com/captcha.gif?r="+System.currentTimeMillis()).ignoreContentType(true);//获取连接
        con.header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0");
        try {
            rs = con.execute();
        } catch (Exception e) {
            System.out.println("获得验证码cookie失败");
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
        System.out.println("验证码已保存" + ",路径为:" + file.getAbsolutePath());
    }

    public void getXsrf() {
        Connection con = Jsoup.connect("http://www.zhihu.com");
        con.header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0");
        try {
            rs = con.execute();
        } catch (Exception e) {
            System.out.println("获得Xsrf失败");
            return;
        }
        Document doc=Jsoup.parse(rs.body());
        _xsrf=doc.select(".view.view-signin [name=\"_xsrf\"]").attr("value");
        System.out.println("已获得xsrf");
    }

    public void getLoginCookies() {
        loginCookies.clear();
        Scanner sc=new Scanner(System.in);
        getXsrf();
        getCaptchaCookies();
        System.out.println("请输入帐号");
        String userName=sc.nextLine();
        System.out.println("请输入密码");
        String passWord=sc.nextLine();
        System.out.println("请打开工程路径查看验证码并输入");
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
            System.out.println("获得loginCookies失败");
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
        System.out.println("cookies已保存");
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
        System.out.println(loginCookies);
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
            System.out.println("通过帐号密码验证码登录失败");
            return false;
        }
        Document doc = Jsoup.parse(rs.body());
        if (checkLogin(doc)) {
            saveCookies("zhihu_cookies.txt", loginCookies);
            return true;
        }
        System.out.println("登录失败");
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
            System.out.println("读取Cookie登录失败");
            return false;
        }
        Document doc = Jsoup.parse(rs.body());
        if (checkLogin(doc))
            return true;
        System.out.println("读取cookie登录失败,下面手动登录:");
        return login();
    }
    public boolean checkLogin(Document doc) {
        Elements elmts=doc.select(".zu-top-nav-userinfo");
        if(!elmts.isEmpty()){
            System.out.println("登录成功！"+"登录用户为:"+elmts.select(".name").text());
            isLogin = true;
            return true;
        }
        isLogin = false;
        return false;
    }

    public void watchNews(String url) {
        if (!isLogin) {
            if (!loginBySavedCookies()) {
                System.out.println("网络貌似不太好");
                return;
            }
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Connection con = Jsoup.connect(url);//获取连接
        con.header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0");//配置模拟浏览器
        con.cookies(loginCookies);
        try {
            rs = con.execute();
        } catch (Exception e) {
            System.out.println("拉取动态失败");
        }
        Document doc = Jsoup.parse(rs.body());
        checkLogin(doc);
        Elements elmts = doc.select(".zm-profile-section-item.zm-item.clearfix");
        for (Element elmt : elmts) {
            System.out.println(sdf.format(new java.util.Date(Long.parseLong(elmt.attr("data-time")) * 1000)));
            System.out.println(elmt.select(".zm-profile-activity-page-item-main").text() + "www.zhihu.com" + elmt.select(".question_link").attr("href"));
        }
        System.out.println("*************" + sdf.format(System.currentTimeMillis()) + "*************");
    }
}
