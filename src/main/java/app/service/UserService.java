package app.service;

import app.cache.LruCache;
import app.dao.UserRepository;
import app.models.Order;
import app.models.User;
import java.time.LocalDate;
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
    private final LruCache<Long, User> userCache;

    @Autowired
    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder, LruCache<Long, User> userCache) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userCache = userCache;
    }

    public List<User> getAllUsers() {
        long dbCount = userRepository.count();
        return userRepository.findAll();
    }


    public Optional<User> getUserById(Long id) {
        User cachedUser = userCache.get(id);
        if (cachedUser != null) {
            return Optional.of(cachedUser);
        }
        Optional<User> userOpt = userRepository.findById(id);
        userOpt.ifPresent(user -> userCache.put(id, user));
        return userOpt;
    }

    public Optional<User> getUserDetails(Long id) {
        return userRepository.findWithOrdersById(id)
                .map(user -> {
                    userCache.put(user.getId(), user);
                    return user;
                });
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
            userCache.put(savedUser.getId(), savedUser);
            return savedUser;
        }).orElse(null);
    }

    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
        userCache.remove(id);
    }

    public List<User> getUsersByOrderAndPhoneCriteria(Double minTotal, String phoneBrand,
                                                      LocalDate date, boolean nativeQuery) {
        if (nativeQuery) {
            List<User> users = userRepository
                    .findUsersByOrderAndPhoneCriteriaNative(minTotal, phoneBrand, date);
            users.forEach(u -> userCache.put(u.getId(), u));
            return users;
        } else {
            List<User> users =  userRepository
                    .findUsersByOrderAndPhoneCriteriaJpql(minTotal, phoneBrand, date);
            users.forEach(u -> userCache.put(u.getId(), u));
            return users;
        }
    }
}

