package brandwatch.assessment.store.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.net.URI;

@Configuration
public class JedisConfig {
    @Value("${spring.redis.url}")
    private String redisUrl;

    @Bean
    public JedisConnectionFactory jedisConnectionFactory() {
        URI redisUri = URI.create(redisUrl);
        RedisStandaloneConfiguration config =
                new RedisStandaloneConfiguration(redisUri.getHost(), redisUri.getPort());
        return new JedisConnectionFactory(config);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(jedisConnectionFactory());
        return template;
    }
}
