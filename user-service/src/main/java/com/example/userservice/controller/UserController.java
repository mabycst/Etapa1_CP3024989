package com.example.userservice.controller;

import com.example.userservice.dto.CreateUserDto;
import com.example.userservice.dto.LoginUserDto;
import com.example.userservice.dto.RecoveryJwtTokenDto;
import com.example.userservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<Void> createUser(@Valid @RequestBody CreateUserDto createUserDto) {
        userService.createUser(createUserDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<RecoveryJwtTokenDto> login(@Valid @RequestBody LoginUserDto loginUserDto) {
        RecoveryJwtTokenDto tokenDto = userService.authenticateUser(loginUserDto);
        return ResponseEntity.ok(tokenDto);
    }

    @GetMapping("/test/customer")
    public ResponseEntity<String> testCustomer() {
        return ResponseEntity.ok("Acesso autorizado para CUSTOMER!");
    }
}
