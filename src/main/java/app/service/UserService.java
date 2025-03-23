package app.service;

import app.cache.InMemoryCache;
import app.dao.UserRepository;
import app.models.Order;
import app.models.User;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final InMemoryCache<Long, User> userCache;

    @Autowired
    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       InMemoryCache<Long, User> orderCache) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userCache = orderCache;
    }

    public List<User> getAllUsers() {
        long dbCount = userRepository.count();
        int cacheSize = userCache.size();

        if (cacheSize == dbCount && cacheSize > 0) {
            return userCache.getAllValues();
        } else {
            List<User> users = userRepository.findAll();
            users.forEach(u -> userCache.put(u.getId(), u));
            return users;
        }
    }

    public Optional<User> getUserById(Long id) {
        User cachedUser = userCache.get(id);
        if (cachedUser != null) {
            return Optional.of(cachedUser);
        }
        Optional<User> user = userRepository.findById(id);
        user.ifPresent(u -> userCache.put(u.getId(), u));
        return user;
    }

    public Optional<User> getUserDetails(Long id) {
        return userRepository.findWithOrdersById(id);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Transactional
    public User saveUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);
        userCache.put(savedUser.getId(), savedUser);
        return savedUser;
    }

    @Transactional
    public User updateUser(Long id, User updatedUser) {
        return userRepository.findById(id).map(existingUser -> {
            existingUser.setUsername(updatedUser.getUsername());

            if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
                existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
            }

            User savedUser = userRepository.save(existingUser);
            userCache.update(savedUser.getId(), savedUser);
            return savedUser;
        }).orElse(null);
    }

    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
        userCache.remove(id);
    }
}

