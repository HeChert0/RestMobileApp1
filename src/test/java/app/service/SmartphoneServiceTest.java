
package app.service;

import app.cache.LruCache;
import app.dao.OrderRepository;
import app.dao.SmartphoneRepository;
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
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SmartphoneServiceTest {

    @Mock
    private SmartphoneRepository smartphoneRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private LruCache<Long, Smartphone> smartphoneCache;

    @Mock
    private LruCache<Long, Order> orderCache;

    @Mock
    private LruCache<Long, User> userCache;

    @InjectMocks
    private SmartphoneService smartphoneService;

    private Smartphone testPhone;
    private List<Smartphone> testPhones;
    private User testUser;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        testPhone = new Smartphone("Apple", "iPhone 13", 999.99);
        testPhone.setId(1L);

        Smartphone phone2 = new Smartphone("Samsung", "Galaxy S21", 899.99);
        phone2.setId(2L);

        testPhones = Arrays.asList(testPhone, phone2);

        var testResU = userCache.size();
        var testResO = orderCache.size();
        var testResS = smartphoneCache.size();

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");

        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setUser(testUser);
        testOrder.setSmartphones(new ArrayList<>(testPhones));
        testOrder.setOrderDate(LocalDate.now());
        testOrder.setTotalAmount(1899.98);
    }

    @Test
    void getSmartphoneById_withNonCachedPhone_shouldFetchFromRepositoryAndUpdateCache() {
        when(smartphoneRepository.findById(1L)).thenReturn(Optional.of(testPhone));

        Optional<Smartphone> result = smartphoneService.getSmartphoneById(1L);

        assertTrue(result.isPresent());
        assertEquals(testPhone, result.get());
        verify(smartphoneRepository).findById(1L);
    }

    @Test
    void getSmartphoneById_withNonExistentPhone_shouldReturnEmpty() {
        when(smartphoneRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Smartphone> result = smartphoneService.getSmartphoneById(999L);

        assertFalse(result.isPresent());
        verify(smartphoneRepository).findById(999L);
    }

    @Test
    void saveSmartphone_shouldSaveAndCachePhone() {
        Smartphone phoneToSave = new Smartphone("Google", "Pixel 6", 799.99);
        phoneToSave.setId(3L);
        when(smartphoneRepository.save(phoneToSave)).thenReturn(phoneToSave);

        Smartphone result = smartphoneService.saveSmartphone(phoneToSave);

        assertEquals(phoneToSave, result);
        verify(smartphoneRepository).save(phoneToSave);
    }


    @Test
    void updateSmartphone_withExistingPhone_shouldUpdateAndCachePhone() {
        Smartphone updatedPhone = new Smartphone("Apple", "iPhone 14", 1099.99);
        updatedPhone.setId(1L);

        when(smartphoneRepository.findById(1L)).thenReturn(Optional.of(testPhone));
        when(smartphoneRepository.save(any(Smartphone.class))).thenReturn(updatedPhone);

        Smartphone result = smartphoneService.updateSmartphone(1L, updatedPhone);

        assertNotNull(result);
        assertEquals(updatedPhone, result);
        verify(smartphoneRepository).findById(1L);
        verify(smartphoneRepository).save(any(Smartphone.class));
    }

    @Test
    void updateSmartphone_withPriceChange_shouldUpdateOrdersAndCache() {
        Smartphone updatedPhone = new Smartphone("Apple", "iPhone 13", 1099.99);
        List<Order> orders = Arrays.asList(testOrder);

        when(smartphoneRepository.findById(1L)).thenReturn(Optional.of(testPhone));
        when(smartphoneRepository.save(any(Smartphone.class))).thenAnswer(invocation -> {
            Smartphone savedPhone = invocation.getArgument(0);
            savedPhone.setId(1L);
            return savedPhone;
        });
        when(orderRepository.findAll()).thenReturn(orders);
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        Smartphone result = smartphoneService.updateSmartphone(1L, updatedPhone);

        assertNotNull(result);
        assertEquals(1099.99, result.getPrice(), 0.001);

        verify(smartphoneRepository).findById(1L);
        verify(smartphoneRepository).save(any(Smartphone.class));

        verify(orderRepository).findAll();
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void updateSmartphone_withNoOrdersAffected_shouldOnlyUpdatePhone() {
        Smartphone updatedPhone = new Smartphone("Apple", "iPhone 13", 1199.99);
        updatedPhone.setId(1L);

        when(smartphoneRepository.findById(1L)).thenReturn(Optional.of(testPhone));
        when(smartphoneRepository.save(any(Smartphone.class))).thenAnswer(invocation -> {
            Smartphone savedPhone = invocation.getArgument(0);
            savedPhone.setId(1L);
            return savedPhone;
        });
        when(orderRepository.findAll()).thenReturn(Collections.emptyList());

        Smartphone result = smartphoneService.updateSmartphone(1L, updatedPhone);

        assertNotNull(result);
        assertEquals(1199.99, result.getPrice(), 0.001);
        verify(smartphoneRepository).findById(1L);
        verify(smartphoneRepository).save(any(Smartphone.class));
        verify(orderRepository).findAll();
        verifyNoMoreInteractions(orderRepository);
    }

    @Test
    void filterSmartphones_withNullParameters_shouldReturnAllPhonesNative() {
        when(smartphoneRepository.filterSmartphonesNative(null, null, null))
                .thenReturn(testPhones);

        List<Smartphone> result = smartphoneService.filterSmartphones(null, null, null, true);

        assertEquals(testPhones.size(), result.size());
        verify(smartphoneRepository).filterSmartphonesNative(null, null, null);
    }

    @Test
    void deleteSmartphone_withExistingPhone_shouldDeleteAndUpdateCache() {
        when(smartphoneRepository.findById(1L)).thenReturn(Optional.of(testPhone));
        when(orderRepository.findAll()).thenReturn(Arrays.asList(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        smartphoneService.deleteSmartphone(1L);

        verify(smartphoneRepository).findById(1L);
        verify(smartphoneRepository).delete(testPhone);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void deleteSmartphone_withNoOrders_shouldDeleteSuccessfully() {
        when(smartphoneRepository.findById(1L)).thenReturn(Optional.of(testPhone));
        when(orderRepository.findAll()).thenReturn(Collections.emptyList());

        smartphoneService.deleteSmartphone(1L);

        verify(smartphoneRepository).findById(1L);
        verify(smartphoneRepository).delete(testPhone);
        verify(orderRepository).findAll();
    }

    @Test
    void deleteSmartphone_withNoOrders_shouldDeletePhone() {
        when(smartphoneRepository.findById(1L)).thenReturn(Optional.of(testPhone));
        when(orderRepository.findAll()).thenReturn(Collections.emptyList());

        smartphoneService.deleteSmartphone(1L);

        verify(smartphoneRepository).findById(1L);
        verify(smartphoneRepository).delete(testPhone);
        verify(orderRepository).findAll();
    }

    @Test
    void filterSmartphones_withAllNullParameters_shouldReturnAllPhones() {
        when(smartphoneRepository.filterSmartphonesJpql(null, null, null))
                .thenReturn(testPhones);

        List<Smartphone> result = smartphoneService.filterSmartphones(null, null, null, false);

        assertEquals(testPhones.size(), result.size());
        verify(smartphoneRepository).filterSmartphonesJpql(null, null, null);
    }

    @Test
    void deleteSmartphone_withNonExistentPhone_shouldThrowException() {
        when(smartphoneRepository.findById(999L)).thenThrow(
                new IllegalArgumentException("Smartphone with id 999 not found.")
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> smartphoneService.deleteSmartphone(999L)
        );
    }


    @Test
    void filterSmartphones_withJpql_shouldReturnFilteredPhones() {
        when(smartphoneRepository.filterSmartphonesJpql("Apple", "iPhone", null))
                .thenReturn(Arrays.asList(testPhone));

        List<Smartphone> result = smartphoneService.filterSmartphones("Apple", "iPhone", null, false);

        assertEquals(1, result.size());
        assertEquals(testPhone, result.get(0));
        verify(smartphoneRepository).filterSmartphonesJpql("Apple", "iPhone", null);
    }

    @Test
    void filterSmartphones_withNative_shouldReturnFilteredPhones() {
        when(smartphoneRepository.filterSmartphonesNative("Samsung", null, 899.99))
                .thenReturn(Arrays.asList(testPhones.get(1)));

        List<Smartphone> result = smartphoneService.filterSmartphones("Samsung", null, 899.99, true);

        assertEquals(1, result.size());
        assertEquals(testPhones.get(1), result.get(0));
        verify(smartphoneRepository).filterSmartphonesNative("Samsung", null, 899.99);
    }

    @Test
    void updateSmartphone_withSamePrice_shouldNotInvokeOrderRepo() {
        Smartphone updatedSame = new Smartphone("Apple", "iPhone 13 Pro", 999.99);
        updatedSame.setId(1L);

        when(smartphoneRepository.findById(1L)).thenReturn(Optional.of(testPhone));
        when(smartphoneRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Smartphone result = smartphoneService.updateSmartphone(1L, updatedSame);

        assertNotNull(result);
        assertEquals(999.99, result.getPrice(), 0.001);
        verify(smartphoneRepository).findById(1L);
        verify(smartphoneRepository).save(any(Smartphone.class));
        verify(orderRepository, never()).findAll();
    }

}


