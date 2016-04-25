import org.json.JSONObject;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import util.epub.domain.Author;
import util.epub.domain.Book;
import util.epub.domain.Resource;
import util.epub.epub.EpubWriter;
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
    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/49.0.2623.87 Safari/537.36";
    private static Logger log=Logger.getLogger(ZhihuUtil.class);
    private Date date = new Date();
    public static final String HTML_DIR_ROOT = "C:/book1/";
    public static final String IMG_DIR_ROOT="c:/book1/img/";

    //存储获得的cookie
    private Map<String,String> captchaCookies =new HashMap<>();
    private Map<String,String> loginCookies =new HashMap<>();
    //存储用户产生的新动态
    private Map<String,String> updateNews=new HashMap<>();

    //下载html文件成功与失败列表
    private Map<Integer,String> downloadHtmlFailedMap =new HashMap<>();
    private List<String> downloadHtmlSucceedList=new ArrayList<>();
    //图片 预下载、下载成功、下载失败
    private List<String> downloadImgList=new ArrayList<>();
    private List<String> downloadImgSucceedList=new ArrayList<>();
    private Set<String> downloadImgFailedSet=new HashSet<>();
    //用户名
    private String peopleName=" ";
    //准备下载的回答的编号,自增
    private int downloadAnswerNum =0;

    //是否获得当前最新动态的时间
    private boolean recentNewsTimeInit=false;
    //当前最新动态的unix时间戳
    private String recentNewsTime="0";

    //是否为登录状态
    private boolean isLogin = false;
    //_xsrf的值
    private String _xsrf;
    //是否获得_xsrf值
    private boolean getXsrf=false;

    private Scanner sc=new Scanner(System.in);
    //邮件内容
    private StringBuffer emailContent=new StringBuffer();
    private Connection con;
    private Connection.Response rs;
    private Document doc;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    //爬取动态时，为防止网络中断，记录当前爬取到的动态标记
    private String traverseStartSign = "";

    //线程池
    ThreadPoolExecutor threadPool;

    public boolean getXsrf(int recursiveTimes) {
        if(recursiveTimes>5){
            return false;
        }
        con = Jsoup.connect("http://www.zhihu.com").timeout(30000);
        con.header("User-Agent", USER_AGENT);
        try {
            rs = con.execute();
        } catch (Exception e) {
            log.info("获得Xsrf第"+recursiveTimes+"次失败");
            return getXsrf(recursiveTimes+1);
        }
        Document doc=Jsoup.parse(rs.body());
        _xsrf=doc.select(".view.view-signin [name=\"_xsrf\"]").attr("value");
        log.info("已获得xsrf");
        getXsrf=true;
        return true;
    }

    public void getCaptchaCookies() {
        captchaCookies.clear();
        con = Jsoup.connect("https://www.zhihu.com/captcha.gif?r=" + System.currentTimeMillis()+"&type=login")
                .ignoreContentType(true)
                .timeout(30000);//获取连接
        con.header("User-Agent", USER_AGENT);
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
        getCaptchaCookies();
        log.info("请输入帐号");
        String userName=sc.nextLine();
        log.info("请输入密码");
        String passWord=sc.nextLine();
        log.info("请打开工程路径查看验证码并输入");
        String captcha=sc.nextLine();
        con = Jsoup.connect("https://www.zhihu.com/login/email").timeout(30000);
        con.header("user-agent", USER_AGENT);
        try {
            rs = con.ignoreContentType(true).method(Connection.Method.POST)
                    .data("_xsrf", _xsrf)
                    .data("email", userName)
                    .data("password", passWord)
                    .data("remember_me","true")
                    .data("captcha", captcha)
                    .cookies(captchaCookies).execute();
        } catch (Exception e) {
            log.info("获得loginCookies失败");
            return getLoginCookies();
        }
        log.info("登录返回:"+rs.body());
        loginCookies.putAll(rs.cookies());
        return true;
    }

    /**
     * 登陆成功后将当前cookie保存到文件，以便下次免登录
     * @param fileName 文件名
     * @param cookies cookies
     */
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

    /**
     * 从文件读取cookie
     * @param filename 文件名
     */
    public void readCookies(String filename) {
        loginCookies.clear();
        if(!new File(filename).exists()){
            log.info(filename+"不存在");
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
                loginCookies.put(
                        str.substring(0, index),
                        str.substring(index + 1, str.length())
                );
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //见方法名
    public boolean loginBySavedCookies() {
        if(!getXsrf)
            getXsrf(0);
        readCookies("zhihu_cookies.txt");
        con = Jsoup.connect("https://www.zhihu.com").timeout(30000);//获取连接
        con.header("User-Agent", USER_AGENT);//配置模拟浏览器
        con.cookies(loginCookies);
        try {
            rs = con.execute();
        } catch (Exception e) {
            log.info("读取cookie登录失败");
            return false;
        }
        return checkLogin(Jsoup.parse(rs.body()));
    }

    /**
     * 从文件读取cookie登录失败时，通过帐号密码登录喽
     * @return
     */
    public boolean loginByEmailAndPwd() {
        getLoginCookies();
        con = Jsoup.connect("https://www.zhihu.com");//获取连接
        con.header("User-Agent", USER_AGENT);//配置模拟浏览器
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

    //登录逻辑
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

    //检查登录状态
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

    /**
     * 监控某用户知乎动态，若出现新的动态，将会向指定邮箱发送邮件
     * @param url 用户知乎首页地址
     */
    public void watchNews(String url) {
        login();
        con = Jsoup.connect(url).timeout(30000);//获取连接
        con.header("User-Agent", USER_AGENT);//配置模拟浏览器
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

    /**
     * 按时间爬取用户所有知乎动态
     * @param url 用户知乎首页地址
     */
    public void traverseNews(String url){
        Elements elmts;
        login();
        if (traverseStartSign.isEmpty()) {
            con = Jsoup.connect(url).timeout(30000);//获取连接
            con.header("User-Agent", USER_AGENT);//配置模拟浏览器
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
            con.header("User-Agent", USER_AGENT);
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
            JSONObject jsonObj = new JSONObject(rs.body());
            doc = Jsoup.parse(jsonObj.getJSONArray("msg").get(1).toString());
        }
        elmts = doc.select(".zm-profile-section-item.zm-item.clearfix");
        while(!elmts.isEmpty()){
            for (Element elmt : elmts) {
                //此处通过正则表达式配置关键词过滤
                if (elmt.select(".zm-profile-activity-page-item-main").text().matches(".*(keyword1|.*).*")) {
                    date.setTime(Long.parseLong(elmt.attr("data-time")) * 1000);
                    log.info(sdf.format(date));
                    log.info(elmt.select(".zm-profile-activity-page-item-main").text() + "www.zhihu.com" + elmt.select(".question_link").attr("href"));
                }
                traverseStartSign = elmt.attr("data-time");
            }
            con = Jsoup.connect(url + "/activities").method(Connection.Method.POST).timeout(3000).ignoreContentType(true);//获取连接
            con.header("User-Agent", USER_AGENT);//配置模拟浏览器
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
            JSONObject jsonObj=new JSONObject(rs.body());
            doc=Jsoup.parse(jsonObj.getJSONArray("msg").get(1).toString());
            elmts = doc.select(".zm-profile-section-item.zm-item.clearfix");
        }
        log.info("拉取完毕");
        System.exit(0);
    }

    //监控动态入口
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
    //爬取动态入口
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
    //发送邮件
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
    public synchronized int addDownloadAnswerNum(){
        return downloadAnswerNum++;
    }

    /**
     * 获取用户回答页的document
     *
     * @param orderyByVoteNum 按赞同数排序or时间排序
     * @param url 用户回答页url，如 https://www.zhihu.com/people/excited-vczh/answers
     * @param pageNum 页数
     * @param recursiveTimes 递归次数
     * @return
     */
    public Document getAnswerPageDoc(boolean orderyByVoteNum,String url,int pageNum,int recursiveTimes){
        if(recursiveTimes>5)
            return null;
        String answerUrl="";
        if(orderyByVoteNum){
            answerUrl = url+"?order_by=vote_num&page="+pageNum;
        } else{
            answerUrl = url+"?page="+pageNum;
        }
        con = Jsoup.connect(answerUrl).timeout(30000);//获取连接
        con.header("User-Agent", USER_AGENT);//配置模拟浏览器
        con.cookies(loginCookies);
        try {
            rs = con.execute();
        } catch (Exception e) {
            log.info("----failed");
            e.printStackTrace();
            return getAnswerPageDoc(orderyByVoteNum,url,pageNum,recursiveTimes+1);
        }
        doc=Jsoup.parse(rs.body());
        doc.setBaseUri("https://www.zhihu.com");
        return doc;
    }

    /**
     * 将用户回答下载打包为epub格式电子书,入口
     * @param orderyByVoteNum
     * @param url
     * @param limitCount
     */
    public void peopleAnswer2Epub(boolean orderyByVoteNum,String url,int limitCount){
        login();
        int pageCount;
        Document doc;
        threadPool = new ThreadPoolExecutor(10, 20, 3, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(1000), new ThreadPoolExecutor.DiscardOldestPolicy());
        initDir();
        doc=getAnswerPageDoc(orderyByVoteNum,url,1,0);
        if(doc==null)
            return;
        peopleName=doc.select(".title-section .name").text();
        Elements items;
        int maxPageNum=Integer.parseInt(doc.select(".zm-invite-pager span").last().previousElementSibling().text());
        if(limitCount==0||limitCount>maxPageNum)
            pageCount=maxPageNum;
        else
            pageCount=limitCount;
        for(int i=0;i<pageCount;i++){
            doc=getAnswerPageDoc(orderyByVoteNum,url,i+1,0);
            items=doc.select(".zm-item");
            for(int j=0;j<items.size();j++){
                threadPool.execute(new ThreadDownloadHtml(addDownloadAnswerNum(), items.get(j).select(".question_link").attr("abs:href")));
            }
        }
        shutdownThreadPool();
        waitThreadPoolEnd(getThreadPool());
        threadPool = new ThreadPoolExecutor(20, 40, 3, TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(40), new ThreadPoolExecutor.DiscardOldestPolicy());
        downloadImg();
        shutdownThreadPool();
        waitThreadPoolEnd(getThreadPool());
        downloadFailedItems();
        pack2Epub();
        System.out.println("over!!!!!");
    }
    public void topicAnswer2Epub(String url){

    }

    //初始化路径
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

    /**
     * 读取下载失败列表，并重试下载
     */
    public void downloadFailedItems(){
        log.info("读取下载失败列表...");
        if(downloadHtmlFailedMap.size()>0){
            threadPool = new ThreadPoolExecutor(10, 20, 3, TimeUnit.SECONDS,
                    new ArrayBlockingQueue<Runnable>(1000), new ThreadPoolExecutor.DiscardOldestPolicy());
            log.info("有网页下载失败,如下:");
            for (Map.Entry<Integer, String> entry : downloadHtmlFailedMap.entrySet()) {
                log.info("编号"+entry.getKey()+"下载失败,下面重试下载");
                threadPool.execute(new ThreadDownloadHtml(entry.getKey(),entry.getValue()));
            }
            shutdownThreadPool();
            waitThreadPoolEnd(getThreadPool());
        }
        if(downloadImgFailedSet.size()>0){
            threadPool = new ThreadPoolExecutor(10, 20, 3, TimeUnit.SECONDS,
                    new ArrayBlockingQueue<Runnable>(1000), new ThreadPoolExecutor.DiscardOldestPolicy());
            log.info("有图片下载失败,如下:");
            Iterator i = downloadImgFailedSet.iterator();
            while(i.hasNext()){
                String url=(String)i.next();
                log.info("图片"+url+"下载失败,下面重试下载");
                threadPool.execute(new ThreadDownloadImg(url));
            }
            shutdownThreadPool();
            waitThreadPoolEnd(getThreadPool());
        }

    }

    /**
     * 阻塞，直到线程池里所有任务结束
     * @param pool
     */
    public void waitThreadPoolEnd(ThreadPoolExecutor pool){
        boolean loop = true;
        try {
            do {
                loop = !pool.awaitTermination(2, TimeUnit.SECONDS);
            } while(loop);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        ZhihuUtil zu=new ZhihuUtil();
        zu.startWatch("https://www.zhihu.com/people/lin-shen-shi-jian-lu",3000);
    }
    //下载html的线程
    class  ThreadDownloadHtml implements  Runnable{
        private int number;
        private String answerUrl;
        public ThreadDownloadHtml(int number,String answerUrl){
            this.number=number;
            this.answerUrl=answerUrl;
        }
        @Override
        public void run(){
            if(download(0))
                return;
            downloadHtmlFailedMap.put(number, answerUrl);
        }
        public boolean download(int recursiveTimes){
            if(recursiveTimes>5){
                return false;
            }
            con = Jsoup.connect(answerUrl).timeout(30000);//获取连接
            con.header("User-Agent", USER_AGENT);//配置模拟浏览器
            con.cookies(loginCookies);
            try {
                log.info("编号"+number+",链接"+answerUrl+"...");
                rs = con.execute();
            } catch (Exception e) {
                log.info("编号"+number+"第"+recursiveTimes+"次下载失败");
                return download(recursiveTimes+1);
            }
            log.info("准备解析下载编号"+number+"...");
            doc=Jsoup.parse(rs.body());
            return parseAnswer(number,doc);
        }
    }
    //下载图片的线程
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
           download(0);
        }
        public boolean download(int recursiveTimes){
            if(recursiveTimes>5)
                return false;
            con = Jsoup.connect(url)
                    .ignoreContentType(true)
                    .timeout(30000);//获取连接
            con.header("User-Agent", USER_AGENT);
            try {
                rs = con.execute();
            } catch (Exception e) {
                log.info("图片"+picName+"第"+recursiveTimes+"次获取失败");
                return download(recursiveTimes+1);
            }
            File file = new File(IMG_DIR_ROOT+picName);
            try {
                out= new FileOutputStream(file);
                out.write(rs.bodyAsBytes());
                downloadImgSucceedList.add(picName);
                log.info("下载图片成功"+picName+"执行"+recursiveTimes+"次");
                return true;
            } catch (IOException e) {
                log.info("保存图片失败---"+picName);
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

    /**
     * 解析html文档。并保存到本地
     * @param number
     * @param doc
     * @return
     */
    public boolean parseAnswer(int number,Document doc){
        String fileName=String.format("%04d",number)+"."+doc.select(".zm-item-title.zm-editable-content").text()+".html";
        fileName=fileName
                .replaceAll("，",",")
                .replaceAll("\\?","")
                .replaceAll("\"","")
                .replaceAll("/","")
                .replaceAll("\\\\","")
                .trim();
        Element htmlContent=doc.select(".zm-editable-content.clearfix").first();
        if(htmlContent==null){
            log.info("编号"+number+"网页读取到内容为空，可能为被删除的内容");
            return false;
        }
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
                    +"<h5>"+doc.select(".zm-item-title.zm-editable-content").text()+"</h5>\n"
                    +htmlContent.toString()
                    +"\n</body>\n</html>"
                    ,"UTF-8");
            downloadHtmlSucceedList.add(fileName);
            log.info("下载编号"+number+"网页成功");
            return  true;
        } catch (IOException e) {
            log.info("------------下载编号"+number+"网页失败--------------");
            return  false;
        }
    }

    //打包为epub电子书
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
