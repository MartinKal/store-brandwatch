package brandwatch.assessment.store.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class RedisService {
    private final RedisTemplate<String, String> redisTemplate;

    public RedisService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void sendInStockMessage(String streamKey, Map<String, Integer> items) {
        StreamOperations<String, String, Object> streamOperations = redisTemplate.opsForStream();
        Map<String, Object> message = new HashMap<>();
        items.forEach((key, value) -> {
            message.put(key, String.valueOf(value));
        });
        streamOperations.add(streamKey, message);
    }
}
