package app.service;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import app.cache.LruCache;
import app.dao.OrderRepository;
import app.dao.SmartphoneRepository;
import app.models.Smartphone;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SmartphoneServiceTest {

    @Mock
    private SmartphoneRepository smartphoneRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private LruCache<Long, Smartphone> smartphoneCache;

    @InjectMocks
    private SmartphoneService smartphoneService;

    private Smartphone smartphone1;
    private Smartphone smartphone2;

    @BeforeEach
    void setUp() throws Exception {
        // Создаем два тестовых смартфона
        smartphone1 = new Smartphone("Apple", "iPhone 13", 799.99);
        smartphone2 = new Smartphone("Samsung", "Galaxy S21", 699.99);

        // Устанавливаем идентификаторы через рефлексию, если нет сеттеров для id
        java.lang.reflect.Field idField = Smartphone.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(smartphone1, 1L);
        idField.set(smartphone2, 2L);
    }

    @Test
    void testGetSmartphoneById_CacheHit() {
        // Если смартфон уже есть в кеше, то не вызывается репозиторий
        when(smartphoneCache.get(1L)).thenReturn(smartphone1);

        Optional<Smartphone> result = smartphoneService.getSmartphoneById(1L);
        assertTrue(result.isPresent());
        assertEquals(smartphone1, result.get());
        verify(smartphoneCache, times(1)).get(1L);
        verify(smartphoneRepository, never()).findById(anyLong());
    }

    @Test
    void testGetSmartphoneById_CacheMiss() {
        // Если кеш не содержит нужного объекта, то происходит запрос в репозиторий
        when(smartphoneCache.get(1L)).thenReturn(null);
        when(smartphoneRepository.findById(1L)).thenReturn(Optional.of(smartphone1));

        Optional<Smartphone> result = smartphoneService.getSmartphoneById(1L);
        assertTrue(result.isPresent());
        assertEquals(smartphone1, result.get());
        verify(smartphoneCache, times(1)).get(1L);
        verify(smartphoneRepository, times(1)).findById(1L);
        // После обращения к репозиторию мы ожидаем, что результат будет помещен в кеш
        verify(smartphoneCache, times(1)).put(eq(1L), any(Smartphone.class));
    }

    @Test
    void testSaveSmartphone() {
        // При сохранении нового смартфона
        when(smartphoneRepository.save(smartphone1)).thenReturn(smartphone1);

        Smartphone result = smartphoneService.saveSmartphone(smartphone1);
        assertNotNull(result);
        assertEquals(smartphone1.getId(), result.getId());
        verify(smartphoneRepository, times(1)).save(smartphone1);
        verify(smartphoneCache, times(1)).put(eq(smartphone1.getId()), eq(smartphone1));
    }

    @Test
    void testUpdateSmartphone() throws Exception {
        // Тест обновления – изменение цены, бренда и модели
        double newPrice = 849.99;
        Smartphone updatedSmartphone = new Smartphone("Apple", "iPhone 13 Pro", newPrice);
        // Устанавливаем тот же id для обновления
        java.lang.reflect.Field idField = Smartphone.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(updatedSmartphone, 1L);

        when(smartphoneRepository.findById(1L)).thenReturn(Optional.of(smartphone1));
        when(smartphoneRepository.save(any(Smartphone.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Smartphone result = smartphoneService.updateSmartphone(1L, updatedSmartphone);
        assertNotNull(result);
        assertEquals("Apple", result.getBrand());
        assertEquals("iPhone 13 Pro", result.getModel());
        assertEquals(newPrice, result.getPrice());
        verify(smartphoneRepository, times(1)).findById(1L);
        verify(smartphoneRepository, times(1)).save(any(Smartphone.class));
        verify(smartphoneCache, times(1)).put(eq(1L), eq(result));
    }

    @Test
    void testDeleteSmartphone() {
        // Подготовим ситуацию: заказ, содержащий смартфон, который мы хотим удалить
        // Здесь мы для упрощения проверяем только вызовы методов удаления кеша и репозитория
        when(smartphoneRepository.findById(1L)).thenReturn(Optional.of(smartphone1));
        when(orderRepository.findAll()).thenReturn(Collections.emptyList());
        doNothing().when(smartphoneRepository).flush();
        doNothing().when(smartphoneRepository).delete(smartphone1);
        when(smartphoneCache.remove(1L)).thenReturn(smartphone1);

        // Выполняем удаление
        assertDoesNotThrow(() -> smartphoneService.deleteSmartphone(1L));

        verify(smartphoneRepository, times(1)).flush();
        verify(smartphoneRepository, times(1)).delete(smartphone1);
        verify(smartphoneCache, times(1)).remove(1L);
    }

    @Test
    void testFilterSmartphones_Jpql() {
        // Проверяем фильтрацию через JPQL
        List<Smartphone> expectedList = Arrays.asList(smartphone1);
        when(smartphoneRepository.filterSmartphonesJpql("apple", null, null)).thenReturn(expectedList);

        List<Smartphone> result = smartphoneService.filterSmartphones("apple", null, null, false);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(smartphone1, result.get(0));
        verify(smartphoneRepository, times(1)).filterSmartphonesJpql("apple", null, null);
    }

    @Test
    void testFilterSmartphones_Native() {
        // Проверяем фильтрацию через нативный запрос
        List<Smartphone> expectedList = Arrays.asList(smartphone2);
        when(smartphoneRepository.filterSmartphonesNative("samsung", null, null)).thenReturn(expectedList);

        List<Smartphone> result = smartphoneService.filterSmartphones("samsung", null, null, true);
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(smartphone2, result.get(0));
        verify(smartphoneRepository, times(1)).filterSmartphonesNative("samsung", null, null);
    }
}
