package app.service;

import app.cache.LruCache;
import app.dao.OrderRepository;
import app.dao.SmartphoneRepository;
import app.dao.UserRepository;
import app.exception.OrderNotFoundException;
import app.models.Order;
import app.models.Smartphone;
import app.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private SmartphoneRepository smartphoneRepository;
    @Mock
    private LruCache<Long, Order> orderCache;
    @Mock
    private LruCache<Long, User> userCache;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrderService orderService;

    private Order testOrder;
    private User testUser;
    private Smartphone testPhone;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");

        testPhone = new Smartphone("Apple", "iPhone 15", 999.99);
        testPhone.setId(1L);

        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setUser(testUser);
        testOrder.setSmartphones(List.of(testPhone));
        testOrder.setTotalAmount(999.99);
        testOrder.setOrderDate(LocalDate.now());

        // Сброс всех моков перед каждым тестом
        Mockito.reset(orderRepository, smartphoneRepository, orderCache, userCache, userRepository);
    }

    @Test
    void getOrderById_CacheHit() {
        when(orderCache.get(1L)).thenReturn(testOrder);

        Optional<Order> result = orderService.getOrderById(1L);

        assertTrue(result.isPresent());
        assertEquals(testOrder, result.get());
        verify(orderRepository, never()).findById(any());
    }

    @Test
    void getOrderById_CacheMiss() {
        when(orderCache.get(1L)).thenReturn(null);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        Optional<Order> result = orderService.getOrderById(1L);

        assertTrue(result.isPresent());
        verify(orderCache).put(eq(1L), eq(testOrder));
    }

    @Test
    @Transactional
    void createOrder_ValidData() {
        Order newOrder = new Order();
        newOrder.setUser(testUser);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(smartphoneRepository.findById(1L)).thenReturn(Optional.of(testPhone));
        when(orderRepository.save(any())).thenAnswer(invocation -> {
            Order saved = invocation.getArgument(0);
            saved.setId(1L); // Устанавливаем ID
            return saved;
        });

        Order savedOrder = orderService.createOrder(newOrder, List.of(1L));

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderCache).put(eq(1L), orderCaptor.capture());

        assertEquals(999.99, orderCaptor.getValue().getTotalAmount());
    }

    @Test
    @Transactional
    void updateOrder_RemoveSmartphones() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        Order result = orderService.updateOrder(1L, testOrder, Collections.emptyList());

        assertNull(result);
        verify(orderCache).remove(eq(1L));
    }

    @Test
    @Transactional
    void updateOrder_PriceRecalculation() {
        Smartphone newPhone = new Smartphone("Samsung", "S24", 899.99);
        newPhone.setId(2L);
        Order updatedOrder = new Order();
        updatedOrder.setUser(testUser);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(smartphoneRepository.findById(2L)).thenReturn(Optional.of(newPhone));
        when(orderRepository.save(any())).thenReturn(testOrder);

        orderService.updateOrder(1L, updatedOrder, List.of(2L));

        verify(orderCache).put(eq(1L), any(Order.class));
    }

    @Test
    void getOrdersByUserUsernameJpql() {
        when(orderRepository.findByUserUsernameJpql("testUser")).thenReturn(List.of(testOrder));

        List<Order> result = orderService.getOrdersByUserUsernameJpql("testUser");

        verify(orderCache).put(eq(1L), any(Order.class));
    }

    @Test
    void getOrdersBySmartphoneCriteria_NativeQuery() {
        when(orderRepository.findOrdersBySmartphoneCriteriaNative(any(), any(), any(), any()))
                .thenReturn(List.of(testOrder));

        List<Order> result = orderService.getOrdersBySmartphoneCriteria("Apple", "iPhone", 900.0, 1000.0, true);

        verify(orderCache).put(eq(1L), any(Order.class));
    }

    @Test
    void deleteOrder_WithCacheUpdate() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        orderService.deleteOrder(1L);

        verify(orderCache).remove(eq(1L));
    }
}