package app.controller;

import app.dto.OrderDto;
import app.dto.SmartphoneDto;
import app.mapper.OrderMapper;
import app.mapper.SmartphoneMapper;
import app.models.Order;
import app.service.OrderService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import app.service.SmartphoneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final OrderMapper orderMapper;
    private final SmartphoneService smartphoneService;
    private final SmartphoneMapper smartphoneMapper;

    @Autowired
    public OrderController(OrderService orderService, OrderMapper orderMapper,
                           SmartphoneService smartphoneService, SmartphoneMapper smartphoneMapper) {
        this.orderService = orderService;
        this.orderMapper = orderMapper;
        this.smartphoneService = smartphoneService;
        this.smartphoneMapper = smartphoneMapper;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllOrders() {
        List<Order> orders = orderService.getAllOrders();

        List<OrderDto> orderDtos = orders.stream()
                .map(orderMapper::toDto)
                .collect(Collectors.toList());

        List<Map<String, Object>> response = orderDtos.stream().map(orderDto -> {
            Map<String, Object> orderMap = new LinkedHashMap<>();
            orderMap.put("id", orderDto.getId());
            orderMap.put("userId", orderDto.getUserId());
            orderMap.put("orderDate", orderDto.getOrderDate());
            orderMap.put("totalAmount", orderDto.getTotalAmount());
            // Преобразуем список ID в список SmartphoneDTO
            List<SmartphoneDto> smartphones = orderDto.getSmartphoneIds().stream()
                    .map(id -> smartphoneService.getSmartphoneById(id)
                            .map(smartphoneMapper::toDto)
                            .orElse(null))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            orderMap.put("smartphones", smartphones);
            return orderMap;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }


    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id)
                .map(orderMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<OrderDto> createOrder(@RequestBody OrderDto orderDto) {
        Order order = orderMapper.toEntity(orderDto);
        Order savedOrder = orderService.createOrder(order, orderDto.getSmartphoneIds());
        return ResponseEntity.ok(orderMapper.toDto(savedOrder));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrderDto> updateOrder(@PathVariable Long id,
                                                @RequestBody OrderDto orderDto) {
        Order order = orderMapper.toEntity(orderDto);
        Order updatedOrder = orderService.updateOrder(id, order, orderDto.getSmartphoneIds());
        if (updatedOrder == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(orderMapper.toDto(updatedOrder));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }
}
