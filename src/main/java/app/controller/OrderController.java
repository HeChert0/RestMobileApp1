package app.controller;

import app.dto.OrderDto;
import app.dto.SmartphoneDto;
import app.mapper.OrderMapper;
import app.mapper.SmartphoneMapper;
import app.models.Order;
import app.service.OrderService;
import app.service.SmartphoneService;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    public ResponseEntity<List<OrderDto>> getAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        List<OrderDto> dtos = orderMapper.toDtos(orders);
        return ResponseEntity.ok(dtos);
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
            return ResponseEntity.status(HttpStatus.NO_CONTENT)
                    .header("X-Info", "Order deleted because no smartphones were provided.")
                    .build();
        }
        return ResponseEntity.ok(orderMapper.toDto(updatedOrder));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/filter")
    public ResponseEntity<List<OrderDto>> getOrdersByUsername(
            @RequestParam String username,
            @RequestParam(defaultValue = "false") boolean nativeQuery) {

        List<Order> orders;
        if (nativeQuery) {
            orders = orderService.getOrdersByUserUsernameNative(username);
        } else {
            orders = orderService.getOrdersByUserUsernameJpql(username);
        }

        List<OrderDto> dtos = orders.stream()
                .map(orderMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
}
