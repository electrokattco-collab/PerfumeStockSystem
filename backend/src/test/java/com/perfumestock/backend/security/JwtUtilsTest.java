package com.perfumestock.backend.security;

import com.perfumestock.backend.entity.User;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtUtils Unit Tests")
class JwtUtilsTest {

    private JwtUtils jwtUtils;

    @Mock
    private Authentication authentication;

    private final String jwtSecret = "mySecretKeyForTestingThatNeedsToBeAtLeast32CharactersLong";
    private final int jwtExpirationMs = 86400000; // 24 hours
    private UserDetailsImpl userDetails;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", jwtSecret);
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", jwtExpirationMs);

        userDetails = new UserDetailsImpl(
                1L,
                "testuser",
                "test@example.com",
                "password",
                User.Role.ADMIN,
                true,
                null
        );
    }

    @Test
    @DisplayName("Should generate valid JWT token from authentication")
    void shouldGenerateValidJwtTokenFromAuthentication() {
        // Given
        given(authentication.getPrincipal()).willReturn(userDetails);

        // When
        String token = jwtUtils.generateJwtToken(authentication);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts
    }

    @Test
    @DisplayName("Should generate valid JWT token from username")
    void shouldGenerateValidJwtTokenFromUsername() {
        // When
        String token = jwtUtils.generateTokenFromUsername("testuser");

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    @DisplayName("Should extract username from valid JWT token")
    void shouldExtractUsernameFromValidToken() {
        // Given
        given(authentication.getPrincipal()).willReturn(userDetails);
        String token = jwtUtils.generateJwtToken(authentication);

        // When
        String extractedUsername = jwtUtils.getUserNameFromJwtToken(token);

        // Then
        assertThat(extractedUsername).isEqualTo("testuser");
    }

    @Test
    @DisplayName("Should validate JWT token successfully")
    void shouldValidateJwtTokenSuccessfully() {
        // Given
        given(authentication.getPrincipal()).willReturn(userDetails);
        String token = jwtUtils.generateJwtToken(authentication);

        // When
        boolean isValid = jwtUtils.validateJwtToken(token);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Should return false for null token")
    void shouldReturnFalseForNullToken() {
        // When
        boolean isValid = jwtUtils.validateJwtToken(null);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should return false for empty token")
    void shouldReturnFalseForEmptyToken() {
        // When
        boolean isValid = jwtUtils.validateJwtToken("");

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should return false for malformed JWT token")
    void shouldReturnFalseForMalformedToken() {
        // Given
        String malformedToken = "this.is.not.a.valid.jwt";

        // When
        boolean isValid = jwtUtils.validateJwtToken(malformedToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should return false for expired JWT token")
    void shouldReturnFalseForExpiredToken() {
        // Given - Create an expired token manually
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        String expiredToken = Jwts.builder()
                .subject("testuser")
                .issuedAt(new Date(System.currentTimeMillis() - 86400000)) // 1 day ago
                .expiration(new Date(System.currentTimeMillis() - 3600000)) // Expired 1 hour ago
                .signWith(key, Jwts.SIG.HS256)
                .compact();

        // When
        boolean isValid = jwtUtils.validateJwtToken(expiredToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should return false for token with invalid signature")
    void shouldReturnFalseForInvalidSignature() {
        // Given - Create a token with different secret
        String differentSecret = "aDifferentSecretKeyThatIsAlso32CharactersLong";
        SecretKey differentKey = Keys.hmacShaKeyFor(differentSecret.getBytes(StandardCharsets.UTF_8));
        String tokenWithWrongSignature = Jwts.builder()
                .subject("testuser")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(differentKey, Jwts.SIG.HS256)
                .compact();

        // When
        boolean isValid = jwtUtils.validateJwtToken(tokenWithWrongSignature);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should return false for unsupported JWT token")
    void shouldReturnFalseForUnsupportedToken() {
        // Given - Create an unsecured token (no signature)
        String unsecuredToken = Jwts.builder()
                .subject("testuser")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .compact();

        // When
        boolean isValid = jwtUtils.validateJwtToken(unsecuredToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Should include correct claims in generated token")
    void shouldIncludeCorrectClaimsInToken() {
        // Given
        given(authentication.getPrincipal()).willReturn(userDetails);

        // When
        String token = jwtUtils.generateJwtToken(authentication);

        // Then - Parse and verify claims
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        var claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        assertThat(claims.getSubject()).isEqualTo("testuser");
        assertThat(claims.get("id", Long.class)).isEqualTo(1L);
        assertThat(claims.get("email", String.class)).isEqualTo("test@example.com");
        assertThat(claims.get("role", String.class)).isEqualTo("ADMIN");
        assertThat(claims.getIssuedAt()).isNotNull();
        assertThat(claims.getExpiration()).isNotNull();
        assertThat(claims.getExpiration()).isAfter(new Date());
    }

    @Test
    @DisplayName("Should generate token with correct expiration time")
    void shouldGenerateTokenWithCorrectExpirationTime() {
        // Given
        given(authentication.getPrincipal()).willReturn(userDetails);
        long beforeGeneration = System.currentTimeMillis();

        // When
        String token = jwtUtils.generateJwtToken(authentication);
        long afterGeneration = System.currentTimeMillis();

        // Then
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        var claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        long expectedExpiration = beforeGeneration + jwtExpirationMs;
        long actualExpiration = claims.getExpiration().getTime();

        // Allow for some time difference due to test execution
        assertThat(actualExpiration)
                .isGreaterThanOrEqualTo(expectedExpiration - 5000)
                .isLessThanOrEqualTo(afterGeneration + jwtExpirationMs + 5000);
    }

    @Test
    @DisplayName("Should generate unique tokens for each call")
    void shouldGenerateUniqueTokens() {
        // Given
        given(authentication.getPrincipal()).willReturn(userDetails);

        // When
        String token1 = jwtUtils.generateJwtToken(authentication);
        String token2 = jwtUtils.generateJwtToken(authentication);

        // Then
        assertThat(token1).isNotEqualTo(token2);
    }
}
