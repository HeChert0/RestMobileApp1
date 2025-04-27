package app.service;

import app.dao.OrderRepository;
import app.dao.SmartphoneRepository;
import app.models.Order;
import app.models.Smartphone;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "smartphones")
public class SmartphoneService {

    private final SmartphoneRepository smartphoneRepository;
    private final OrderRepository orderRepository;

    @Cacheable(value = "smartphones", key = "#id")
    public Optional<Smartphone> getSmartphoneById(Long id) {
        return smartphoneRepository.findById(id);
    }

    @Cacheable(value = "smartphones")
    @Transactional(readOnly = true)
    public List<Smartphone> getAllSmartphones() {
        return smartphoneRepository.findAll();
    }

    @Caching(
            put = @CachePut(value = "smartphones", key = "#result.id"),
            evict = @CacheEvict(value = "smartphones", allEntries = true)
    )
    @Transactional
    public Smartphone saveSmartphone(Smartphone smartphone) {
        return smartphoneRepository.save(smartphone);
    }

    @SuppressWarnings("checkstyle:VariableDeclarationUsageDistance")
    @Caching(
            put = @CachePut(key = "#id"),
            evict = {
                    @CacheEvict(allEntries = true),                     // чистим кэш смартфонов
                    @CacheEvict(cacheNames = "orders", allEntries = true),  // опционально: сбросить кэш заказов
                    @CacheEvict(cacheNames = "users",  allEntries = true)   // сбросить кэш пользователей
            }
    )
    @Transactional
    public Smartphone updateSmartphone(Long id, Smartphone updated) {
        return smartphoneRepository.findById(id)
            .map(existing -> {
                double oldPrice = existing.getPrice();
                existing.setBrand(updated.getBrand());
                existing.setModel(updated.getModel());
                existing.setPrice(updated.getPrice());
                Smartphone saved = smartphoneRepository.save(existing);

                if (oldPrice != saved.getPrice()) {
                    orderRepository.findAll().stream()
                            .filter(o -> o.getSmartphones().stream()
                                    .anyMatch(p -> p.getId().equals(saved.getId())))
                            .forEach(o -> {
                                o.setTotalAmount(
                                        o.getSmartphones().stream()
                                                .mapToDouble(p -> p.getId().equals(saved.getId())
                                                        ? saved.getPrice()
                                                        : p.getPrice())
                                                .sum()
                                );
                                orderRepository.save(o);
                            });
                }
                return saved;
            })
                .orElseThrow(() -> new IllegalArgumentException("Smartphone not found: " + id));
    }

    @Caching(
            evict = {
                    @CacheEvict(key = "#id"),                               // удалить его из кэша смартфонов
                    @CacheEvict(allEntries = true),                         // очистить кэш смартфонов целиком
                    @CacheEvict(cacheNames = "orders", allEntries = true),  // опционально: сбросить кэш заказов
                    @CacheEvict(cacheNames = "users",  allEntries = true)   // сбросить кэш пользователей
            }
    )
    @Transactional
    public void deleteSmartphone(Long id) {
        Smartphone phone = smartphoneRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Smartphone not found: " + id));
        orderRepository.findAll().forEach(o -> {
            if (o.getSmartphones().removeIf(p -> p.getId().equals(id))) {
                o.setTotalAmount(o.getSmartphones().stream()
                        .mapToDouble(Smartphone::getPrice)
                        .sum());
                orderRepository.save(o);
            }
        });
        smartphoneRepository.delete(phone);
    }

    @Transactional(readOnly = true)
    public List<Smartphone> filterSmartphones(String brand,
                                              String model,
                                              Double price,
                                              boolean nativeQuery) {
        if (nativeQuery) {
            return smartphoneRepository.filterSmartphonesNative(brand, model, price);
        }
        return smartphoneRepository.filterSmartphonesJpql(brand, model, price);
    }
}
