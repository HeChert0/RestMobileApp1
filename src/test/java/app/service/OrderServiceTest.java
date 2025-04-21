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

// Дополните ваш OrderServiceTest следующими методами:

    // 1. getAllOrders()
    @Test
    void getAllOrders_shouldReturnList() {
        Order sample = new Order();
        sample.setId(1L);
        when(orderRepository.findAll()).thenReturn(List.of(sample));

        List<Order> result = orderService.getAllOrders();

        assertEquals(1, result.size());
        assertEquals(sample, result.get(0));
        verify(orderRepository).findAll();
    }

    // 2. getOrderById – найден
    @Test
    void getOrderById_found() {
        Order sample = new Order();
        sample.setId(1L);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(sample));

        Optional<Order> result = orderService.getOrderById(1L);

        assertTrue(result.isPresent());
        assertEquals(sample, result.get());
        verify(orderRepository).findById(1L);
    }

    // 3. getOrderById – не найден
    @Test
    void getOrderById_notFound() {
        when(orderRepository.findById(2L)).thenReturn(Optional.empty());

        Optional<Order> result = orderService.getOrderById(2L);

        assertFalse(result.isPresent());
        verify(orderRepository).findById(2L);
    }

    // 4. createOrder – успех
    @Test
    void createOrder_success() {
        // подготовка
        User u = new User();
        u.setId(10L);
        Order input = new Order();
        input.setUser(u);
        // два смартфона
        Smartphone s1 = new Smartphone(); s1.setId(100L); s1.setPrice(50.);
        Smartphone s2 = new Smartphone(); s2.setId(200L); s2.setPrice(70.);
        when(userRepository.findById(10L)).thenReturn(Optional.of(u));
        when(smartphoneRepository.findById(100L)).thenReturn(Optional.of(s1));
        when(smartphoneRepository.findById(200L)).thenReturn(Optional.of(s2));
        when(orderRepository.save(any())).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(5L);
            return o;
        });

        Order result = orderService.createOrder(input, List.of(100L, 200L));

        assertEquals(5L, result.getId());
        assertEquals(u, result.getUser());
        assertEquals(120.0, result.getTotalAmount());
        assertEquals(2, result.getSmartphones().size());
        assertNotNull(result.getOrderDate());
        verify(orderRepository).save(any());
    }

    // 5. createOrder – нет пользователя
    @Test
    void createOrder_noUser_shouldThrow() {
        Order input = new Order();
        assertThrows(IllegalArgumentException.class,
                () -> orderService.createOrder(input, List.of(1L)));
    }

    // 6. createOrder – пользователь не найден
    @Test
    void createOrder_userNotFound_shouldThrow() {
        User u = new User(); u.setId(99L);
        Order input = new Order(); input.setUser(u);
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> orderService.createOrder(input, List.of(1L)));
        assertTrue(ex.getMessage().contains("User with ID 99 not found"));
    }


    // 8. updateOrder – успех
    @Test
    void updateOrder_success() {
        // подготовка существующего заказа
        Order existing = new Order();
        existing.setId(1L);
        existing.setUser(new User()); existing.getUser().setId(1L);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(existing));
        // новый пользователь
        User newU = new User(); newU.setId(2L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(newU));
        // новый смартфон
        Smartphone s = new Smartphone(); s.setId(50L); s.setPrice(25.);
        when(smartphoneRepository.findById(50L)).thenReturn(Optional.of(s));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Order updReq = new Order();
        updReq.setOrderDate(LocalDate.of(2021,1,1));
        User u2 = new User(); u2.setId(2L);
        updReq.setUser(u2);

        Order result = orderService.updateOrder(1L, updReq, List.of(50L));

        assertNotNull(result);
        assertEquals(newU, result.getUser());
        assertEquals(25.0, result.getTotalAmount());
        assertEquals(LocalDate.of(2021,1,1), result.getOrderDate());
        verify(orderRepository).save(existing);
    }

    // 9. updateOrder – удаление при пустом списке
    @Test
    void updateOrder_deleteAllSmartphones_returnsNull() {
        Order existing = new Order();
        existing.setId(1L);
        existing.setUser(new User()); existing.getUser().setId(1L);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(existing));
        User u = new User(); u.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(u));

        Order result = orderService.updateOrder(1L, new Order() {{ setUser(u); }}, Collections.emptyList());

        assertNull(result);
        verify(orderRepository).delete(existing);
    }

    // 10. updateOrder – заказ не найден
    @Test
    void updateOrder_notFound_shouldThrow() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class,
                () -> orderService.updateOrder(99L, new Order() {{ setUser(new User() {{ setId(1L); }}); }}, List.of(1L)));
    }

    // 11. deleteOrder – успех
    @Test
    void deleteOrder_success() {
        Order o = new Order(); o.setId(1L);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(o));

        orderService.deleteOrder(1L);

        verify(orderRepository).delete(o);
    }

    // 12. deleteOrder – не найден
    @Test
    void deleteOrder_notFound_shouldThrow() {
        when(orderRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class,
                () -> orderService.deleteOrder(2L));
    }

    // 13. getOrdersByUserUsernameJpql
    @Test
    void getOrdersByUserUsernameJpql_returnsList() {
        Order o = new Order(); o.setId(1L);
        when(orderRepository.findByUserUsernameJpql("john"))
                .thenReturn(List.of(o));

        List<Order> list = orderService.getOrdersByUserUsernameJpql("john");

        assertEquals(1, list.size());
        assertEquals(o, list.get(0));
        verify(orderRepository).findByUserUsernameJpql("john");
    }

    // 14. getOrdersBySmartphoneCriteria – native
    @Test
    void getOrdersBySmartphoneCriteria_native() {
        Order o = new Order(); o.setId(1L);
        when(orderRepository.findOrdersBySmartphoneCriteriaNative("A","B",1.,2.))
                .thenReturn(List.of(o));

        List<Order> list = orderService.getOrdersBySmartphoneCriteria("A","B",1.,2., true);

        assertEquals(1, list.size());
        verify(orderRepository).findOrdersBySmartphoneCriteriaNative("A","B",1.,2.);
    }

    // 15. getOrdersBySmartphoneCriteria – jpql
    @Test
    void getOrdersBySmartphoneCriteria_jpql() {
        Order o = new Order(); o.setId(2L);
        when(orderRepository.findOrdersBySmartphoneCriteriaJpql("X",null, null, null))
                .thenReturn(List.of(o));

        List<Order> list = orderService.getOrdersBySmartphoneCriteria("X",null,null,null, false);

        assertEquals(1, list.size());
        verify(orderRepository).findOrdersBySmartphoneCriteriaJpql("X",null,null,null);
    }

    @Test
    void createOrder_emptySmartphones_success() {
        User u = new User();
        u.setId(1L);
        Order input = new Order();
        input.setUser(u);
        when(userRepository.findById(1L)).thenReturn(Optional.of(u));
        when(orderRepository.save(any())).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(42L);
            return o;
        });

        Order result = orderService.createOrder(input, Collections.emptyList());

        assertNotNull(result);
        assertEquals(42L, result.getId());
        assertTrue(result.getSmartphones().isEmpty());
        assertEquals(0.0, result.getTotalAmount(), 0.001);
        verify(smartphoneRepository, never()).findById(anyLong());
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void createOrder_nullSmartphoneIds_shouldThrowNPE() {
        User u = new User();
        u.setId(1L);
        Order input = new Order();
        input.setUser(u);
        when(userRepository.findById(1L)).thenReturn(Optional.of(u));

        assertThrows(NullPointerException.class,
                () -> orderService.createOrder(input, null));
    }

    @Test
    void updateOrder_userNull_shouldThrow() {
        Order existing = new Order();
        existing.setId(1L);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(existing));

        Order updated = new Order(); // user == null
        assertThrows(IllegalArgumentException.class,
                () -> orderService.updateOrder(1L, updated, List.of(1L)));
    }

    @Test
    void updateOrder_userIdNull_shouldThrow() {
        Order existing = new Order();
        existing.setId(1L);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(existing));

        User u = new User(); // id == null
        Order updated = new Order();
        updated.setUser(u);

        assertThrows(IllegalArgumentException.class,
                () -> orderService.updateOrder(1L, updated, List.of(1L)));
    }

    @Test
    void updateOrder_userNotFound_shouldThrow() {
        Order existing = new Order();
        existing.setId(1L);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(existing));

        User u = new User();
        u.setId(99L);
        Order updated = new Order();
        updated.setUser(u);

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> orderService.updateOrder(1L, updated, List.of(1L)));
    }

    @Test
    void updateOrder_smartphoneNotFound_shouldThrow() {
        Order existing = new Order();
        existing.setId(1L);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(existing));

        User u = new User();
        u.setId(1L);
        Order updated = new Order();
        updated.setUser(u);
        when(userRepository.findById(1L)).thenReturn(Optional.of(u));

        // repository returns empty for smartphone
        when(smartphoneRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> orderService.updateOrder(1L, updated, List.of(5L)));
    }

    @Test
    void updateOrder_nullSmartphoneIds_deletesAndReturnsNull() {
        Order existing = new Order();
        existing.setId(1L);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(existing));

        User u = new User(); u.setId(1L);
        Order updated = new Order();
        updated.setUser(u);
        when(userRepository.findById(1L)).thenReturn(Optional.of(u));

        Order result = orderService.updateOrder(1L, updated, null);

        assertNull(result);
        verify(orderRepository).delete(existing);
    }

    @Test
    void getOrdersByUserUsernameNative_returnsList() {
        Order o = new Order();
        o.setId(7L);
        when(orderRepository.findByUserUsernameNative("alice"))
                .thenReturn(List.of(o));

        List<Order> list = orderService.getOrdersByUserUsernameNative("alice");

        assertEquals(1, list.size());
        assertEquals(o, list.get(0));
        verify(orderRepository).findByUserUsernameNative("alice");
    }

    @Test
    void updateOrder_orderDateNull_setsNow() {
        // existing
        Order existing = new Order();
        existing.setId(1L);
        existing.setUser(new User()); existing.getUser().setId(1L);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(existing));
        // user found
        User u = new User(); u.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(u));
        // one smartphone
        Smartphone s = new Smartphone(); s.setId(3L); s.setPrice(20.);
        when(smartphoneRepository.findById(3L)).thenReturn(Optional.of(s));
        // save returns the same
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Order upd = new Order();
        upd.setUser(u);
        upd.setOrderDate(null);

        Order result = orderService.updateOrder(1L, upd, List.of(3L));
        assertNotNull(result.getOrderDate());
        assertEquals(20.0, result.getTotalAmount());
    }

    @Test
    void createOrder_orderDateNull_setsNow() {
        User u = new User(); u.setId(1L);
        Order in = new Order();
        in.setUser(u);
        in.setOrderDate(null);
        Smartphone s = new Smartphone(); s.setId(2L); s.setPrice(10.);
        when(userRepository.findById(1L)).thenReturn(Optional.of(u));
        when(smartphoneRepository.findById(2L)).thenReturn(Optional.of(s));
        when(orderRepository.save(any())).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(7L);
            return o;
        });

        Order out = orderService.createOrder(in, List.of(2L));
        assertNotNull(out.getOrderDate());
        assertEquals(7L, out.getId());
        assertEquals(10.0, out.getTotalAmount());
    }
}


