package com.example.userservice.controller;

import com.example.userservice.dto.RecoveryJwtTokenDto;
import com.example.userservice.dto.RequestCodeDto;
import com.example.userservice.dto.VerifyCodeDto;
import com.example.userservice.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/request-code")
    public ResponseEntity<Void> requestCode(@Valid @RequestBody RequestCodeDto requestCodeDto) {
        authService.requestCode(requestCodeDto.email());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/verify-code")
    public ResponseEntity<RecoveryJwtTokenDto> verifyCode(@Valid @RequestBody VerifyCodeDto verifyCodeDto) {
        String token = authService.verifyCode(verifyCodeDto.email(), verifyCodeDto.code());
        if (token != null) {
            return ResponseEntity.ok(new RecoveryJwtTokenDto(token));
        } else {
            return ResponseEntity.badRequest().build();
        }
    }
}
