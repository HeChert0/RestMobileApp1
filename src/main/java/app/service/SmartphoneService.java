package app.service;

import app.cache.InMemoryCache;
import app.dao.SmartphoneRepository;
import app.models.Order;
import app.models.Smartphone;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class SmartphoneService {

    private final SmartphoneRepository smartphoneRepository;
    private final InMemoryCache<Long, Smartphone> smartphoneCache;

    @Autowired
    public SmartphoneService(SmartphoneRepository smartphoneRepository,
                             InMemoryCache<Long, Smartphone> smartphoneCache) {
        this.smartphoneRepository = smartphoneRepository;
        this.smartphoneCache = smartphoneCache;
    }

    public List<Smartphone> getAllSmartphones() {
        long dbCount = smartphoneRepository.count();
        int cacheSize = smartphoneCache.size();

        if (cacheSize == dbCount && cacheSize > 0) {
            return smartphoneCache.getAllValues();
        } else {
            List<Smartphone> smartphones = smartphoneRepository.findAll();
            smartphones.forEach(s -> smartphoneCache.put(s.getId(), s));
            return smartphones;
        }
    }

    public Optional<Smartphone> getSmartphoneById(Long id) {
        Smartphone cachedSmartphone = smartphoneCache.get(id);
        if (cachedSmartphone != null) {
            return Optional.of(cachedSmartphone);
        }
        Optional<Smartphone> smartphone = smartphoneRepository.findById(id);
        smartphone.ifPresent(s -> smartphoneCache.put(s.getId(), s));
        return smartphone;
    }

    @Transactional
    public Smartphone saveSmartphone(Smartphone smartphone) {
        Smartphone savedSmartphone = smartphoneRepository.save(smartphone);
        smartphoneCache.put(savedSmartphone.getId(), savedSmartphone);
        return savedSmartphone;
    }

    @Transactional
    public Smartphone updateSmartphone(Long id, Smartphone updatedSmartphone) {
        return smartphoneRepository.findById(id).map(existingSmartphone -> {
            existingSmartphone.setBrand(updatedSmartphone.getBrand());
            existingSmartphone.setModel(updatedSmartphone.getModel());
            existingSmartphone.setPrice(updatedSmartphone.getPrice());

            Smartphone savedSmartphone = smartphoneRepository.save(existingSmartphone);
            smartphoneCache.put(savedSmartphone.getId(), savedSmartphone);
            return savedSmartphone;
        }).orElse(null);
    }

    @Transactional
    public void deleteSmartphone(Long id) {
        smartphoneRepository.deleteById(id);
        smartphoneCache.remove(id);
    }

    public List<Smartphone> filterSmartphones(String brand, String model, Double price) {
        long dbCount = smartphoneRepository.count();
        int cacheSize = smartphoneCache.size();

        List<Smartphone> smartphones;
        if (cacheSize == dbCount && cacheSize > 0) {
            smartphones =  smartphoneCache.getAllValues();
        } else {
            smartphones = smartphoneRepository.findAll();
            smartphones.forEach(s -> smartphoneCache.put(s.getId(), s));
        }

        return smartphones.stream()
                .filter(s -> brand == null || s.getBrand().equalsIgnoreCase(brand))
                .filter(s -> model == null || s.getModel().equalsIgnoreCase(model))
                .filter(s -> price == null || s.getPrice().equals(price))
                .collect(Collectors.toList());
    }
}
