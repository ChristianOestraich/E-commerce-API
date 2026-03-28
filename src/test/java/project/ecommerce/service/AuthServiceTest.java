package project.ecommerce.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import project.ecommerce.dto.AuthResponse;
import project.ecommerce.dto.LoginRequest;
import project.ecommerce.dto.RegisterRequest;
import project.ecommerce.entity.RefreshToken;
import project.ecommerce.entity.User;
import project.ecommerce.entity.enums.Role;
import project.ecommerce.repository.UserRepository;
import project.ecommerce.security.JwtUtil;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;
    @Mock private RefreshTokenService refreshTokenService;
    @InjectMocks private AuthService authService;
    @Mock private EmailService emailService;

    private User user;
    private RefreshToken refreshToken;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .name("Christian")
                .email("christian@email.com")
                .password("encoded_password")
                .role(Role.CUSTOMER)
                .active(true)
                .build();

        refreshToken = RefreshToken.builder()
                .id(1L)
                .user(user)
                .token("refresh-token-uuid")
                .revoked(false)
                .build();
    }

    @Test
    void register_ShouldReturnAuthResponse_WhenEmailNotExists() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Christian");
        request.setEmail("christian@email.com");
        request.setPassword("123456");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtUtil.generateToken(anyString(), anyString())).thenReturn("access-token");
        when(refreshTokenService.createRefreshToken(any(User.class))).thenReturn(refreshToken);

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token-uuid", response.getRefreshToken());
        assertEquals("CUSTOMER", response.getRole());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_ShouldThrowException_WhenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("christian@email.com");

        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.register(request));

        assertEquals("Email ja cadastrado.", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_ShouldReturnAuthResponse_WhenCredentialsAreValid() {
        LoginRequest request = new LoginRequest();
        request.setEmail("christian@email.com");
        request.setPassword("123456");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtUtil.generateToken(anyString(), anyString())).thenReturn("access-token");
        when(refreshTokenService.createRefreshToken(any(User.class))).thenReturn(refreshToken);

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());
        assertEquals("CUSTOMER", response.getRole());
    }

    @Test
    void login_ShouldThrowException_WhenUserNotFound() {
        LoginRequest request = new LoginRequest();
        request.setEmail("naoexiste@email.com");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.login(request));

        assertEquals("Usuario nao encontrado.", ex.getMessage());
    }

    @Test
    void login_ShouldThrowException_WhenUserIsInactive() {
        user.setActive(false);
        LoginRequest request = new LoginRequest();
        request.setEmail("christian@email.com");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.login(request));

        assertEquals("Usuario inativo.", ex.getMessage());
    }

    @Test
    void login_ShouldThrowException_WhenPasswordIsWrong() {
        LoginRequest request = new LoginRequest();
        request.setEmail("christian@email.com");
        request.setPassword("senha_errada");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.login(request));

        assertEquals("Senha incorreta.", ex.getMessage());
    }
}