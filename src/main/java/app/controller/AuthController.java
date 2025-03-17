package app.controller;

import app.models.User;
import app.entities.Role;
import app.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService appUserService;

    @Autowired
    public AuthController(UserService appUserService) {
        this.appUserService = appUserService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody User user) {
        user.setRole(Role.USER);
        appUserService.saveUser(user);
        return ResponseEntity.ok("User registered successfully");
    }
}
