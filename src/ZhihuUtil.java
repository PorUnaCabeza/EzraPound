import net.sf.json.JSONObject;
import nl.siegmann.epublib.domain.Author;
import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.epub.EpubWriter;
import org.apache.commons.io.FileUtils;
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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Cabeza on 2016/1/13.
 *
 * The apparition of these faces in the crowd;
 * Petals on a wet, black bough.
 *         --------In a Station of the Metro
 */
public class ZhihuUtil {
    public static final String userAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:29.0) Gecko/20100101 Firefox/29.0";
    private static Logger log=Logger.getLogger(ZhihuUtil.class);
    private Date date = new Date();
    public static final String HTML_DIR_ROOT = "C:/book1/";
    public static final String IMG_DIR_ROOT="c:/book1/img/";

    private Map<String,String> captchaCookies =new HashMap<>();
    private Map<String,String> loginCookies =new HashMap<>();
    private Map<String,String> updateNews=new HashMap<>();
    private Map<Integer,String> downloadHtmlFailedMap =new HashMap<>();
    private List<String> downloadImgList=new ArrayList<>();
    private Set<String> downloadImgFailedSet=new HashSet<>();
    private List<String> downloadImgSucceedList=new ArrayList<>();
    private List<String> downloadHtmlSucceedList=new ArrayList<>();
    private String peopleName=" ";

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
    ThreadPoolExecutor threadPool;

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
        threadPool = new ThreadPoolExecutor(20, 40, 3, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(40), new ThreadPoolExecutor.DiscardOldestPolicy());
        initDir();
        String answerUrl="";
        if(orderyByVoteNum){
            answerUrl = url+"?order_by=vote_num&page="+1;
        } else{
            answerUrl = url+"?page="+1;
        }
        login();
        con = Jsoup.connect(answerUrl).timeout(30000);//获取连接
        con.header("User-Agent", userAgent);//配置模拟浏览器
        con.cookies(loginCookies);
        try {
            rs = con.execute();
        } catch (Exception e) {
            log.info("----failed");
            e.printStackTrace();
            return;
        }
        doc=Jsoup.parse(rs.body());
        doc.setBaseUri("https://www.zhihu.com");
        peopleName=doc.select(".title-section .name").text();
        Elements items=doc.select(".zm-item");
        for(int i=0;i<items.size();i++){
            threadPool.execute(new ThreadDownloadHtml(i, items.get(i).select(".question_link").attr("abs:href")));
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        shutdownThreadPool();
        waitThreadPoolEnd(getThreadPool());
        threadPool = new ThreadPoolExecutor(20, 40, 3, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(40), new ThreadPoolExecutor.DiscardOldestPolicy());
        downloadImg();
        shutdownThreadPool();
        waitThreadPoolEnd(getThreadPool());
        pack2Epub();
        System.out.println("over!!!!!");
    }
    public void topicAnswer2Epub(String url){
    }
    public void initDir(){
        File htmlDir=new File(HTML_DIR_ROOT);
        File imgDir=new File(IMG_DIR_ROOT);
        if(!htmlDir.exists()&&!htmlDir.isDirectory()){
            htmlDir.mkdir();
            log.info(HTML_DIR_ROOT+"不存在,现创建..");
        }
        if(!imgDir.exists()&&!imgDir.isDirectory()){
            imgDir.mkdir();
            log.info(IMG_DIR_ROOT+"不存在,现创建..");
        }
    }
    public ThreadPoolExecutor getThreadPool(){
        return threadPool;
    }
    public void shutdownThreadPool(){
        threadPool.shutdown();
    }
    public void downloadImg(){
        for(String url:downloadImgList){
            log.info(url);
            threadPool.execute(new ThreadDownloadImg(url));
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    public void downloadFailedItems(){

    }
    public void waitThreadPoolEnd(ThreadPoolExecutor pool){
        boolean loop = true;
        try {
            do {
                loop = !pool.awaitTermination(2, TimeUnit.SECONDS);  //阻塞，直到线程池里所有任务结束
            } while(loop);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ZhihuUtil zu=new ZhihuUtil();
        zu.peopleAnswer2Epub(true, "https://www.zhihu.com/people/gong-min-98/answers");
    }

    class  ThreadDownloadHtml implements  Runnable{
        private int number;
        private String answerUrl;
        private int downloadCount=0;
        public ThreadDownloadHtml(int number,String answerUrl){
            this.number=number;
            this.answerUrl=answerUrl;
        }
        @Override
        public void run(){
            if(download())
                return;
            while(!download()&&downloadCount<5){
                downloadCount++;
            }
        }
        public boolean download(){
            con = Jsoup.connect(answerUrl).timeout(30000);//获取连接
            con.header("User-Agent", userAgent);//配置模拟浏览器
            con.cookies(loginCookies);
            try {
                log.info("编号"+number+",链接"+answerUrl+"...");
                rs = con.execute();
            } catch (Exception e) {
                downloadHtmlFailedMap.put(number, answerUrl);
                e.printStackTrace();
                return false;
            }
            log.info("准备解析下载编号"+number+"...");
            doc=Jsoup.parse(rs.body());
            return parseAnswer(number,doc);
        }
    }
    class ThreadDownloadImg implements Runnable{
        private String url;
        private String picName;
        private int downloadCount=0;
        private FileOutputStream out=null;
        public ThreadDownloadImg(String url){
            this.url=url;
            this.picName=url.replaceFirst("https://(.*)/", "");
        }
        @Override
        public void run(){
            if(download())
                return;
            while(!download()&&downloadCount<5){
                downloadCount++;
                log.info("*******************************++");
            }
        }
        public boolean download(){
            con = Jsoup.connect(url)
                    .ignoreContentType(true)
                    .timeout(30000);//获取连接
            con.header("User-Agent", userAgent);
            try {
                rs = con.execute();
            } catch (Exception e) {
                log.info("获取图片失败,执行"+downloadCount+"次"+picName);
                downloadImgFailedSet.add(url);
                return false;
            }
            File file = new File(IMG_DIR_ROOT+picName);
            try {
                out= new FileOutputStream(file);
                out.write(rs.bodyAsBytes());
                downloadImgSucceedList.add(picName);
                log.info("下载图片成功"+picName+"执行"+downloadCount+"次");
                return true;
            } catch (IOException e) {
                log.info("---------保存图片失败-----------");
                e.printStackTrace();
                return false;
            }finally {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public boolean parseAnswer(int number,Document doc){
        String fileName=String.format("%03d",number)+doc.select(".zm-item-title.zm-editable-content").text()+".html";
        fileName=fileName.replaceAll("，",",").replaceAll("\\?","").trim();
        Element htmlContent=doc.select(".zm-editable-content.clearfix").first();
        if(htmlContent==null)
            return false;
        htmlContent.select("noscript").remove();
        Elements imgs=htmlContent.select("img");
        for(Element img:imgs){
            img.attr("src",img.attr("data-actualsrc").replaceFirst("https://(.*)/", "img/"));
            img.append("<br>");
            downloadImgList.add(img.attr("data-actualsrc"));
        }
        try {
            FileUtils.writeStringToFile(new File(HTML_DIR_ROOT+fileName)
                    , "<html>\n<head>\n<meta charset=\"utf-8\">\n</head>\n<body>\n"
                    +"<h5>"+doc.select(".zm-item-title.zm-editable-content").text()+"</h5>"
                    +htmlContent.toString()
                    +"\n</body>\n</html>"
                    ,"UTF-8");
            downloadHtmlSucceedList.add(fileName);
            log.info("下载编号"+number+"网页成功");
            return  true;
        } catch (IOException e) {
            e.printStackTrace();
            log.info("------------下载编号"+number+"网页失败--------------");
            return  false;
        }
    }
    public void pack2Epub(){
        try {
            Book book = new Book();
            book.getMetadata().addTitle(peopleName+"的知乎回答");
            book.getMetadata().addAuthor(new Author("", peopleName));
            for(String url:downloadImgSucceedList){
                String imgName=url.replaceFirst("https://(.*)/", "");
                book.getResources().add(new Resource(new FileInputStream(new File(IMG_DIR_ROOT + imgName)), "img/"+imgName));
            }
            downloadHtmlSucceedList.sort(new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    if(o1.compareTo(o2)<0)
                        return -1;
                    return 1;
                }
            });
            for(int i=0;i<downloadHtmlSucceedList.size();i++){
                String url=downloadHtmlSucceedList.get(i);
                book.addSection(url.substring(0,url.length()-4), new Resource(new FileInputStream(
                        new File(HTML_DIR_ROOT + url)), i+".html"));
            }
            EpubWriter epubWriter = new EpubWriter();
            epubWriter.write(book, new FileOutputStream(HTML_DIR_ROOT +peopleName+"的知乎回答.epub"));
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
