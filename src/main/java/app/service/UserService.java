package app.service;

import app.dao.SmartphoneRepository;
import app.models.Smartphone;
import app.models.User;
import app.dao.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private SmartphoneRepository smartphoneRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public User updateSmartphones(Long userId, List<Smartphone> smartphones) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Загрузка смартфонов по ID (чтобы избежать проблем с detach-объектами)
        List<Smartphone> smartphoneEntities = smartphones.stream()
                .map(s -> smartphoneRepository.findById(s.getId())
                        .orElseThrow(() -> new RuntimeException("Smartphone not found")))
                .toList();

        user.setSmartphones(smartphoneEntities);
        return userRepository.save(user);
    }
}
