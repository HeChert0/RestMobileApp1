
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
    void getAllSmartphones_shouldReturnAllSmartphones() {
        // Given
        when(smartphoneRepository.findAll()).thenReturn(testPhones);

        // When
        List<Smartphone> result = smartphoneService.getAllSmartphones();

        // Then
        assertEquals(testPhones, result);
        verify(smartphoneRepository).count();
        verify(smartphoneRepository).findAll();
    }

    @Test
    void getSmartphoneById_withCachedPhone_shouldReturnPhoneFromCache() {
        // Given
        when(smartphoneCache.get(1L)).thenReturn(testPhone);

        // When
        Optional<Smartphone> result = smartphoneService.getSmartphoneById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testPhone, result.get());
        verify(smartphoneCache).get(1L);
        verifyNoInteractions(smartphoneRepository);
    }

    @Test
    void getSmartphoneById_withNonCachedPhone_shouldFetchFromRepositoryAndUpdateCache() {
        // Given
        when(smartphoneCache.get(1L)).thenReturn(null);
        when(smartphoneRepository.findById(1L)).thenReturn(Optional.of(testPhone));

        // When
        Optional<Smartphone> result = smartphoneService.getSmartphoneById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testPhone, result.get());
        verify(smartphoneCache).get(1L);
        verify(smartphoneRepository).findById(1L);
        verify(smartphoneCache).put(1L, testPhone);
    }

    @Test
    void getSmartphoneById_withNonExistentPhone_shouldReturnEmpty() {
        // Given
        when(smartphoneCache.get(999L)).thenReturn(null);
        when(smartphoneRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<Smartphone> result = smartphoneService.getSmartphoneById(999L);

        // Then
        assertFalse(result.isPresent());
        verify(smartphoneCache).get(999L);
        verify(smartphoneRepository).findById(999L);
        verifyNoMoreInteractions(smartphoneCache);
    }

    @Test
    void saveSmartphone_shouldSaveAndCachePhone() {
        // Given
        Smartphone phoneToSave = new Smartphone("Google", "Pixel 6", 799.99);
        when(smartphoneRepository.save(phoneToSave)).thenReturn(phoneToSave);

        // When
        Smartphone result = smartphoneService.saveSmartphone(phoneToSave);

        // Then
        assertEquals(phoneToSave, result);
        verify(smartphoneRepository).save(phoneToSave);
        verify(smartphoneCache).put(phoneToSave.getId(), phoneToSave);
    }

    @Test
    void updateSmartphone_withExistingPhone_shouldUpdateAndCachePhone() {
        // Given
        Smartphone updatedPhone = new Smartphone("Apple", "iPhone 14", 1099.99);

        when(smartphoneRepository.findById(1L)).thenReturn(Optional.of(testPhone));
        when(smartphoneRepository.save(any(Smartphone.class))).thenAnswer(invocation -> {
            Smartphone savedPhone = invocation.getArgument(0);
            savedPhone.setId(1L);
            return savedPhone;
        });

        // When
        Smartphone result = smartphoneService.updateSmartphone(1L, updatedPhone);

        // Then
        assertNotNull(result);
        assertEquals("Apple", result.getBrand());
        assertEquals("iPhone 14", result.getModel());
        assertEquals(1099.99, result.getPrice(), 0.001);

        verify(smartphoneRepository).findById(1L);
        verify(smartphoneRepository).save(any(Smartphone.class));
        verify(smartphoneCache).put(eq(1L), any(Smartphone.class));

        // No price update, so no order updates
        verify(orderRepository, never()).findAll();
    }

    @Test
    void updateSmartphone_withPriceChange_shouldUpdateOrdersAndCache() {
        // Given
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

        // When
        Smartphone result = smartphoneService.updateSmartphone(1L, updatedPhone);

        // Then
        assertNotNull(result);
        assertEquals(1099.99, result.getPrice(), 0.001);

        verify(smartphoneRepository).findById(1L);
        verify(smartphoneRepository).save(any(Smartphone.class));
        verify(smartphoneCache).put(eq(1L), any(Smartphone.class));

        // Price changed, so orders should be updated
        verify(orderRepository).findAll();
        verify(orderRepository).save(any(Order.class));
        verify(orderCache).put(eq(1L), any(Order.class));
        verify(userCache).get(1L);
    }

    @Test
    void updateSmartphone_withNonExistentPhone_shouldReturnNull() {
        // Given
        Smartphone updatedPhone = new Smartphone("Apple", "iPhone 14", 1099.99);
        when(smartphoneRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Smartphone result = smartphoneService.updateSmartphone(999L, updatedPhone);

        // Then
        assertNull(result);
        verify(smartphoneRepository).findById(999L);
        verifyNoMoreInteractions(smartphoneRepository);
        verifyNoInteractions(smartphoneCache);
    }

    @Test
    void deleteSmartphone_withExistingPhone_shouldDeleteAndUpdateCache() {
        // Given
        List<Order> orders = new ArrayList<>();
        orders.add(testOrder);

        when(smartphoneRepository.findById(1L)).thenReturn(Optional.of(testPhone));
        when(orderRepository.findAll()).thenReturn(orders);
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // When
        smartphoneService.deleteSmartphone(1L);

        // Then
        verify(smartphoneRepository).findById(1L);
        verify(orderRepository).findAll();
        verify(smartphoneRepository).flush();
        verify(smartphoneRepository).delete(testPhone);
        verify(smartphoneCache).remove(1L);

        verify(orderRepository).save(any(Order.class));
        verify(orderCache).put(eq(1L), any(Order.class));
        verify(userCache).get(1L);
    }

    @Test
    void deleteSmartphone_withNonExistentPhone_shouldThrowException() {
        // Given
        when(smartphoneRepository.findById(999L)).thenThrow(
                new IllegalArgumentException("Smartphone with id 999 not found.")
        );

        // When & Then
        assertThrows(
                IllegalArgumentException.class,
                () -> smartphoneService.deleteSmartphone(999L)
        );
    }

    @Test
    void filterSmartphones_withJpql_shouldReturnFilteredPhones() {
        // Given
        when(smartphoneRepository.filterSmartphonesJpql("Apple", "iPhone", null))
                .thenReturn(Arrays.asList(testPhone));

        // When
        List<Smartphone> result = smartphoneService.filterSmartphones("Apple", "iPhone", null, false);

        // Then
        assertEquals(1, result.size());
        assertEquals(testPhone, result.get(0));
        verify(smartphoneRepository).filterSmartphonesJpql("Apple", "iPhone", null);
    }

    @Test
    void filterSmartphones_withNative_shouldReturnFilteredPhones() {
        // Given
        when(smartphoneRepository.filterSmartphonesNative("Samsung", null, 899.99))
                .thenReturn(Arrays.asList(testPhones.get(1)));

        // When
        List<Smartphone> result = smartphoneService.filterSmartphones("Samsung", null, 899.99, true);

        // Then
        assertEquals(1, result.size());
        assertEquals(testPhones.get(1), result.get(0));
        verify(smartphoneRepository).filterSmartphonesNative("Samsung", null, 899.99);
    }
}


