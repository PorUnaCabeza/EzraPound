import org.junit.Test;
import redis.clients.jedis.Jedis;
import util.JedisUtil;

/**
 * Created by Cabeza on 2016/4/25.
 */
public class RedisTest {
    @Test
    public void set(){
        Jedis jedis= JedisUtil.getJedis();
        jedis.set("country", "China");
        JedisUtil.returnResource(jedis);
    }
    @Test
    public void get(){
        Jedis jedis= JedisUtil.getJedis();
        System.out.println(jedis.get("country"));
        JedisUtil.returnResource(jedis);
    }
}
