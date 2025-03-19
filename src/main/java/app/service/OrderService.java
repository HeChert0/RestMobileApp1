package app.service;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final SmartphoneRepository smartphoneRepository;
    private final OrderMapper orderMapper;
    private final UserRepository userRepository;

    @Autowired
    public OrderService(OrderRepository orderRepository,
                        SmartphoneRepository smartphoneRepository,
                        OrderMapper orderMapper,
                        UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.smartphoneRepository = smartphoneRepository;
        this.orderMapper = orderMapper;
        this.userRepository = userRepository;
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findWithSmartphonesById(id);
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
    }
}
