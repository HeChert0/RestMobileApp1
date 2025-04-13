package app.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import app.cache.LruCache;
import app.dao.UserRepository;
import app.exception.UserNotFoundException;
import app.models.User;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private LruCache<Long, User> userCache;

    @InjectMocks
    private UserService userService;

    private User user;
    private final Long userId = 1L;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(userId);
        user.setUsername("JohnDoe");
        user.setPassword("rawPassword");
    }

    @Test
    void saveUser_Success() {
        String encodedPassword = "encodedPass";
        when(passwordEncoder.encode("rawPassword")).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(userId);
            return u;
        });

        User savedUser = userService.saveUser(user);
        assertNotNull(savedUser);
        assertEquals(userId, savedUser.getId());
        assertEquals("JohnDoe", savedUser.getUsername());
        assertEquals(encodedPassword, savedUser.getPassword());
        verify(userRepository, times(1)).save(user);
        verify(userCache, times(1)).put(userId, savedUser);
    }

    @Test
    void getUserById_CacheHit() {
        when(userCache.get(userId)).thenReturn(user);
        Optional<User> result = userService.getUserById(userId);
        assertTrue(result.isPresent());
        assertEquals(userId, result.get().getId());
        verify(userCache, times(1)).get(userId);
        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    void getUserById_CacheMiss() {
        when(userCache.get(userId)).thenReturn(null);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        Optional<User> result = userService.getUserById(userId);
        assertTrue(result.isPresent());
        assertEquals(userId, result.get().getId());
        verify(userCache, times(1)).get(userId);
        verify(userRepository, times(1)).findById(userId);
        verify(userCache, times(1)).put(userId, user);
    }

    @Test
    void updateUser_Success() {
        User updatedUser = new User();
        updatedUser.setUsername("JaneDoe");
        updatedUser.setPassword("newPass");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPass")).thenReturn("encodedNewPass");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.updateUser(userId, updatedUser);
        assertNotNull(result);
        assertEquals("JaneDoe", result.getUsername());
        assertEquals("encodedNewPass", result.getPassword());
        verify(userRepository, times(1)).save(any(User.class));
        verify(userCache, times(1)).put(userId, result);
    }

    @Test
    void deleteUser_Success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        userService.deleteUser(userId);
        verify(userRepository, times(1)).delete(user);
        verify(userCache, times(1)).remove(userId);
    }

    @Test
    void loadUserByUsername_Success() {
        when(userRepository.findByUsername("JohnDoe")).thenReturn(Optional.of(user));
        User result = (User) userService.loadUserByUsername("JohnDoe");
        assertNotNull(result);
        assertEquals("JohnDoe", result.getUsername());
    }

    @Test
    void loadUserByUsername_NotFound() {
        when(userRepository.findByUsername("NonExistent")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () ->
                userService.loadUserByUsername("NonExistent")
        );
    }
}
