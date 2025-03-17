package app.service;

import app.dao.SmartphoneRepository;
import app.models.Smartphone;
import app.models.User;
import app.dao.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    private SmartphoneRepository smartphoneRepository;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public User saveUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

//    public User updateSmartphones(Long userId, List<Smartphone> smartphones) {
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        // Загрузка смартфонов по ID (чтобы избежать проблем с detach-объектами)
//        List<Smartphone> smartphoneEntities = smartphones.stream()
//                .map(s -> smartphoneRepository.findById(s.getId())
//                        .orElseThrow(() -> new RuntimeException("Smartphone not found")))
//                .toList();
//
//        user.setSmartphones(smartphoneEntities);
//        return userRepository.save(user);
//    }

    public User updateSmartphones(Long userId, List<app.models.Smartphone> smartphones) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Загрузка смартфонов по ID (чтобы избежать проблем с detach-объектами)
        List<app.models.Smartphone> smartphoneEntities = smartphones.stream()
                .map(s -> (Smartphone) SmartphoneRepository.class.cast(null) /* логика загрузки по ID */)
                .toList();

        user.setSmartphones(smartphoneEntities);
        return userRepository.save(user);
    }
}
