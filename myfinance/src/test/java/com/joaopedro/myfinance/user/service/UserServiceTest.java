package com.joaopedro.myfinance.user.service;

import com.joaopedro.myfinance.user.domain.Role;
import com.joaopedro.myfinance.user.domain.User;
import com.joaopedro.myfinance.user.dto.CreateUserRequest;
import com.joaopedro.myfinance.user.dto.UserResponse;
import com.joaopedro.myfinance.user.exception.EmailAlreadyInUseException;
import com.joaopedro.myfinance.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private CreateUserRequest buildRequest(String name, String email, String password) {
        var request = new CreateUserRequest();
        request.setName(name);
        request.setEmail(email);
        request.setPassword(password);
        return request;
    }

    @Test
    void shouldCreateUserWhenDataIsValid() {
        var request = buildRequest("Carlos Silva", "carlos@teste.com", "senha123");

        when(userRepository.existsByEmail("carlos@teste.com")).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("hashed_password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(1L);
            return u;
        });

        UserResponse result = userService.createUser(request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Carlos Silva", result.getNome());
        assertEquals("carlos@teste.com", result.getEmail());
        assertEquals(Role.USER.name(), result.getRole());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

        verify(userRepository).existsByEmail("carlos@teste.com");
        verify(passwordEncoder).encode("senha123");
        verify(userRepository).save(captor.capture());

        User savedUser = captor.getValue();
        assertEquals("hashed_password", savedUser.getPassword());
        assertEquals(Role.USER.name(), savedUser.getRole());
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyInUse() {
        var request = buildRequest("Carlos Silva", "carlos@teste.com", "senha123");

        when(userRepository.existsByEmail("carlos@teste.com")).thenReturn(true);

        assertThrows(EmailAlreadyInUseException.class, () -> userService.createUser(request));

        verify(userRepository).existsByEmail("carlos@teste.com");
        verifyNoMoreInteractions(userRepository);
        verifyNoInteractions(passwordEncoder);
    }
}
