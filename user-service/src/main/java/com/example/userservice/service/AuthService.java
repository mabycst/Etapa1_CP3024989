package com.example.userservice.service;

import com.example.userservice.domain.Role;
import com.example.userservice.domain.RoleName;
import com.example.userservice.domain.User;
import com.example.userservice.dto.EmailDto;
import com.example.userservice.producer.UserProducer;
import com.example.userservice.repository.RoleRepository;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.security.JwtTokenService;
import com.example.userservice.security.UserDetailsImpl;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final CodigoCacheService codigoCacheService;
    private final UserProducer userProducer;
    private final JwtTokenService jwtTokenService;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder, CodigoCacheService codigoCacheService,
                       UserProducer userProducer, JwtTokenService jwtTokenService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.codigoCacheService = codigoCacheService;
        this.userProducer = userProducer;
        this.jwtTokenService = jwtTokenService;
    }

    public void requestCode(String email) {
        // 1. Gera código aleatório de 6 dígitos
        String code = String.format("%06d", new Random().nextInt(1000000));

        // 2. Armazena no cache (expira em 5 minutos)
        codigoCacheService.save(email, code);

        // 3. Verifica se o usuário existe, senão cria um usuário temporário
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            Role role = roleRepository.findByName(RoleName.ROLE_CUSTOMER)
                    .orElseGet(() -> roleRepository.save(new Role(RoleName.ROLE_CUSTOMER)));
            
            // Cria usuário temporário sem nome, role ROLE_CUSTOMER e senha aleatória criptografada
            User tempUser = new User(
                    email,
                    passwordEncoder.encode(UUID.randomUUID().toString()),
                    List.of(role)
            );
            return userRepository.save(tempUser);
        });

        // 4. Monta EmailDto (com assunto "Seu código de acesso" e o código no corpo)
        // Como o ID do usuário no banco é Long, geramos um UUID determinístico a partir dele para o DTO
        UUID userIdUuid = UUID.nameUUIDFromBytes(String.valueOf(user.getId()).getBytes());
        EmailDto emailDto = new EmailDto(
                email,
                "Seu código de acesso",
                "Olá! Seu código de acesso temporário é: " + code,
                userIdUuid
        );

        // 5. Publica na fila do RabbitMQ
        userProducer.publishEmailMessage(emailDto);
    }

    public String verifyCode(String email, String code) {
        String cachedCode = codigoCacheService.get(email);
        if (cachedCode != null && cachedCode.equals(code)) {
            codigoCacheService.delete(email); // Consome o código após validação
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado com e-mail: " + email));
            UserDetailsImpl userDetails = new UserDetailsImpl(user);
            return jwtTokenService.generateToken(userDetails);
        }
        return null;
    }
}
