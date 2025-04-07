package app.controller;

import app.dto.OrderDto;
import app.dto.SmartphoneDto;
import app.mapper.OrderMapper;
import app.mapper.SmartphoneMapper;
import app.models.Order;
import app.service.OrderService;
import app.service.SmartphoneService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @Operation(summary = "Get all orders", description =
            "Retrieves a list of all orders and caches each order individually")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Orders retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Bad request")
    })
    @GetMapping
    public ResponseEntity<List<OrderDto>> getAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        List<OrderDto> dtos = orderMapper.toDtos(orders);
        return ResponseEntity.ok(dtos);
    }


    @Operation(summary = "Get order by ID", description =
            "Retrieves a single order by its ID using cache if available")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order found"),
        @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id)
                .map(orderMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(
            summary = "Create order",
            description = "Creates a new order using the provided OrderDto. "
                    + "The order is created with a list of smartphone IDs,"
                    + " and then cached accordingly."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid order data provided")
    })
    @PostMapping
    public ResponseEntity<OrderDto> createOrder(@RequestBody OrderDto orderDto) {
        Order order = orderMapper.toEntity(orderDto);
        Order savedOrder = orderService.createOrder(order, orderDto.getSmartphoneIds());
        return ResponseEntity.ok(orderMapper.toDto(savedOrder));
    }

    @Operation(
            summary = "Update order",
            description = "Updates an existing order identified by ID using the provided OrderDto."
                    + "If the provided list of smartphone IDs is empty,"
                    + " the order is deleted and a NO_CONTENT response "
                    + "with additional header information is returned."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Order updated successfully"),
        @ApiResponse(responseCode = "204", description =
                "Order deleted because no smartphones were provided"),
        @ApiResponse(responseCode = "400", description = "Invalid order data provided"),
        @ApiResponse(responseCode = "404", description = "Order not found")
    })
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

    @Operation(
            summary = "Delete order",
            description = "Deletes the order identified by the provided ID"
                    + " and updates the related caches accordingly."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Order deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Get orders by username",
            description = "Retrieves a list of orders for a given username. "
                    + "Allows switching between JPQL and native query"
                    + " by setting the nativeQuery parameter."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Orders retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters")
    })
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


    @SuppressWarnings("checkstyle:Indentation")
    @Operation(
            summary = "Get orders by smartphone criteria",
            description = "Retrieves orders that contain at least"
                    + " one smartphone matching the specified criteria: "
                    + "brand, model, minPrice, and maxPrice. "
                    + "Set nativeQuery=true to use native SQL query."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orders retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid filter parameters")
    })
    @GetMapping("/filterByPhone")
    public ResponseEntity<List<OrderDto>> getOrdersBySmartphoneCriteria(
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "false") boolean nativeQuery) {

        List<Order> orders = orderService.getOrdersBySmartphoneCriteria(brand, model,
                minPrice, maxPrice, nativeQuery);
        List<OrderDto> dtos = orders.stream()
                .map(orderMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
}
