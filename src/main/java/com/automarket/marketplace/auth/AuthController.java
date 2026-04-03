package com.automarket.marketplace.auth;

import com.automarket.marketplace.auth.dto.AuthResponse;
import com.automarket.marketplace.auth.dto.EmailLoginRequest;
import com.automarket.marketplace.auth.dto.EmailRegisterRequest;
import com.automarket.marketplace.auth.dto.FirebaseAuthRequest;
import com.automarket.marketplace.auth.dto.LogoutRequest;
import com.automarket.marketplace.auth.dto.MeResponse;
import com.automarket.marketplace.auth.dto.RefreshRequest;
import com.automarket.marketplace.security.UserPrincipal;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.http.ResponseCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.CookieValue;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final FirebaseAuthService firebaseAuthService;

    @PostMapping("/email/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody EmailRegisterRequest request,
                                                 HttpServletRequest httpRequest,
                                                 HttpServletResponse httpResponse) {
        AuthResponse authResponse = authService.registerEmail(request, httpRequest.getHeader("User-Agent"), clientIp(httpRequest));
        addRefreshTokenCookie(httpResponse, authResponse.refreshToken());
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/email/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody EmailLoginRequest request,
                                              HttpServletRequest httpRequest,
                                              HttpServletResponse httpResponse) {
        AuthResponse authResponse = authService.loginEmail(request, httpRequest.getHeader("User-Agent"), clientIp(httpRequest));
        addRefreshTokenCookie(httpResponse, authResponse.refreshToken());
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/firebase")
    public ResponseEntity<AuthResponse> firebase(@Valid @RequestBody FirebaseAuthRequest request,
                                                 HttpServletRequest httpRequest,
                                                 HttpServletResponse httpResponse) {
        FirebaseToken token = firebaseAuthService.verifyIdToken(request.idToken());
        AuthResponse authResponse = authService.firebaseLogin(token, httpRequest.getHeader("User-Agent"), clientIp(httpRequest));
        addRefreshTokenCookie(httpResponse, authResponse.refreshToken());
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@CookieValue(name = "refreshToken", required = false) String refreshToken,
                                                HttpServletRequest httpRequest,
                                                HttpServletResponse httpResponse) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.status(401).build();
        }
        AuthResponse authResponse = authService.refresh(refreshToken, httpRequest.getHeader("User-Agent"), clientIp(httpRequest));
        addRefreshTokenCookie(httpResponse, authResponse.refreshToken());
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue(name = "refreshToken", required = false) String refreshToken,
                                       HttpServletResponse httpResponse) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            authService.logout(refreshToken);
            clearRefreshTokenCookie(httpResponse);
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<MeResponse> me(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(authService.me(principal));
    }

    private String clientIp(HttpServletRequest request) {
        String header = request.getHeader("X-Forwarded-For");
        if (header != null && !header.isBlank()) {
            return header.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
    
    private void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        if (refreshToken == null) return;
        ResponseCookie cookie = ResponseCookie
                .from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(false) // Change to true if in production and on HTTPS
                .path("/api/auth/refresh")
                .maxAge(Duration.ofDays(7))
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie
                .from("refreshToken", "")
                .httpOnly(true)
                .secure(false)
                .path("/api/auth/refresh")
                .maxAge(0)
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}

