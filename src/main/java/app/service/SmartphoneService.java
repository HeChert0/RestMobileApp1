package app.service;

import app.cache.LruCache;
import app.dao.OrderRepository;
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
    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final LruCache<Long, Smartphone> smartphoneCache;
    private final LruCache<Long, Order> orderCache;
    private final LruCache<Long, app.models.User> userCache;

    @Autowired
    public SmartphoneService(SmartphoneRepository smartphoneRepository,
                             OrderRepository orderRepository,
                             OrderService orderService, LruCache<Long, Smartphone> smartphoneCache, LruCache<Long, Order> orderCache, LruCache<Long, app.models.User> userCache) {
        this.smartphoneRepository = smartphoneRepository;
        this.orderRepository = orderRepository;
        this.orderService = orderService;
        this.smartphoneCache = smartphoneCache;
        this.orderCache = orderCache;
        this.userCache = userCache;
    }

    public List<Smartphone> getAllSmartphones() {
        long dbCount = smartphoneRepository.count();
        return smartphoneRepository.findAll();
    }

    public Optional<Smartphone> getSmartphoneById(Long id) {
        return smartphoneRepository.findById(id);
    }

    @Transactional
    public Smartphone saveSmartphone(Smartphone smartphone) {
        return smartphoneRepository.save(smartphone);
    }

    @SuppressWarnings("checkstyle:VariableDeclarationUsageDistance")
    @Transactional
    public Smartphone updateSmartphone(Long id, Smartphone updatedSmartphone) {
        return smartphoneRepository.findById(id).map(existingSmartphone -> {
            double oldPrice = existingSmartphone.getPrice();
            existingSmartphone.setBrand(updatedSmartphone.getBrand());
            existingSmartphone.setModel(updatedSmartphone.getModel());
            existingSmartphone.setPrice(updatedSmartphone.getPrice());

            Smartphone savedSmartphone = smartphoneRepository.save(existingSmartphone);
            smartphoneCache.put(savedSmartphone.getId(), savedSmartphone);

            if (oldPrice != savedSmartphone.getPrice()) {
                List<Order> orders = orderRepository.findAll();
                orders.forEach(order -> {
                    boolean containsPhone = order.getSmartphones().stream()
                            .anyMatch(p -> p.getId().equals(savedSmartphone.getId()));
                    if (containsPhone) {
                        double newTotal = order.getSmartphones().stream()
                                .mapToDouble(p -> p.getId().equals(savedSmartphone.getId())
                                        ? savedSmartphone.getPrice()
                                        : p.getPrice())
                                .sum();
                        order.setTotalAmount(newTotal);
                        orderRepository.save(order);
                        orderCache.put(order.getId(), order);

                        app.models.User user = userCache.get(order.getUser().getId());
                        if (user != null) {
                            user.getOrders().removeIf(o -> o.getId().equals(order.getId()));
                            user.getOrders().add(order);
                            userCache.put(user.getId(), user);
                        }
                    }
                });
            }

            return savedSmartphone;
        }).orElse(null);
    }

    @Transactional
    public void deleteSmartphone(Long id) {
        Smartphone phone = smartphoneRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Smartphone with id " + id + " not found."));
        // Для каждого заказа удаляем ссылки на данный телефон
        List<Order> orders = orderRepository.findAll();
        orders.forEach(order -> {
            if (order.getSmartphones().removeIf(p -> p.getId().equals(id))) {
                double newTotal = order.getSmartphones().stream()
                        .mapToDouble(p -> p.getPrice())
                        .sum();
                order.setTotalAmount(newTotal);
                orderRepository.save(order);
                orderCache.put(order.getId(), order);
                // Обновляем кэш пользователя
                app.models.User user = userCache.get(order.getUser().getId());
                if (user != null) {
                    user.getOrders().removeIf(o -> o.getId().equals(order.getId()));
                    user.getOrders().add(order);
                    userCache.put(user.getId(), user);
                }
            }
        });
        smartphoneRepository.flush();
        smartphoneRepository.delete(phone);
        smartphoneCache.remove(id);
    }


    public List<Smartphone> filterSmartphones(String brand, String model, Double price) {
        long dbCount = smartphoneRepository.count();

        List<Smartphone> smartphones;
        smartphones = smartphoneRepository.findAll();

        return smartphones.stream()
                .filter(s -> brand == null || s.getBrand().equalsIgnoreCase(brand))
                .filter(s -> model == null || s.getModel().equalsIgnoreCase(model))
                .filter(s -> price == null || s.getPrice().equals(price))
                .collect(Collectors.toList());
    }
}
