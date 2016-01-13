import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
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
            new Captcha().login();

    }
    public void getCaptchaCookies()throws IOException{
        System.out.println(System.currentTimeMillis());
        Connection con = Jsoup.connect("https://www.zhihu.com/captcha.gif?r="+System.currentTimeMillis()).ignoreContentType(true);//获取连接
        con.header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0");
        Connection.Response rs = con.execute();
        FileOutputStream out = (new FileOutputStream(new java.io.File("cabeza.gif")));
        out.write(rs.bodyAsBytes());
        captchaCookies.putAll(rs.cookies());
        System.out.println(captchaCookies);
    }
    public void getXsrf()throws IOException{
        Connection con = Jsoup.connect("http://www.zhihu.com");
        con.header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0");
        Connection.Response rs = con.execute();
        Document doc=Jsoup.parse(rs.body());
        _xsrf=doc.select(".view.view-signin [name=\"_xsrf\"]").attr("value");
        System.out.println(_xsrf);
    }
    public void getLoginCookies() throws IOException{
        Scanner sc=new Scanner(System.in);
        getXsrf();
        getCaptchaCookies();
        String captcha=sc.nextLine();
        Connection con = Jsoup.connect("https://www.zhihu.com/login/email");
        con.header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0");
        Connection.Response rs=con.ignoreContentType(true).method(Connection.Method.POST)
                .data("_xsrf",_xsrf)
                .data("email", "username")
                .data("password","password")
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
        System.out.println(doc);
        FileOutputStream fos = new FileOutputStream("zhihu_cookie.txt");
        BufferedOutputStream bos
                = new BufferedOutputStream(fos);
        PrintWriter pw = new PrintWriter(bos);
        pw.print(loginCookies);
        pw.close();
    }
}
