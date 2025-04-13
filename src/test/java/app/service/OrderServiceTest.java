package app.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import app.cache.LruCache;
import app.dao.OrderRepository;
import app.dao.SmartphoneRepository;
import app.dao.UserRepository;
import app.exception.OrderNotFoundException;
import app.exception.UserNotFoundException;
import app.models.Order;
import app.models.Smartphone;
import app.models.User;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private SmartphoneRepository smartphoneRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private LruCache<Long, Order> orderCache;

    @Mock
    private LruCache<Long, User> userCache;

    @InjectMocks
    private OrderService orderService;

    private Order order;
    private User user;
    private Smartphone smartphone;
    private final Long orderId = 100L;
    private final Long userId = 10L;

    @BeforeEach
    void setUp() {
        // Настраиваем пользователя: создаём копию с mutable списком заказов
        user = new User();
        user.setId(userId);
        user.setUsername("TestUser");
        // Инициализируем список заказов (mutable)
        user.setOrders(new java.util.ArrayList<>());

        // Настраиваем смартфон
        smartphone = new Smartphone("TestBrand", "TestModel", 500.0);
        smartphone.setId(1L);

        // Настраиваем заказ
        order = new Order();
        order.setId(orderId);
        order.setUser(user);
        order.setSmartphones(Arrays.asList(smartphone));
        order.setTotalAmount(500.0);
        order.setOrderDate(LocalDate.now());
    }

    @Test
    void createOrder_Success() {
        // Подготавливаем список id смартфонов
        var smartphoneIds = Arrays.asList(1L);

        // Мокаем поиск пользователя
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        // Мокаем поиск смартфона
        when(smartphoneRepository.findById(1L)).thenReturn(Optional.of(smartphone));
        // Мокаем сохранение заказа: возвращаем заказ с установленным id
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId(orderId);
            return o;
        });
        // Для корректного обновления кеша пользователя – пусть кеш вернёт mutable объект user
        when(userCache.get(userId)).thenReturn(user);

        Order result = orderService.createOrder(order, smartphoneIds);

        assertNotNull(result);
        assertEquals(500.0, result.getTotalAmount());
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(orderCache, times(1)).put(orderId, result);
        // Проверяем, что обновление кеша пользователя производится
        verify(userCache, times(1)).get(userId);
        verify(userCache, times(1)).put(eq(userId), any(User.class));
    }

    @Test
    void createOrder_UserNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> orderService.createOrder(order, Arrays.asList(1L)));
        assertTrue(exception.getMessage().contains("User with ID " + userId + " not found"));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrder_EmptySmartphoneList() {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> orderService.createOrder(order, Collections.emptyList()));
        assertTrue(exception.getMessage().contains("Order must contain at least one smartphone."));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void getOrderById_CacheHit() {
        when(orderCache.get(orderId)).thenReturn(order);
        Optional<Order> result = orderService.getOrderById(orderId);
        assertTrue(result.isPresent());
        assertEquals(orderId, result.get().getId());
        verify(orderCache, times(1)).get(orderId);
        verify(orderRepository, never()).findById(anyLong());
    }

    @Test
    void getOrderById_CacheMiss() {
        when(orderCache.get(orderId)).thenReturn(null);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        Optional<Order> result = orderService.getOrderById(orderId);
        assertTrue(result.isPresent());
        assertEquals(orderId, result.get().getId());
        verify(orderCache, times(1)).get(orderId);
        verify(orderRepository, times(1)).findById(orderId);
        verify(orderCache, times(1)).put(orderId, order);
    }

    @Test
    void updateOrder_WithNewSmartphones() {
        // Создаём новый смартфон и список id для обновления
        Smartphone newPhone = new Smartphone("NewBrand", "NewModel", 600.0);
        newPhone.setId(2L);

        Order updatedOrder = new Order();
        // Устанавливаем новую дату (например, завтрашнюю)
        updatedOrder.setOrderDate(LocalDate.now().plusDays(1));
        // Обновляем пользователя (будет тот же user)
        User updatedUser = new User();
        updatedUser.setId(userId);
        updatedOrder.setUser(updatedUser);

        var newSmartphoneIds = Arrays.asList(2L);
        // Мокаем поиск заказа
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        // Мокаем поиск нового смартфона
        when(smartphoneRepository.findById(2L)).thenReturn(Optional.of(newPhone));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));
        // Пусть кеш пользователя вернёт mutable объект user
        when(userCache.get(userId)).thenReturn(user);

        Order result = orderService.updateOrder(orderId, updatedOrder, newSmartphoneIds);
        assertNotNull(result);
        assertEquals(600.0, result.getTotalAmount());
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(orderCache, times(1)).put(orderId, result);
        verify(userCache, times(1)).get(userId);
        verify(userCache, times(1)).put(eq(userId), any(User.class));
    }

    @Test
    void updateOrder_EmptySmartphoneList_DeletesOrder() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        // Чтобы условие обновления кеша пользователя сработало, пусть get(userId) вернёт mutable объект user
        when(userCache.get(userId)).thenReturn(user);

        Order result = orderService.updateOrder(orderId, order, Collections.emptyList());
        assertNull(result);
        verify(orderRepository, times(1)).delete(order);
        verify(orderCache, times(1)).remove(orderId);
        verify(userCache, times(1)).get(userId);
        verify(userCache, times(1)).put(eq(userId), any(User.class));
    }

    @Test
    void deleteOrder_Success() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(userCache.get(userId)).thenReturn(user);

        orderService.deleteOrder(orderId);
        verify(orderRepository, times(1)).delete(order);
        verify(orderCache, times(1)).remove(orderId);
        verify(userCache, times(1)).get(userId);
        verify(userCache, times(1)).put(eq(userId), any(User.class));
    }

    @Test
    void deleteOrder_NotFound() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());
        assertThrows(OrderNotFoundException.class, () -> orderService.deleteOrder(orderId));
        verify(orderRepository, never()).delete(any(Order.class));
        verify(orderCache, never()).remove(anyLong());
    }
}
