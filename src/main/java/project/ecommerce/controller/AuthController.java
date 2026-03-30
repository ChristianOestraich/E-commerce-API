package project.ecommerce.controller;

import project.ecommerce.dto.*;
import project.ecommerce.service.AuthService;
import project.ecommerce.service.RefreshTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;

    // Adicione os endpoints
    @PutMapping("/profile")
    public ResponseEntity<Void> updateProfile(
            Authentication auth,
            @RequestBody Map<String, String> body) {
        authService.updateProfile(auth.getName(), body.get("name"));
        return ResponseEntity.ok().build();
    }

    @PutMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            Authentication auth,
            @RequestBody Map<String, String> body) {
        authService.changePassword(
                auth.getName(),
                body.get("currentPassword"),
                body.get("newPassword")
        );
        return ResponseEntity.ok().build();
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        String newAccessToken = refreshTokenService.refreshAccessToken(request.getRefreshToken());
        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        refreshTokenService.revokeRefreshToken(request.getRefreshToken());
        return ResponseEntity.noContent().build();
    }
}
