package app.config;

import app.cache.LruCache;
import app.models.Order;
import app.models.Smartphone;
import app.models.User;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig {

    @Bean
    public LruCache<Long, Smartphone> smartphoneCache() {
        return new LruCache<>(5);
    }

    @Bean
    public LruCache<Long, Order> orderCache() {
        return new LruCache<>(5);
    }

    @Bean
    public LruCache<Long, User> userCache() {
        return new LruCache<>(5);
    }
}
