package brandwatch.assessment.store.service;

import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.XAddParams;

import java.util.HashMap;
import java.util.Map;

@Service
public class RedisService {

    private final JedisPool jedisPool;

    public RedisService(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    public void sendInStockMessage(String streamKey, Map<String, Integer> items) {
        try(Jedis jedis = jedisPool.getResource()) {
            Map<String, String> message = new HashMap<>();
            items.forEach((key, value) -> {
                message.put(key, String.valueOf(value));
            });
            jedis.xadd(streamKey, new XAddParams(), message);
        }
    }
}
