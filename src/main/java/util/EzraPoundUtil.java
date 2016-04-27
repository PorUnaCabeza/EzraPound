package util;

import dao.ZhihuDao;
import org.n3r.eql.EqlPage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Cabeza on 2016/4/25.
 */
public class EzraPoundUtil {
    private static Logger log= LoggerFactory.getLogger(EzraPoundUtil.class);
    public static final String CAPTCHA_DIR="cabeza.gif";
    public static final String LOGIN_COOKIES_DIR="zhihu_cookies.txt";
    public static AtomicInteger finishedUserCount=new AtomicInteger(0);


    public static void rebuildFilter(){
        Jedis jedis=JedisUtil.getJedis();
        jedis.del("filter");

        int userCount=ZhihuDao.queryUserCount();
        int rebuildCount=0;

        log.info("总数:" + userCount);
        int pageNum=5000;
        EqlPage eqlPage=new EqlPage(0,pageNum);
        List<String> list;
        for(int i=0;i<userCount;i+=pageNum){
            eqlPage.setStartIndex(i);
            list= ZhihuDao.queryUserList(eqlPage);
            for (String str : list) {
                jedis.sadd("filter", str);
            }
            rebuildCount+=list.size();
            log.info("已添加"+rebuildCount+"条");
        }
        log.info("重建完毕!");
        JedisUtil.returnResource(jedis);
    }

    public static String unicode2Character(String str)
    {
        str = (str == null ? "" : str);
        if (str.indexOf("\\u") == -1)//如果不是unicode码则原样返回
            return str;

        StringBuffer sb = new StringBuffer(1000);

        for (int i = 0; i < str.length() - 6;)
        {
            String strTemp = str.substring(i, i + 6);
            String value = strTemp.substring(2);
            int c = 0;
            for (int j = 0; j < value.length(); j++)
            {
                char tempChar = value.charAt(j);
                int t = 0;
                switch (tempChar)
                {
                    case 'a':
                        t = 10;
                        break;
                    case 'b':
                        t = 11;
                        break;
                    case 'c':
                        t = 12;
                        break;
                    case 'd':
                        t = 13;
                        break;
                    case 'e':
                        t = 14;
                        break;
                    case 'f':
                        t = 15;
                        break;
                    default:
                        t = tempChar - 48;
                        break;
                }

                c += t * ((int) Math.pow(16, (value.length() - j - 1)));
            }
            sb.append((char) c);
            i = i + 6;
        }
        return sb.toString();
    }
}
