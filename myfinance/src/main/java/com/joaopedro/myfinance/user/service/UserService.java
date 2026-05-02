package com.joaopedro.myfinance.user.service;

import com.joaopedro.myfinance.user.exception.EmailAlreadyInUseException;
import com.joaopedro.myfinance.user.repository.UserRepository;
import com.joaopedro.myfinance.user.domain.Role;
import com.joaopedro.myfinance.user.domain.User;
import com.joaopedro.myfinance.user.dto.CreateUserRequest;
import com.joaopedro.myfinance.user.dto.UserResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse createUser(CreateUserRequest dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new EmailAlreadyInUseException();
        }

        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(Role.USER);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);

        UserResponse responseDTO = new UserResponse();
        responseDTO.setId(savedUser.getId());
        responseDTO.setNome(savedUser.getName());
        responseDTO.setEmail(savedUser.getEmail());
        responseDTO.setRole(savedUser.getRole());

        return responseDTO;
    }
}
