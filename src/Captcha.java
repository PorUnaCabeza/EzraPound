import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
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
    public static void main(String[] args) throws IOException{
        Captcha c=new Captcha();
        c.loginBySavedCookies();
     //   c.login();

    }
    public void getCaptchaCookies()throws IOException{
        captchaCookies.clear();
        System.out.println(System.currentTimeMillis());
        Connection con = Jsoup.connect("https://www.zhihu.com/captcha.gif?r="+System.currentTimeMillis()).ignoreContentType(true);//获取连接
        con.header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0");
        Connection.Response rs = con.execute();
        FileOutputStream out = (new FileOutputStream(new java.io.File("cabeza.gif")));
        out.write(rs.bodyAsBytes());
        captchaCookies.putAll(rs.cookies());
        System.out.println("验证码已保存");
    }
    public void getXsrf()throws IOException{
        Connection con = Jsoup.connect("http://www.zhihu.com");
        con.header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0");
        Connection.Response rs = con.execute();
        Document doc=Jsoup.parse(rs.body());
        _xsrf=doc.select(".view.view-signin [name=\"_xsrf\"]").attr("value");
        System.out.println("已获得xsrf");
    }
    public void getLoginCookies() throws IOException{
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
        Connection.Response rs=con.ignoreContentType(true).method(Connection.Method.POST)
                .data("_xsrf",_xsrf)
                .data("email", userName)
                .data("password", passWord)
                .data("captcha", captcha).cookies(captchaCookies).execute();
        loginCookies.putAll(rs.cookies());
    }
    public void login() throws IOException{
        getLoginCookies();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Connection con = Jsoup.connect("https://www.zhihu.com");//获取连接
        con.header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0");//配置模拟浏览器
        con.cookies(loginCookies);
        Connection.Response rs = con.execute();
        Document doc = Jsoup.parse(rs.body());
        if(checkLogin(doc))
            saveCookies("zhihu_cookies.txt", loginCookies);

    }
    public void saveCookies(String fileName,Map<String,String> cookies) throws IOException{
        FileOutputStream fos = new FileOutputStream(fileName);
        BufferedOutputStream bos
                = new BufferedOutputStream(fos);
        PrintWriter pw = new PrintWriter(bos);
        for (Map.Entry<String, String> entry : cookies.entrySet()) {
            pw.println(entry.getKey() + "=" + entry.getValue().replace("\"",""));
        }
        pw.close();
        System.out.println("cookies已保存");
    }
    public void readCookies(String filename) throws IOException{
        loginCookies.clear();
        FileInputStream fis
                = new FileInputStream(filename);
        InputStreamReader isr
                = new InputStreamReader(fis);
        BufferedReader br
                = new BufferedReader(isr);
        String str = null;
        while((str = br.readLine()) != null){
            int index=str.indexOf("=");
            loginCookies.put(
                    str.substring(0,index),
                    str.substring(index+1,str.length())
            );
        }
        System.out.println(loginCookies);
    }
    public void loginBySavedCookies() throws IOException{
        readCookies("zhihu_cookies.txt");
        Connection con = Jsoup.connect("https://www.zhihu.com");//获取连接
        con.header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0");//配置模拟浏览器
        con.cookies(loginCookies);
        Connection.Response rs = con.execute();
        Document doc = Jsoup.parse(rs.body());
        checkLogin(doc);
    }
    public boolean checkLogin(Document doc) {
        Elements elmts=doc.select(".zu-top-nav-userinfo");
        if(!elmts.isEmpty()){
            System.out.println("登录成功！"+"登录用户为:"+elmts.select(".name").text());
            return true;
        }
        return false;
    }
}
