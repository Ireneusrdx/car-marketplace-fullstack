package com.automarket.marketplace.auth;

import com.automarket.marketplace.auth.dto.AuthResponse;
import com.automarket.marketplace.auth.dto.AuthUserDto;
import com.automarket.marketplace.auth.dto.EmailLoginRequest;
import com.automarket.marketplace.auth.dto.EmailRegisterRequest;
import com.automarket.marketplace.auth.dto.MeResponse;
import com.automarket.marketplace.security.JwtService;
import com.automarket.marketplace.security.UserPrincipal;
import com.automarket.marketplace.user.User;
import com.automarket.marketplace.user.UserRepository;
import com.automarket.marketplace.user.UserRole;
import com.google.firebase.auth.FirebaseToken;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final TokenHasher tokenHasher;

    @Transactional
    public AuthResponse registerEmail(EmailRegisterRequest request, String userAgent, String ipAddress) {
        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new AuthException(HttpStatus.CONFLICT, "Email is already in use");
        }

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail(request.email().trim().toLowerCase());
        user.setPhone(request.phone());
        user.setFullName(request.fullName());
        user.setAuthProvider("EMAIL");
        user.setEmailVerified(false);
        user.setRole(UserRole.BUYER);
        user.setActive(true);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setLastLoginAt(LocalDateTime.now());

        User saved = userRepository.save(user);
        return issueTokens(saved, userAgent, ipAddress);
    }

    @Transactional
    public AuthResponse loginEmail(EmailLoginRequest request, String userAgent, String ipAddress) {
        String normalizedEmail = request.email().trim().toLowerCase();

        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(normalizedEmail, request.password())
            );
        } catch (AuthenticationException ex) {
            throw new AuthException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
            .orElseThrow(() -> new AuthException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!user.isActive()) {
            throw new AuthException(HttpStatus.FORBIDDEN, "Account is deactivated");
        }

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
        return issueTokens(user, userAgent, ipAddress);
    }

    @Transactional
    public AuthResponse firebaseLogin(FirebaseToken firebaseToken, String userAgent, String ipAddress) {
        String email = firebaseToken.getEmail();
        if (email == null || email.isBlank()) {
            throw new AuthException(HttpStatus.BAD_REQUEST, "Firebase token does not contain a valid email");
        }

        User user = userRepository.findByEmailIgnoreCase(email)
            .orElseGet(() -> {
                User created = new User();
                created.setId(UUID.randomUUID());
                created.setEmail(email.toLowerCase());
                created.setFullName(firebaseToken.getName() == null ? "AutoMarket User" : firebaseToken.getName());
                created.setAvatarUrl(firebaseToken.getPicture());
                created.setAuthProvider("FIREBASE");
                created.setProviderId(firebaseToken.getUid());
                created.setEmailVerified(Boolean.TRUE.equals(firebaseToken.isEmailVerified()));
                created.setRole(UserRole.BUYER);
                created.setActive(true);
                created.setLastLoginAt(LocalDateTime.now());
                return userRepository.save(created);
            });

        user.setProviderId(firebaseToken.getUid());
        user.setEmailVerified(Boolean.TRUE.equals(firebaseToken.isEmailVerified()));
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        return issueTokens(user, userAgent, ipAddress);
    }

    @Transactional
    public AuthResponse refresh(String rawRefreshToken, String userAgent, String ipAddress) {
        RefreshToken token = refreshTokenRepository.findByTokenHashAndRevokedAtIsNull(tokenHasher.sha256(rawRefreshToken))
            .orElseThrow(() -> new AuthException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

        if (token.isExpired()) {
            token.setRevokedAt(LocalDateTime.now());
            refreshTokenRepository.save(token);
            throw new AuthException(HttpStatus.UNAUTHORIZED, "Refresh token expired");
        }

        token.setRevokedAt(LocalDateTime.now());
        refreshTokenRepository.save(token);

        return issueTokens(token.getUser(), userAgent, ipAddress);
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        refreshTokenRepository.findByTokenHashAndRevokedAtIsNull(tokenHasher.sha256(rawRefreshToken))
            .ifPresent(token -> {
                token.setRevokedAt(LocalDateTime.now());
                refreshTokenRepository.save(token);
            });
    }

    @Transactional(readOnly = true)
    public MeResponse me(UserPrincipal principal) {
        User user = userRepository.findById(principal.getId())
            .orElseThrow(() -> new AuthException(HttpStatus.NOT_FOUND, "User not found"));

        return new MeResponse(
            user.getId(),
            user.getEmail(),
            user.getFullName(),
            user.getPhone(),
            user.getRole().name(),
            user.isEmailVerified(),
            user.isVerifiedSeller()
        );
    }

    private AuthResponse issueTokens(User user, String userAgent, String ipAddress) {
        String accessToken = jwtService.generateAccessToken(user);
        String rawRefreshToken = UUID.randomUUID() + "." + UUID.randomUUID();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(tokenHasher.sha256(rawRefreshToken));
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(jwtService.refreshTokenExpirationDays()));
        refreshToken.setUserAgent(userAgent);
        refreshToken.setIpAddress(ipAddress);
        refreshTokenRepository.save(refreshToken);

        return new AuthResponse(
            accessToken,
            rawRefreshToken,
            "Bearer",
            jwtService.accessTokenExpiresInSeconds(),
            new AuthUserDto(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getRole().name(),
                user.isVerifiedSeller()
            )
        );
    }
}
