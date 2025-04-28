package app.service;

import app.dao.OrderRepository;
import app.dao.SmartphoneRepository;
import app.dao.UserRepository;
import app.exception.OrderNotFoundException;
import app.exception.UserNotFoundException;
import app.models.Order;
import app.models.Smartphone;
import app.models.User;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = {"orders"})
public class OrderService {

    private final OrderRepository orderRepository;
    private final SmartphoneRepository smartphoneRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Cacheable(value = "orders", key = "#id")
    @Transactional(readOnly = true)
    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    @Caching(
            put = @CachePut(key = "#result.id"),
            evict = @CacheEvict(cacheNames = "users", key = "#order.user.id")
    )
    @Transactional
    public Order createOrder(Order order, List<Long> smartphoneIds) {
        if (order.getUser() == null || order.getUser().getId() == null) {
            throw new IllegalArgumentException("Order must be associated with a user");
        }
        User user = userRepository.findById(order.getUser().getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "User with ID " + order.getUser().getId() + " not found"));

        List<Smartphone> phones = smartphoneIds.stream()
                .map(id -> smartphoneRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Smartphone with id " + id + " not found.")))
                .collect(Collectors.toList());

        order.setUser(user);
        order.setSmartphones(phones);
        order.setTotalAmount(phones.stream().mapToDouble(Smartphone::getPrice).sum());
        if (order.getOrderDate() == null) {
            order.setOrderDate(LocalDate.now());
        }
        return orderRepository.save(order);
    }

    @Caching(
            put = @CachePut(key = "#id"),
            evict = @CacheEvict(cacheNames = "users", key = "#updatedOrder.user.id")
    )
    @Transactional
    public Order updateOrder(Long id, Order updatedOrder, List<Long> smartphoneIds) {
        Order existing = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found: " + id));

        existing.setOrderDate(updatedOrder.getOrderDate() != null
                ? updatedOrder.getOrderDate() : LocalDate.now());

        if (updatedOrder.getUser() == null || updatedOrder.getUser().getId() == null) {
            throw new IllegalArgumentException("Order must be associated with a user");
        }
        User user = userRepository.findById(updatedOrder.getUser().getId())
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found: " + updatedOrder.getUser().getId()));
        existing.setUser(user);

        if (smartphoneIds == null || smartphoneIds.isEmpty()) {
            orderRepository.delete(existing);
            return null;
        }

        List<Smartphone> phones = smartphoneIds.stream()
                .map(sid -> smartphoneRepository.findById(sid)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Smartphone with id " + sid + " not found.")))
                .collect(Collectors.toList());
        existing.setSmartphones(phones);
        existing.setTotalAmount(phones.stream().mapToDouble(Smartphone::getPrice).sum());

        Order savedOrder = orderRepository.save(existing);
        return savedOrder;
    }

    @Caching(
            evict = {
                    @CacheEvict(key = "#id"),
                    @CacheEvict(cacheNames = "users", allEntries = true)
            }
    )
    @Transactional
    public void deleteOrder(Long id) {
        orderRepository.findById(id).ifPresentOrElse(
                orderRepository::delete,
                () -> {
                    throw new OrderNotFoundException("Order not found: " + id); }
        );
    }

    @Transactional(readOnly = true)
    public List<Order> getOrdersByUserUsernameJpql(String username) {
        return orderRepository.findByUserUsernameJpql(username);
    }

    @Transactional(readOnly = true)
    public List<Order> getOrdersByUserUsernameNative(String username) {
        return orderRepository.findByUserUsernameNative(username);
    }

    @Transactional(readOnly = true)
    public List<Order> getOrdersBySmartphoneCriteria(String brand, String model,
                                                     Double minPrice, Double maxPrice,
                                                     boolean nativeQuery) {
        if (nativeQuery) {
            return orderRepository.findOrdersBySmartphoneCriteriaNative(
                    brand, model, minPrice, maxPrice);
        }
        return orderRepository.findOrdersBySmartphoneCriteriaJpql(
                brand, model, minPrice, maxPrice);
    }
}
