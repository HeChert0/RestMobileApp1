package app.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Scope("prototype")
public class InMemoryCache<K, V> {

    private static final Logger logger = LoggerFactory.getLogger(InMemoryCache.class);

    private final int maxSize = 100;

    private final Map<K, V> cache = new HashMap<>();

    private final Map<K, Integer> usageCount = new HashMap<>();

    // Мьютекс для синхронизации доступа
    private final Object lock = new Object();

    public V get(K key) {
        synchronized (lock) {
            if (cache.containsKey(key)) {
                usageCount.put(key, usageCount.get(key) + 1);
                logger.info("Cache hit for key: {}", key);
                return cache.get(key);
            }
            logger.info("Cache miss for key: {}", key);
            return null;
        }
    }

    /**
     * Добавить элемент в кэш.
     * Если кэш переполнен, удаляем элемент с минимальной частотой использования.
     */
    public void put(K key, V value) {
        synchronized (lock) {

            if (cache.containsKey(key)) {
                cache.put(key, value);
                usageCount.put(key, usageCount.get(key) + 1);
                logger.info("Cache updated for key: {}", key);
            } else {

                if (cache.size() >= maxSize) {
                    evictLeastFrequentlyUsed();
                }
                cache.put(key, value);
                usageCount.put(key, 1);
                logger.info("Cache added for key: {}", key);
            }
        }
    }

    /**
     * Удаляет элемент по ключу.
     */
    public void remove(K key) {
        synchronized (lock) {
            cache.remove(key);
            usageCount.remove(key);
            logger.info("Cache removed for key: {}", key);
        }
    }

    /**
     * Очищает кэш полностью.
     */
    public void clear() {
        synchronized (lock) {
            cache.clear();
            usageCount.clear();
            logger.info("Cache cleared");
        }
    }

    /**
     * Вспомогательный метод для удаления элемента с минимальной частотой использования.
     */
    private void evictLeastFrequentlyUsed() {
        K lfuKey = Collections.min(usageCount.entrySet(), Map.Entry.comparingByValue()).getKey();
        cache.remove(lfuKey);
        usageCount.remove(lfuKey);
        logger.info("Evicted least frequently used key: {}", lfuKey);
    }

    /**
     * Обновить кэш для конкретного ключа.
     * Используется, если элемент изменился (например, в результате PUT).
     */
    public void update(K key, V newValue) {
        synchronized (lock) {
            if (cache.containsKey(key)) {
                cache.put(key, newValue);
                // Можно сбросить или увеличить счетчик использования при обновлении
                usageCount.put(key, usageCount.get(key) + 1);
                logger.info("Cache updated for key: {}", key);
            }
        }
    }

    public int size() {
        synchronized (lock) {
            return cache.size();
        }
    }

    public List<V> getAllValues() {
        synchronized (lock) {
            return new ArrayList<>(cache.values());
        }
    }

}

//ограничить, шо если заполнен, удалить вместе delete put
//фул collection и collections, отличие linkedList arraylist
//как раюотает мапа и как в ней putЧ
//log файл - брать только определенные(сегодня мб)
//swagger, аоп
//логирование