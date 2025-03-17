package app.service;

import app.dao.OrderRepository;
import app.dao.SmartphoneRepository;
import app.dto.OrderDto;
import app.mapper.OrderMapper;
import app.models.Order;
import app.models.Smartphone;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final SmartphoneRepository smartphoneRepository;

    @Autowired
    public OrderService(OrderRepository orderRepository,
                        SmartphoneRepository smartphoneRepository,
                        OrderMapper orderMapper) {
        this.orderRepository = orderRepository;
        this.smartphoneRepository = smartphoneRepository;
        this.orderMapper = orderMapper;
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    public Order createOrder(OrderDto orderDto) {

        Order order = orderMapper.toEntity(orderDto);

        if (orderDto.getSmartphoneIds() != null && !orderDto.getSmartphoneIds().isEmpty()) {
            List<Smartphone> phones = smartphoneRepository.findAllById(orderDto.getSmartphoneIds());

            if (phones.size() < orderDto.getSmartphoneIds().size()) {
                throw new IllegalArgumentException("Some smartphone IDs do not exist!");
            }

            for (Smartphone phone : phones) {
                phone.setOrder(order);
            }
            order.setSmartphones(phones);
        }

        return orderRepository.save(order);
    }

    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }
}
