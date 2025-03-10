package app.controller;

import app.dto.UserDTO;
import app.models.User;
import app.models.Smartphone;
import app.service.UserService;
import app.service.SmartphoneService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final SmartphoneService smartphoneService;

    @Autowired
    public UserController(UserService userService, SmartphoneService smartphoneService) {
        this.userService = userService;
        this.smartphoneService = smartphoneService;
    }

    @GetMapping
    public List<UserDTO> getAllUsers() {
        return userService.getAllUsers().stream().map(this::convertToDTO).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        Optional<User> user = userService.getUserById(id);
        return user.map(value -> ResponseEntity.ok(convertToDTO(value)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public UserDTO createUser(@Valid @RequestBody UserDTO userDTO) {
        User user = new User(userDTO.getName());
        User savedUser = userService.saveUser(user);
        return convertToDTO(savedUser);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUserSmartphones(@PathVariable Long id, @RequestBody UserDTO userDTO) {
        List<Smartphone> smartphones = userDTO.getSmartphoneIds().stream()
                .map(smartphoneService::getSmartphoneById)
                .flatMap(Optional::stream)
                .toList();

        User updatedUser = userService.updateSmartphones(id, smartphones);
        return ResponseEntity.ok(convertToDTO(updatedUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (userService.getUserById(id).isPresent()) {
            userService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    private UserDTO convertToDTO(User user) {
        List<Long> smartphoneIds = (user.getSmartphones() == null) ?
                new ArrayList<>() :
                user.getSmartphones().stream().map(Smartphone::getId).toList();

        return new UserDTO(user.getId(), user.getUsername(), smartphoneIds);
    }

}
