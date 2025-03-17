package app.cache;

import app.models.Smartphone;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Component;


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
//ограничить, шо если заполнен, удалить вместе delete put
//фул collection и collections, отличие linkedList arraylist
//как раюотает мапа и как в ней putЧ
//log файл - брать только определенные(сегодня мб)
//swagger, аоп
//логирование