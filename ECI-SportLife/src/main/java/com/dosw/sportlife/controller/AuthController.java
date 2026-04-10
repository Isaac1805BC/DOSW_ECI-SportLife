package com.dosw.sportlife.controller;

import com.dosw.sportlife.dto.request.LoginRequest;
import com.dosw.sportlife.dto.request.RegisterRequest;
import com.dosw.sportlife.dto.response.AuthResponse;
import com.dosw.sportlife.service.impl.AuthServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthServiceImpl authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registrarUsuario(@Valid @RequestBody RegisterRequest datos) {
        AuthResponse respuesta = authService.register(datos);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> iniciarSesion(@Valid @RequestBody LoginRequest datos) {
        return ResponseEntity.ok(authService.login(datos));
    }
}
