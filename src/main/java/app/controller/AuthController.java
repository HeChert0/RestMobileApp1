package app.controller;

import app.entities.Role;
import app.models.User;
import app.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


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
