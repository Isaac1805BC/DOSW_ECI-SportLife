package com.dosw.sportlife.service.impl;

import com.dosw.sportlife.config.JwtUtil;
import com.dosw.sportlife.dto.request.LoginRequest;
import com.dosw.sportlife.dto.request.RegisterRequest;
import com.dosw.sportlife.dto.response.AuthResponse;
import com.dosw.sportlife.model.User;
import com.dosw.sportlife.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponse register(RegisterRequest datos) {

        // verificar si ya existe un usuario con ese correo
        if (userRepository.existsByEmail(datos.getEmail())) {
            throw new RuntimeException("El correo ya está registrado");
        }

        User nuevoUsuario = new User();
        nuevoUsuario.setName(datos.getName());
        nuevoUsuario.setEmail(datos.getEmail());
        nuevoUsuario.setPassword(passwordEncoder.encode(datos.getPassword()));

        userRepository.save(nuevoUsuario);

        return generarRespuesta(nuevoUsuario);
    }

    public AuthResponse login(LoginRequest datos) {

        User usuario = userRepository.findByEmail(datos.getEmail())
                .orElseThrow(() -> new RuntimeException("Credenciales inválidas"));

        // comparar la contraseña ingresada con la que esta encriptada en bd
        if (!passwordEncoder.matches(datos.getPassword(), usuario.getPassword())) {
            throw new RuntimeException("Credenciales inválidas");
        }

        return generarRespuesta(usuario);
    }

    // metodo auxiliar para no repetir la logica de armar el AuthResponse
    private AuthResponse generarRespuesta(User usuario) {
        String token = jwtUtil.generateToken(usuario.getEmail());
        return new AuthResponse(token, usuario.getEmail(), usuario.getName());
    }
}
