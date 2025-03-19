package app.service;

import app.cache.InMemoryCache;
import app.dao.OrderRepository;
import app.dao.SmartphoneRepository;
import app.dao.UserRepository;
import app.dto.OrderDto;
import app.mapper.OrderMapper;
import app.models.Order;
import app.models.Smartphone;
import app.models.User;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final SmartphoneRepository smartphoneRepository;
    private final OrderMapper orderMapper;
    private final UserRepository userRepository;
    private final InMemoryCache<Long, Order> orderCache;

    @Autowired
    public OrderService(OrderRepository orderRepository,
                        SmartphoneRepository smartphoneRepository,
                        OrderMapper orderMapper,
                        UserRepository userRepository,
                        InMemoryCache<Long, Order> orderCache) {
        this.orderRepository = orderRepository;
        this.smartphoneRepository = smartphoneRepository;
        this.orderMapper = orderMapper;
        this.userRepository = userRepository;
        this.orderCache = orderCache;
    }

    @SneakyThrows
    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        long dbCount = orderRepository.count();
        int cacheSize = orderCache.size();

        if (cacheSize == dbCount && cacheSize > 0) {
            return orderCache.getAllValues();
        } else {
            Thread.sleep(2000);
            List<Order> orders = orderRepository.findAll();
            orders.forEach(order -> orderCache.put(order.getId(), order));
            return orders;
        }
    }


    public Optional<Order> getOrderById(Long id) {
        Order cachedOrder = orderCache.get(id);
        if (cachedOrder != null) {
            return Optional.of(cachedOrder);
        }
        Optional<Order> order = orderRepository.findById(id);
        order.ifPresent(o -> orderCache.put(o.getId(), o));
        return order;
    }

    @Transactional
    public Order createOrder(Order order, List<Long> smartphoneIds) {
        User user = userRepository.findById(order.getUser().getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "User with ID " + order.getUser().getId() + " not found"));

        if (smartphoneIds == null || smartphoneIds.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one smartphone.");
        }

        List<Smartphone> phones = smartphoneIds.stream()
                .map(id -> smartphoneRepository.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Smartphone with id " + id + " not found.")))
                .collect(Collectors.toList());

        order.setSmartphones(phones);
        double total = phones.stream().mapToDouble(Smartphone::getPrice).sum();
        order.setTotalAmount(total);

        if (order.getOrderDate() == null) {
            order.setOrderDate(LocalDate.now());
        }

        if (order.getUser() == null) {
            throw new IllegalArgumentException("Order must be associated with a user.");
        }

        Order savedOrder = orderRepository.save(order);
        orderCache.put(savedOrder.getId(), savedOrder);
        return orderRepository.save(order);
    }

    @Transactional
    public Order updateOrder(Long id, Order updatedOrder, List<Long> smartphoneIds) {
        return orderRepository.findById(id).map(existingOrder -> {
            existingOrder.setOrderDate(updatedOrder.getOrderDate() != null
                    ? updatedOrder.getOrderDate()
                    : LocalDate.now());
            existingOrder.setUser(updatedOrder.getUser());

            if (smartphoneIds == null || smartphoneIds.isEmpty()) {
                orderRepository.delete(existingOrder);
                return null;
            } else {
                List<Smartphone> phones = smartphoneIds.stream()
                        .map(sid -> smartphoneRepository.findById(sid)
                                .orElseThrow(() -> new IllegalArgumentException(
                                        "Smartphone with id " + sid + " not found.")))
                        .collect(Collectors.toList());
                existingOrder.setSmartphones(phones);
                double total = phones.stream().mapToDouble(Smartphone::getPrice).sum();
                existingOrder.setTotalAmount(total);
            }

            Order savedOrder = orderRepository.save(existingOrder);
            orderCache.update(savedOrder.getId(), savedOrder);
            return orderRepository.save(existingOrder);
        }).orElseThrow(() -> new IllegalArgumentException("Order with id " + id + " not found."));
    }

    @Transactional
    public void deleteOrder(Long id) {
        orderRepository.findById(id).ifPresent(order -> {
            if (order.getUser() != null) {
                User user = order.getUser();
                user.getOrders().remove(order);
                userRepository.save(user);
            }
            orderRepository.delete(order);
        });

        orderRepository.deleteById(id);
        orderCache.remove(id);
    }
}
