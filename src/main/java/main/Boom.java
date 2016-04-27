package main;

import dao.ZhihuDao;
import entity.LoginInfo;
import filter.RedisFilter;
import redis.clients.jedis.Jedis;
import thread.ThreadPool;
import thread.UserInfoTask;
import util.EzraPoundUtil;
import util.JedisUtil;

import java.util.List;
import java.util.Scanner;

/**
 * Created by Cabeza on 2016/4/26.
 */
public class Boom {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Jedis jedis = JedisUtil.getJedis();
        ThreadPool threadPool = ThreadPool.getThreadPool(30);
        LoginInfo loginInfo = new LoginInfo();
        loginInfo.login();
        System.out.println("请输入1或2 (1:种子模式  2:继续上次爬取)");
        String opt = sc.nextLine();
        if (opt.equals("1")) {
            jedis.flushAll();
            ZhihuDao.clearDatabases();
            System.out.println("请输入种子用户的短链接");
            String userId = sc.nextLine();
            RedisFilter.put(userId);
            threadPool.execute(new UserInfoTask(loginInfo.getXsrf(), loginInfo.getLoginCookies(), userId, threadPool));
            while (true) {
                try {
                    if (EzraPoundUtil.finishedUserCount.get() > 200000) {
                        threadPool.destroy();
                        System.exit(0);
                    }

                    if (threadPool.getWaitTasknumber() < 50) {
                        String url = jedis.spop("queue");
                        if (!RedisFilter.put(url))
                            threadPool.execute(new UserInfoTask(loginInfo.getXsrf(), loginInfo.getLoginCookies(), url, threadPool));
                    }
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else if (opt.equals("2")) {
            System.out.println("是否重建过滤器表:1是,2否");
            if (sc.nextLine().equals("1")) {
                List<String> list = ZhihuDao.queryUserList();
                jedis.del("filter");
                for (String str : list) {
                    jedis.sadd("filter", str);
                }
            }
            while (true) {
                try {
                    if (threadPool.getWaitTasknumber() < 50) {
                        String url = jedis.spop("queue");
                        if (!RedisFilter.put(url))
                            threadPool.execute(new UserInfoTask(loginInfo.getXsrf(), loginInfo.getLoginCookies(), url, threadPool));
                    }
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
