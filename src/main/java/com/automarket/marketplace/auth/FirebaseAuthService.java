package com.automarket.marketplace.auth;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.ObjectProvider;

@Service
@RequiredArgsConstructor
public class FirebaseAuthService {

    private final ObjectProvider<FirebaseApp> firebaseAppProvider;

    public FirebaseToken verifyIdToken(String idToken) {
        try {
            FirebaseApp firebaseApp = firebaseAppProvider.getIfAvailable();
            if (firebaseApp == null) {
                throw new AuthException(HttpStatus.SERVICE_UNAVAILABLE, "Firebase auth is not configured");
            }
            return FirebaseAuth.getInstance(firebaseApp).verifyIdToken(idToken);
        } catch (AuthException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new AuthException(HttpStatus.UNAUTHORIZED, "Invalid Firebase token");
        }
    }
}
