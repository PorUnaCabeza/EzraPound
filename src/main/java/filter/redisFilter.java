package filter;

import redis.clients.jedis.Jedis;
import util.JedisUtil;

/**
 * Created by Cabeza on 2016/4/25.
 */
public class RedisFilter {
    public static final String FILTER_NAME="filter";
    private static Jedis jedis=null;
    static {
        jedis= JedisUtil.getJedis();
    }
    public static boolean put(String value){
        if(value==null||jedis.sismember(FILTER_NAME,value)){
            return true;//存在
        }else{
            jedis.sadd(FILTER_NAME,value);
            return false;//不存在并添加
        }
    }
}
