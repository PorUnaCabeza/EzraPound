package filter;

import redis.clients.jedis.Jedis;
import util.JedisUtil;

/**
 * Created by Cabeza on 2016/4/25.
 */
public class RedisFilter {
    public static final String FILTER_NAME="filter";
    public static boolean put(String value){
        Jedis jedis=JedisUtil.getJedis();
        if(value==null||jedis.sismember(FILTER_NAME,value)){
            JedisUtil.returnResource(jedis);
            return true;//存在
        }else{
            jedis.sadd(FILTER_NAME,value);
            JedisUtil.returnResource(jedis);
            return false;//不存在并添加
        }
    }
    public static void addQueue(String value){
        Jedis jedis=JedisUtil.getJedis();
        jedis.sadd("queue",value);
        JedisUtil.returnResource(jedis);
    }
}
