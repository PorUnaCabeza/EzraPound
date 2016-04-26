package main;

import dao.ZhihuDao;
import entity.LoginInfo;
import filter.RedisFilter;
import redis.clients.jedis.Jedis;
import thread.ThreadPool;
import thread.UserInfoTask;
import util.JedisUtil;

import java.util.Scanner;

/**
 * Created by Cabeza on 2016/4/26.
 */
public class Boom {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Jedis jedis = JedisUtil.getJedis();
        ThreadPool threadPool = ThreadPool.getThreadPool(30);
        LoginInfo loginInfo1 = new LoginInfo();
        loginInfo1.login();
        System.out.println("请输入1或2 (1:种子模式  2:继续上次爬取)");
        String opt = sc.nextLine();
        if (opt.equals("1")) {
            jedis.flushAll();
            ZhihuDao.clearDatabases();
            System.out.println("请输入种子用户的短链接");
            String userId = sc.nextLine();
            RedisFilter.put(userId);
            threadPool.execute(new UserInfoTask(loginInfo1.getXsrf(), loginInfo1.getLoginCookies(), userId, threadPool));
            while (true) {
                try {
                    if (threadPool.getWaitTasknumber() < 50) {
                        String url = jedis.spop("queue");
                        if (!RedisFilter.put(url))
                            threadPool.execute(new UserInfoTask(loginInfo1.getXsrf(), loginInfo1.getLoginCookies(), url, threadPool));
                    }
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }else if(opt.equals("2")){
            while (true) {
                try {
                    if (threadPool.getWaitTasknumber() < 50) {
                        String url = jedis.spop("queue");
                        if (!RedisFilter.put(url))
                            threadPool.execute(new UserInfoTask(loginInfo1.getXsrf(), loginInfo1.getLoginCookies(), url, threadPool));
                    }
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
