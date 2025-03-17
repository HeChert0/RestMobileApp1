package app.service;

import app.dao.SmartphoneRepository;
import app.dto.OrderDTO;
import app.mapper.OrderMapper;
import app.models.Order;
import app.dao.OrderRepository;
import app.models.Smartphone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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

    public Order createOrder(OrderDTO orderDTO) {

        Order order = orderMapper.toEntity(orderDTO);

        if (orderDTO.getSmartphoneIds() != null && !orderDTO.getSmartphoneIds().isEmpty()) {
            List<Smartphone> phones = smartphoneRepository.findAllById(orderDTO.getSmartphoneIds());

            if (phones.size() < orderDTO.getSmartphoneIds().size()) {
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
