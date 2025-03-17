package app.service;

import app.dao.OrderRepository;
import app.dao.SmartphoneRepository;
import app.dao.UserRepository;
import app.models.Order;
import app.models.Smartphone;
import app.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final SmartphoneRepository smartphoneRepository;
    private final UserRepository userRepository;

    @Autowired
    public OrderService(OrderRepository orderRepository,
                        SmartphoneRepository smartphoneRepository,
                        UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.smartphoneRepository = smartphoneRepository;
        this.userRepository = userRepository;
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    public Order createOrder(Order order, List<Long> smartphoneIds) {
        // Устанавливаем связь со смартфонами, если переданы ID
        if (smartphoneIds != null && !smartphoneIds.isEmpty()) {
            List<Smartphone> phones = smartphoneRepository.findAllById(smartphoneIds);
            order.setSmartphones(phones);
        }
        return orderRepository.save(order);
    }

    public Order updateOrder(Long id, Order updatedOrder, List<Long> smartphoneIds) {
        return orderRepository.findById(id).map(existingOrder -> {
            existingOrder.setOrderDate(updatedOrder.getOrderDate());
            existingOrder.setTotalAmount(updatedOrder.getTotalAmount());
            existingOrder.setUser(updatedOrder.getUser());
            if (smartphoneIds != null) {
                List<Smartphone> phones = smartphoneRepository.findAllById(smartphoneIds);
                existingOrder.setSmartphones(phones);
            }
            return orderRepository.save(existingOrder);
        }).orElse(null);
    }


    public void deleteOrder(Long id){
        orderRepository.deleteById(id);
    }
}
