package app.cache;

import app.models.Smartphone;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class SmartphoneCache {

    private final ConcurrentMap<String, List<Smartphone>> cache = new ConcurrentHashMap<>();

    public List<Smartphone> getFromCache(String customerName) {
        return cache.get(customerName);
    }

    public void putToCache(String customerName, List<Smartphone> smartphones) {
        cache.put(customerName, smartphones);
    }
}
