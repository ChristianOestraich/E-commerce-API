package project.ecommerce.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory factory) {

        // Configuracao padrao — 10 minutos
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration
                .defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();

        // TTL customizado por cache
        Map<String, RedisCacheConfiguration> configs = new HashMap<>();
        configs.put("products", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        configs.put("product", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        configs.put("categories", defaultConfig.entryTtl(Duration.ofMinutes(15)));
        configs.put("category", defaultConfig.entryTtl(Duration.ofMinutes(15)));
        configs.put("coupons", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        configs.put("dashboard", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        configs.put("topProducts", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        configs.put("lowStock", defaultConfig.entryTtl(Duration.ofMinutes(3)));

        return RedisCacheManager.builder(factory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(configs)
                .build();
    }
}