package app.config;

import app.cache.InMemoryCache;
import app.models.Order;
import app.models.Smartphone;
import app.models.User;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig {

    @Bean
    public InMemoryCache<Long, Smartphone> smartphoneCache() {
        return new InMemoryCache<>();
    }

    @Bean
    public InMemoryCache<Long, Order> orderCache() {
        return new InMemoryCache<>();
    }

    @Bean
    public InMemoryCache<Long, User> userCache() {
        return new InMemoryCache<>();
    }
}
