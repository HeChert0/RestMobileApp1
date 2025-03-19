package app.cache;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

@Component
public class InMemoryCache<K, V> {

    private final int maxSize = 100;

    private final Map<K, V> cache = new HashMap<>();

    private final Map<K, Integer> usageCount = new HashMap<>();

    // Мьютекс для синхронизации доступа
    private final Object lock = new Object();

    /**
     * Получить значение по ключу.
     * Если элемент найден, увеличиваем его счетчик использования.
     */
    public V get(K key) {
        synchronized (lock) {
            if (cache.containsKey(key)) {
                usageCount.put(key, usageCount.get(key) + 1);
                return cache.get(key);
            }
            return null;
        }
    }

    /**
     * Добавить элемент в кэш.
     * Если кэш переполнен, удаляем элемент с минимальной частотой использования.
     */
    public void put(K key, V value) {
        synchronized (lock) {
            // Если элемент уже есть – обновляем значение и счетчик
            if (cache.containsKey(key)) {
                cache.put(key, value);
                usageCount.put(key, usageCount.get(key) + 1);
            } else {
                // Если кэш заполнен, удаляем элемент с минимальной частотой
                if (cache.size() >= maxSize) {
                    evictLeastFrequentlyUsed();
                }
                cache.put(key, value);
                usageCount.put(key, 1);
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
        }
    }

    /**
     * Очищает кэш полностью.
     */
    public void clear() {
        synchronized (lock) {
            cache.clear();
            usageCount.clear();
        }
    }

    /**
     * Вспомогательный метод для удаления элемента с минимальной частотой использования.
     */
    private void evictLeastFrequentlyUsed() {
        // Находим ключ с минимальной частотой использования
        K lfuKey = Collections.min(usageCount.entrySet(), Map.Entry.comparingByValue()).getKey();
        cache.remove(lfuKey);
        usageCount.remove(lfuKey);
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
            }
        }
    }
}

//ограничить, шо если заполнен, удалить вместе delete put
//фул collection и collections, отличие linkedList arraylist
//как раюотает мапа и как в ней putЧ
//log файл - брать только определенные(сегодня мб)
//swagger, аоп
//логирование