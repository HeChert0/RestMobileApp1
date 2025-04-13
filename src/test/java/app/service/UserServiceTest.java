
package app.service;

import app.cache.LruCache;
import app.dao.UserRepository;
import app.exception.UserNotFoundException;
import app.models.Order;
import app.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private LruCache<Long, User> userCache;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private List<User> testUsers;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");
        testUser.setPassword("password");

        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");
        user2.setPassword("password2");

        testUsers = Arrays.asList(testUser, user2);
    }

    @Test
    void getAllUsers_shouldReturnAllUsers() {
        // Given
        when(userRepository.findAll()).thenReturn(testUsers);
        when(userRepository.count()).thenReturn(2L);

        // When
        List<User> result = userService.getAllUsers();

        // Then
        assertEquals(testUsers, result);
        verify(userRepository).count();
        verify(userRepository).findAll();
    }

    @Test
    void getUserById_withCachedUser_shouldReturnUserFromCache() {
        // Given
        when(userCache.get(1L)).thenReturn(testUser);

        // When
        Optional<User> result = userService.getUserById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testUser, result.get());
        verify(userCache).get(1L);
        verifyNoInteractions(userRepository);
    }

    @Test
    void getUserById_withNonCachedUser_shouldFetchFromRepositoryAndUpdateCache() {
        // Given
        when(userCache.get(1L)).thenReturn(null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = userService.getUserById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testUser, result.get());
        verify(userCache).get(1L);
        verify(userRepository).findById(1L);
        verify(userCache).put(1L, testUser);
    }

    @Test
    void getUserById_withNonExistentUser_shouldReturnEmpty() {
        // Given
        when(userCache.get(999L)).thenReturn(null);
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.getUserById(999L);

        // Then
        assertFalse(result.isPresent());
        verify(userCache).get(999L);
        verify(userRepository).findById(999L);
        verifyNoMoreInteractions(userCache);
    }

    @Test
    void getUserDetails_withExistingUser_shouldReturnUserWithOrders() {
        // Given
        when(userRepository.findWithOrdersById(1L)).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = userService.getUserDetails(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testUser, result.get());
        verify(userRepository).findWithOrdersById(1L);
        verify(userCache).put(1L, testUser);
    }

    @Test
    void getUserDetails_withNonExistentUser_shouldReturnEmpty() {
        // Given
        when(userRepository.findWithOrdersById(999L)).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.getUserDetails(999L);

        // Then
        assertFalse(result.isPresent());
        verify(userRepository).findWithOrdersById(999L);
    }

    @Test
    void saveUser_shouldSuccessfullyCreateUser() {
        // Given
        User newUser = new User();
        newUser.setUsername("newUser");
        newUser.setPassword("rawPassword");

        when(passwordEncoder.encode("rawPassword")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(3L);
            return savedUser;
        });

        // When
        User result = userService.saveUser(newUser);

        // Then
        assertEquals(3L, result.getId());
        assertEquals("newUser", result.getUsername());
        assertEquals("encodedPassword", result.getPassword());

        verify(passwordEncoder).encode("rawPassword");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User capturedUser = userCaptor.getValue();
        assertEquals("newUser", capturedUser.getUsername());
        assertEquals("encodedPassword", capturedUser.getPassword());

        verify(userCache).put(3L, result);
    }

    @Test
    void updateUser_withExistingUser_shouldUpdateUser() {
        // Given
        User updatedUser = new User();
        updatedUser.setUsername("updatedUsername");
        updatedUser.setPassword("newPassword");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = userService.updateUser(1L, updatedUser);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("updatedUsername", result.getUsername());
        assertEquals("encodedNewPassword", result.getPassword());

        verify(userRepository).findById(1L);
        verify(passwordEncoder).encode("newPassword");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User capturedUser = userCaptor.getValue();
        assertEquals(1L, capturedUser.getId());
        assertEquals("updatedUsername", capturedUser.getUsername());
        assertEquals("encodedNewPassword", capturedUser.getPassword());

        verify(userCache).put(1L, testUser);
    }

    @Test
    void updateUser_withEmptyPassword_shouldNotUpdatePassword() {
        // Given
        User updatedUser = new User();
        updatedUser.setUsername("updatedUsername");
        updatedUser.setPassword(""); // пустой пароль

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = userService.updateUser(1L, updatedUser);

        // Then
        assertNotNull(result);
        assertEquals("updatedUsername", result.getUsername());
        assertEquals("password", result.getPassword()); // пароль не должен быть изменен

        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
        verifyNoInteractions(passwordEncoder); // не должно быть вызовов кодировщика
    }

    @Test
    void updateUser_withNonExistentUser_shouldReturnNull() {
        // Given
        User updatedUser = new User();
        updatedUser.setUsername("updatedUsername");

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When
        User result = userService.updateUser(999L, updatedUser);

        // Then
        assertNull(result);
        verify(userRepository).findById(999L);
        verifyNoMoreInteractions(passwordEncoder, userRepository, userCache);
    }

    @Test
    void deleteUser_withExistingUser_shouldDeleteSuccessfully() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).delete(testUser);

        // When & Then
        assertDoesNotThrow(() -> userService.deleteUser(1L));

        verify(userRepository).findById(1L);
        verify(userRepository).delete(testUser);
        verify(userCache).remove(1L);
    }

    @Test
    void deleteUser_withNonExistentUser_shouldThrowException() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userService.deleteUser(999L)
        );

        assertEquals("User with id 999 not found", exception.getMessage());
        verify(userRepository).findById(999L);
        verifyNoInteractions(userCache);
    }

    @Test
    void loadUserByUsername_withExistingUsername_shouldReturnUserDetails() {
        // Given
        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(testUser));

        // When
        UserDetails result = userService.loadUserByUsername("testUser");

        // Then
        assertNotNull(result);
        assertEquals("testUser", result.getUsername());
        assertEquals("password", result.getPassword());
        verify(userRepository).findByUsername("testUser");
    }

    @Test
    void loadUserByUsername_withNonExistentUsername_shouldThrowException() {
        // Given
        when(userRepository.findByUsername("nonExistent")).thenReturn(Optional.empty());

        // When & Then
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> userService.loadUserByUsername("nonExistent")
        );

        assertEquals("User not found", exception.getMessage());
        verify(userRepository).findByUsername("nonExistent");
    }

    @Test
    void getUsersByOrderAndPhoneCriteria_withJpqlQuery_shouldReturnMatchingUsers() {
        // Given
        LocalDate orderDate = LocalDate.of(2023, 6, 1);
        Double minTotal = 1000.0;
        String phoneBrand = "Apple";
        boolean nativeQuery = false;

        when(userRepository.findUsersByOrderAndPhoneCriteriaJpql(minTotal, phoneBrand, orderDate))
                .thenReturn(testUsers);

        // When
        List<User> result = userService.getUsersByOrderAndPhoneCriteria(minTotal, phoneBrand, orderDate, nativeQuery);

        // Then
        assertEquals(2, result.size());
        assertEquals(testUsers, result);

        verify(userRepository).findUsersByOrderAndPhoneCriteriaJpql(minTotal, phoneBrand, orderDate);
        verify(userCache).put(1L, testUser);
        verify(userCache).put(2L, testUsers.get(1));
    }

    @Test
    void getUsersByOrderAndPhoneCriteria_withNativeQuery_shouldReturnMatchingUsers() {
        // Given
        LocalDate orderDate = LocalDate.of(2023, 6, 1);
        Double minTotal = 1000.0;
        String phoneBrand = "Apple";
        boolean nativeQuery = true;

        when(userRepository.findUsersByOrderAndPhoneCriteriaNative(minTotal, phoneBrand, orderDate))
                .thenReturn(testUsers);

        // When
        List<User> result = userService.getUsersByOrderAndPhoneCriteria(minTotal, phoneBrand, orderDate, nativeQuery);

        // Then
        assertEquals(2, result.size());
        assertEquals(testUsers, result);

        verify(userRepository).findUsersByOrderAndPhoneCriteriaNative(minTotal, phoneBrand, orderDate);
        verify(userCache).put(1L, testUser);
        verify(userCache).put(2L, testUsers.get(1));
    }
}