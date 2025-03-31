package app.service;

import app.cache.LruCache;
import app.dao.OrderRepository;
import app.dao.SmartphoneRepository;
import app.dao.UserRepository;
import app.exception.OrderNotFoundException;
import app.exception.UserNotFoundException;
import app.mapper.OrderMapper;
import app.models.Order;
import app.models.Smartphone;
import app.models.User;
import java.time.LocalDate;
import java.util.ArrayList;
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
    private final LruCache<Long, Order> orderCache;
    private final LruCache<Long, User> userCache;
    private final UserRepository userRepository;

    @Autowired
    public OrderService(OrderRepository orderRepository,
                        SmartphoneRepository smartphoneRepository,
                        LruCache<Long, Order> orderCache,
                        LruCache<Long, User> userCache,
                        UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.smartphoneRepository = smartphoneRepository;
        this.orderCache = orderCache;
        this.userCache = userCache;
        this.userRepository = userRepository;
    }

    @SneakyThrows
    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        long dbCount = orderRepository.count();
        return orderRepository.findAll();
    }


    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    @Transactional
    public Order createOrder(Order order, List<Long> smartphoneIds) {

        if (order.getUser() == null || order.getUser().getId() == null) {
            throw new IllegalArgumentException(
                    "Order must be associated with a user (userId must be provided).");
        }
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

        order.setUser(user);
        order.setSmartphones(phones);

        double total = phones.stream().mapToDouble(Smartphone::getPrice).sum();
        order.setTotalAmount(total);

        if (order.getOrderDate() == null) {
            order.setOrderDate(LocalDate.now());
        }
        Order savedOrder = orderRepository.save(order);

        orderCache.put(savedOrder.getId(), savedOrder);
        User cachedUser = userCache.get(user.getId());
        if (cachedUser != null) {
            cachedUser.getOrders().add(savedOrder);
            userCache.put(user.getId(), cachedUser);
        }
        return savedOrder;
    }

    @Transactional
    public Order updateOrder(Long id, Order updatedOrder, List<Long> smartphoneIds) {
        Order existingOrder = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(
                        "Order with id " + id + " not found."));
        existingOrder.setOrderDate(updatedOrder.getOrderDate() != null
                ? updatedOrder.getOrderDate() : LocalDate.now());
        if (updatedOrder.getUser() == null || updatedOrder.getUser().getId() == null) {
            throw new IllegalArgumentException(
                    "Order must be associated with a user (userId must be provided).");
        }
        User user = userRepository.findById(updatedOrder.getUser().getId())
                .orElseThrow(() -> new UserNotFoundException(
                        "User with id " + updatedOrder.getUser().getId() + " not found."));
        existingOrder.setUser(user);

        if (smartphoneIds == null || smartphoneIds.isEmpty()) {
            orderRepository.delete(existingOrder);
            orderCache.remove(id);
            User cachedUser = userCache.get(user.getId());
            if (cachedUser != null) {
                cachedUser.getOrders().removeIf(o -> o.getId().equals(id));
                userCache.put(user.getId(), cachedUser);
            }
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
        orderCache.put(savedOrder.getId(), savedOrder);
        User cachedUser = userCache.get(savedOrder.getUser().getId());
        if (cachedUser != null) {
            cachedUser.getOrders().removeIf(o -> o.getId().equals(savedOrder.getId()));
            cachedUser.getOrders().add(savedOrder);
            userCache.put(savedOrder.getUser().getId(), cachedUser);
        }
        return savedOrder;
    }

    @Transactional
    public void deleteOrder(Long id) {
        Optional<Order> orderOpt = orderRepository.findById(id);
        if (orderOpt.isPresent()) {
            Order order = orderOpt.get();
            Long userId = order.getUser().getId();

            orderRepository.delete(order);
            orderCache.remove(id);
            User cachedUser = userCache.get(userId);
            if (cachedUser != null) {
                cachedUser.getOrders().removeIf(o -> o.getId().equals(id));
                userCache.put(userId, cachedUser);
            }
        }
    }

    //    private void updateUserCacheForOrder(Long userId) {
    //        Optional<User> userOpt = userRepository.findWithOrdersById(userId);
    //    }



    @Transactional(readOnly = true)
    public List<Order> getOrdersByUserUsernameJpql(String username) {
        return orderRepository.findByUserUsernameJpql(username);
    }

    @Transactional(readOnly = true)
    public List<Order> getOrdersByUserUsernameNative(String username) {
        return orderRepository.findByUserUsernameNative(username);
    }

    @Transactional
    public void updateOrdersTotalBySmartphone(Smartphone smartphone) {
        List<Order> orders = new ArrayList<>(smartphone.getOrders());

        for (Order order : orders) {
            double newTotal = order.getSmartphones().stream()
                    .mapToDouble(Smartphone::getPrice)
                    .sum();
            order.setTotalAmount(newTotal);
            orderRepository.save(order);
        }
    }
}
