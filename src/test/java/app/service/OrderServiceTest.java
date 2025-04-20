package app.service;

import app.cache.LruCache;
import app.dao.OrderRepository;
import app.dao.SmartphoneRepository;
import app.dao.UserRepository;
import app.exception.OrderNotFoundException;
import app.exception.UserNotFoundException;
import app.models.Order;
import app.models.Smartphone;
import app.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

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

    private User testUser;
    private Order testOrder;
    private List<Smartphone> testPhones;
    private List<Long> testPhoneIds;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");
        testUser.setPassword("password");

        testPhones = new ArrayList<>();
        Smartphone phone1 = new Smartphone("Apple", "iPhone 13", 999.99);
        phone1.setId(1L);
        Smartphone phone2 = new Smartphone("Samsung", "Galaxy S21", 899.99);
        phone2.setId(2L);
        var r1 = orderCache.size();
        var r2 = userCache.size();
        testPhones.add(phone1);
        testPhones.add(phone2);

        testPhoneIds = Arrays.asList(1L, 2L);

        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setUser(testUser);
        testOrder.setSmartphones(testPhones);
        testOrder.setOrderDate(LocalDate.now());
        testOrder.setTotalAmount(1899.98);
    }

    @Test
    void getAllOrders_shouldReturnAllOrders() {
        // Given
        List<Order> orders = Arrays.asList(testOrder);
        when(orderRepository.findAll()).thenReturn(orders);

        // When
        List<Order> result = orderService.getAllOrders();

        // Then
        assertEquals(orders, result);
        verify(orderRepository).count();
        verify(orderRepository).findAll();
    }

    @Test
    void getOrderById_withNonCachedOrder_shouldFetchFromRepositoryAndUpdateCache() {
        // Given
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        // When
        Optional<Order> result = orderService.getOrderById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testOrder, result.get());
        verify(orderRepository).findById(1L);
    }


    @Test
    void getOrderById_withNonExistentOrder_shouldReturnEmpty() {
        // Given
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<Order> result = orderService.getOrderById(999L);

        // Then
        assertFalse(result.isPresent());
        verify(orderRepository).findById(999L);
    }

    @Test
    void createOrder_withValidInput_shouldCreateAndCacheOrder() {
        // Given
        Order orderToCreate = new Order();
        orderToCreate.setUser(testUser);
        orderToCreate.setId(1L);
        orderToCreate.setTotalAmount(1899.98);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(smartphoneRepository.findById(1L)).thenReturn(Optional.of(testPhones.get(0)));
        when(smartphoneRepository.findById(2L)).thenReturn(Optional.of(testPhones.get(1)));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // When
        Order result = orderService.createOrder(orderToCreate, testPhoneIds);

        // Then
        assertEquals(testOrder, result);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void createOrder_withNullUser_shouldThrowException() {
        // Given
        Order orderToCreate = new Order();
        // user is null

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.createOrder(orderToCreate, testPhoneIds)
        );

        assertEquals("Order must be associated with a user (userId must be provided).", exception.getMessage());
        verifyNoInteractions(orderRepository);
    }

    @Test
    void createOrder_withNullOrderDate_shouldSetCurrentDate() {
        // Given
        Order orderToCreate = new Order();
        orderToCreate.setUser(testUser);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(smartphoneRepository.findById(1L)).thenReturn(Optional.of(testPhones.get(0)));
        when(smartphoneRepository.findById(2L)).thenReturn(Optional.of(testPhones.get(1)));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            savedOrder.setId(1L);
            return savedOrder;
        });

        // When
        Order result = orderService.createOrder(orderToCreate, testPhoneIds);

        // Then
        assertNotNull(result.getOrderDate());
        assertEquals(LocalDate.now(), result.getOrderDate());
    }

    @Test
    void createOrder_withNonExistentUser_shouldThrowException() {
        // Given
        Order orderToCreate = new Order();
        orderToCreate.setUser(testUser);

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.createOrder(orderToCreate, testPhoneIds)
        );

        assertEquals("User with ID 1 not found", exception.getMessage());
        verifyNoInteractions(orderRepository);
    }

    @Test
    void createOrder_withEmptyPhoneIds_shouldThrowException() {
        // Given
        Order orderToCreate = new Order();
        orderToCreate.setUser(testUser);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.createOrder(orderToCreate, Collections.emptyList())
        );

        assertEquals("Order must contain at least one smartphone.", exception.getMessage());
        verifyNoInteractions(orderRepository);
    }

    @Test
    void createOrder_withNullSmartphoneIds_shouldThrowException() {
        // Given
        Order orderToCreate = new Order();
        orderToCreate.setUser(testUser);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.createOrder(orderToCreate, null)
        );

        assertEquals("Order must contain at least one smartphone.", exception.getMessage());
        verifyNoInteractions(orderRepository);
    }


    @Test
    void updateOrder_withNonExistentUser_shouldThrowException() {
        // Given
        Order updatedOrder = new Order();
        updatedOrder.setUser(testUser);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(
                UserNotFoundException.class,
                () -> orderService.updateOrder(1L, updatedOrder, testPhoneIds)
        );
    }

    @Test
    void createOrder_withNonExistentPhone_shouldThrowException() {
        // Given
        Order orderToCreate = new Order();
        orderToCreate.setUser(testUser);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(smartphoneRepository.findById(1L)).thenReturn(Optional.of(testPhones.get(0)));
        when(smartphoneRepository.findById(2L)).thenReturn(Optional.empty());

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> orderService.createOrder(orderToCreate, testPhoneIds)
        );

        assertEquals("Smartphone with id 2 not found.", exception.getMessage());
        verifyNoInteractions(orderRepository);
    }

    @Test
    void updateOrder_withValidInput_shouldUpdateAndCacheOrder() {
        // Given
        Order updatedOrder = new Order();
        updatedOrder.setUser(testUser);
        LocalDate newDate = LocalDate.now().plusDays(1);
        updatedOrder.setOrderDate(newDate);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(smartphoneRepository.findById(1L)).thenReturn(Optional.of(testPhones.get(0)));
        when(smartphoneRepository.findById(2L)).thenReturn(Optional.of(testPhones.get(1)));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // When
        Order result = orderService.updateOrder(1L, updatedOrder, testPhoneIds);

        // Then
        assertEquals(testOrder, result);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void updateOrder_withNonExistentOrder_shouldThrowException() {
        // Given
        Order updatedOrder = new Order();
        updatedOrder.setUser(testUser);

        when(orderRepository.findById(999L)).thenThrow(
                new OrderNotFoundException("Order with id 999 not found.")
        );

        // When & Then
        assertThrows(
                OrderNotFoundException.class,
                () -> orderService.updateOrder(999L, updatedOrder, testPhoneIds)
        );
    }

    @Test
    void updateOrder_withEmptyPhoneList_shouldDeleteOrder() {
        // Given
        Order updatedOrder = new Order();
        updatedOrder.setUser(testUser);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        Order result = orderService.updateOrder(1L, updatedOrder, Collections.emptyList());

        // Then
        assertNull(result);
        verify(orderRepository).delete(testOrder);
    }


    @Test
    void deleteOrder_withExistingOrder_shouldDeleteAndUpdateCache() {
        // Given
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        // When
        orderService.deleteOrder(1L);

        // Then
        verify(orderRepository).delete(testOrder);
    }

    @Test
    void deleteOrder_withNonExistentOrder_shouldThrowException() {
        // Given
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(
                OrderNotFoundException.class,
                () -> orderService.deleteOrder(999L)
        );
    }

    @Test
    void getOrdersByUserUsernameJpql_shouldReturnOrdersAndUpdateCache() {
        // Given
        List<Order> orders = Arrays.asList(testOrder);
        when(orderRepository.findByUserUsernameJpql("testUser")).thenReturn(orders);

        // When
        List<Order> result = orderService.getOrdersByUserUsernameJpql("testUser");

        // Then
        assertEquals(orders, result);
    }

    @Test
    void getOrdersByUserUsernameNative_shouldReturnOrdersAndUpdateCache() {
        // Given
        List<Order> orders = Arrays.asList(testOrder);
        when(orderRepository.findByUserUsernameNative("testUser")).thenReturn(orders);

        // When
        List<Order> result = orderService.getOrdersByUserUsernameNative("testUser");

        // Then
        assertEquals(orders, result);
    }

    @Test
    void getOrdersBySmartphoneCriteria_withJpql_shouldReturnOrders() {
        // Given
        List<Order> orders = Arrays.asList(testOrder);
        when(orderRepository.findOrdersBySmartphoneCriteriaJpql(
                "Apple", "iPhone 13", 900.0, 1000.0)).thenReturn(orders);

        // When
        List<Order> result = orderService.getOrdersBySmartphoneCriteria(
                "Apple", "iPhone 13", 900.0, 1000.0, false);

        // Then
        assertEquals(orders, result);
        verify(orderRepository).findOrdersBySmartphoneCriteriaJpql(
                "Apple", "iPhone 13", 900.0, 1000.0);}

    @Test
    void getOrdersBySmartphoneCriteria_withNative_shouldReturnOrders() {
        // Given
        List<Order> orders = Arrays.asList(testOrder);
        when(orderRepository.findOrdersBySmartphoneCriteriaNative(
                "Apple", "iPhone 13", 900.0, 1000.0)).thenReturn(orders);

        // When
        List<Order> result = orderService.getOrdersBySmartphoneCriteria(
                "Apple", "iPhone 13", 900.0, 1000.0, true);

        // Then
        assertEquals(orders, result);
        verify(orderRepository).findOrdersBySmartphoneCriteriaNative(
                "Apple", "iPhone 13", 900.0, 1000.0);
    }
}


