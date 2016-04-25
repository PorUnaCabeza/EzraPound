package entity;

import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.EzraPoundUtil;
import util.JsoupUtil;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by Cabeza on 2016/4/25.
 */
public class LoginInfo {
    public static final Logger log = LoggerFactory.getLogger(LoginInfo.class);
    private String email;
    private String password;
    private String xsrf = "";
    private String captcha;
    private String remeberMe = "true";
    private String userName;
    private Boolean isLogin = false;
    //最大递归次数
    private int maxRecursiveTimes = 5;
    private Map<String, String> captchaCookies = new HashMap<>();
    private Map<String, String> loginCookies = new HashMap<>();

    public boolean getXsrf(int times) {
        if (times > maxRecursiveTimes)
            return false;
        Connection con = JsoupUtil.getGetCon("http://www.zhihu.com");
        Response rs = null;
        try {
            rs = con.execute();
        } catch (IOException e) {
            e.printStackTrace();
            log.info("获取_xsrf第" + times + "次失败");
            return getXsrf(++times);
        }
        Document doc = Jsoup.parse(rs.body());
        xsrf = doc.select(".view.view-signin [name=\"_xsrf\"]").attr("value");
        log.info("已获得xsrf:" + xsrf);
        return true;
    }

    public boolean getCaptchaImgAndCookies(int times) {
        captchaCookies.clear();
        if (times > maxRecursiveTimes)
            return false;
        Connection con = JsoupUtil.getResourceCon("https://www.zhihu.com/captcha.gif?r=" + System.currentTimeMillis() + "&type=login");
        Response rs = null;
        try {
            rs = con.execute();
        } catch (IOException e) {
            e.printStackTrace();
            log.info("获取验证码第" + times + "次失败");
            return getCaptchaImgAndCookies(++times);
        }
        File file = new File(EzraPoundUtil.CAPTCHA_DIR);
        try {
            FileOutputStream out = (new FileOutputStream(file));
            out.write(rs.bodyAsBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        captchaCookies.putAll(rs.cookies());
        log.info("验证码已保存" + ",路径为:" + file.getAbsolutePath());
        log.info("验证码对应cookie为:" + captchaCookies);
        return true;
    }

    public boolean loginByEmailAndPwd() {
        loginCookies.clear();
        Scanner sc = new Scanner(System.in);
        getCaptchaImgAndCookies(0);
        log.info("请输入账号:");
        email = sc.nextLine();
        log.info("请输入密码");
        password = sc.nextLine();
        log.info("查看验证码并输入");
        captcha = sc.nextLine();
        Connection con = JsoupUtil.getPostCon("https://www.zhihu.com/login/email");
        Response rs = null;
        try {
            rs = con.data("_xsrf", xsrf)
                    .data("email", email)
                    .data("password", password)
                    .data("remember_me", remeberMe)
                    .data("captcha", captcha)
                    .cookies(captchaCookies)
                    .ignoreContentType(true)
                    .execute();
        } catch (IOException e) {
            e.printStackTrace();
            log.info("通过账号密码登录发生异常");
            return false;
        }

        JSONObject jsonObject = new JSONObject(rs.body());
        String result = jsonObject.get("r").toString();
        log.info(EzraPoundUtil.unicode2Character(jsonObject.get("msg").toString()));

        Response rs2 = null;
        try {
            rs2 = JsoupUtil.getGetCon("https://www.zhihu.com").cookies(rs.cookies()).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (checkLogin(Jsoup.parse(rs2.body()))) {
            loginCookies.putAll(rs.cookies());
            saveCookies(EzraPoundUtil.LOGIN_COOKIES_DIR, loginCookies);
            return true;
        }
        return false;
    }

    public boolean loginBySavedCookies() {
        loginCookies.clear();
        readCookies(EzraPoundUtil.LOGIN_COOKIES_DIR, loginCookies);
        Connection con = JsoupUtil.getGetCon("https://www.zhihu.com");
        Response rs = null;
        try {
            rs = con.cookies(loginCookies).execute();
        } catch (IOException e) {
            e.printStackTrace();
            log.info("携带cookie登录测试失败");
            return false;
        }
        return checkLogin(Jsoup.parse(rs.body()));
    }

    public boolean login() {
        getXsrf(0);
        if (isLogin) {
            log.info("已登录,登录用户：" + userName);
            return true;
        }
        if (!loginBySavedCookies()) {
            log.info("读取cookie登录失败,下面手动登录");
            if (loginByEmailAndPwd()) {
                return true;
            } else {
                System.exit(0);
                log.info("请检查账号密码与网络配置");
            }
        }
        return true;
    }

    public void readCookies(String filename, Map<String, String> cookies) {
        cookies.clear();
        if (!new File(filename).exists()) {
            log.info(filename + "不存在");
            return;
        }
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
                cookies.put(
                        str.substring(0, index),
                        str.substring(index + 1, str.length())
                );
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            pw.println(entry.getKey() + "=" + entry.getValue().replace("\"", ""));
        }
        pw.close();
        log.info("cookies已保存");
    }

    public boolean checkLogin(Document doc) {
        Elements elmts = doc.select(".zu-top-nav-userinfo");
        if (!elmts.isEmpty()) {
            userName = elmts.select(".name").text();
            log.info("登录成功！" + "登录用户为:" + userName);
            isLogin = true;
            return true;
        }
        log.info("未登录");
        isLogin = false;
        return false;
    }

    public String getEmail() {
        return email;
    }

    public String getXsrf() {
        return xsrf;
    }

    public String getUserName() {
        return userName;
    }

    public Boolean getIsLogin() {
        return isLogin;
    }

    public Map<String, String> getCaptchaCookies() {
        return captchaCookies;
    }

    public Map<String, String> getLoginCookies() {
        return loginCookies;
    }
}
