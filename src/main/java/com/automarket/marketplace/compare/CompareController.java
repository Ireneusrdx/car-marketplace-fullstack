package com.automarket.marketplace.compare;

import com.automarket.marketplace.auth.AuthException;
import com.automarket.marketplace.compare.dto.CompareSessionResponse;
import com.automarket.marketplace.compare.dto.SaveCompareSessionRequest;
import com.automarket.marketplace.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/compare")
@RequiredArgsConstructor
public class CompareController {

    private final CompareService compareService;

    @PostMapping
    public ResponseEntity<CompareSessionResponse> save(
        @Valid @RequestBody SaveCompareSessionRequest request,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(compareService.saveSession(request.listingIds(), principal));
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<CompareSessionResponse> getSession(
        @PathVariable UUID sessionId,
        @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(compareService.getSession(sessionId, principal));
    }

    @GetMapping("/quick")
    public ResponseEntity<CompareSessionResponse> quick(@RequestParam String ids) {
        if (ids == null || ids.isBlank()) {
            throw new AuthException(HttpStatus.BAD_REQUEST, "ids query parameter is required");
        }
        try {
            List<UUID> parsed = Arrays.stream(ids.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(UUID::fromString)
                .toList();
            return ResponseEntity.ok(compareService.quickCompare(parsed));
        } catch (IllegalArgumentException ex) {
            throw new AuthException(HttpStatus.BAD_REQUEST, "ids must be comma-separated UUID values");
        }
    }
}

