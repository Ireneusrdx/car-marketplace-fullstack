package com.automarket.marketplace.auth;

import com.automarket.marketplace.auth.dto.AuthResponse;
import com.automarket.marketplace.auth.dto.EmailLoginRequest;
import com.automarket.marketplace.auth.dto.EmailRegisterRequest;
import com.automarket.marketplace.security.JwtService;
import com.automarket.marketplace.user.User;
import com.automarket.marketplace.user.UserRepository;
import com.automarket.marketplace.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerIntegrationTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private TokenHasher tokenHasher;

    @InjectMocks
    private AuthService authService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("auth_test@automarket.dev");
        user.setFullName("Auth Tester");
        user.setRole(UserRole.BUYER);
        user.setActive(true);
        user.setVerifiedSeller(false);
    }

    @Test
    void registerShouldCreateUserAndIssueTokens() {
        when(userRepository.existsByEmailIgnoreCase("auth_test@automarket.dev")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateAccessToken(any(User.class))).thenReturn("access-token");
        when(jwtService.refreshTokenExpirationDays()).thenReturn(7L);
        when(jwtService.accessTokenExpiresInSeconds()).thenReturn(1800L);
        when(tokenHasher.sha256(any(String.class))).thenReturn("refresh-token-hash");

        AuthResponse response = authService.registerEmail(
            new EmailRegisterRequest("auth_test@automarket.dev", "Password123!", "Auth Tester", null),
            "JUnit",
            "127.0.0.1"
        );

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isNotBlank();
        assertThat(response.user().email()).isEqualTo("auth_test@automarket.dev");

        verify(userRepository).save(any(User.class));
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void loginShouldFailWithInvalidCredentials() {
        when(userRepository.findByEmailIgnoreCase("auth_test@automarket.dev")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.loginEmail(
            new EmailLoginRequest("auth_test@automarket.dev", "wrong"),
            "JUnit",
            "127.0.0.1"
        )).isInstanceOf(AuthException.class)
          .hasMessage("Invalid credentials");

        verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    }

    @Test
    void refreshShouldRotateTokenAndRevokeOldOne() {
        RefreshToken existing = new RefreshToken();
        existing.setUser(user);
        existing.setTokenHash("old-hash");
        existing.setExpiresAt(LocalDateTime.now().plusDays(1));

        when(tokenHasher.sha256("old-refresh")).thenReturn("old-hash");
        when(tokenHasher.sha256(any(String.class))).thenReturn("new-hash");
        when(refreshTokenRepository.findByTokenHashAndRevokedAtIsNull("old-hash")).thenReturn(Optional.of(existing));
        when(jwtService.generateAccessToken(user)).thenReturn("new-access-token");
        when(jwtService.refreshTokenExpirationDays()).thenReturn(7L);
        when(jwtService.accessTokenExpiresInSeconds()).thenReturn(1800L);

        AuthResponse response = authService.refresh("old-refresh", "JUnit", "127.0.0.1");

        assertThat(response.accessToken()).isEqualTo("new-access-token");
        assertThat(response.refreshToken()).isNotBlank();
        assertThat(existing.getRevokedAt()).isNotNull();

        ArgumentCaptor<RefreshToken> tokenCaptor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(tokenCaptor.capture());
        assertThat(tokenCaptor.getAllValues()).isNotEmpty();
    }
}
