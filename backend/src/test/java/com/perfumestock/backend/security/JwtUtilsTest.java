package com.perfumestock.backend.security;

import com.perfumestock.backend.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtUtils Unit Tests")
class JwtUtilsTest {

    private JwtUtils jwtUtils;

    @Mock
    private Authentication authentication;

    private final String jwtSecret = "mySecretKeyForTestingThatNeedsToBeAtLeast32CharactersLong";
    private final int jwtExpirationMs = 86400000;
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
        given(authentication.getPrincipal()).willReturn(userDetails);
        String token = jwtUtils.generateJwtToken(authentication);
        assertThat(token).isNotNull().isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    @DisplayName("Should generate valid JWT token from username")
    void shouldGenerateValidJwtTokenFromUsername() {
        String token = jwtUtils.generateTokenFromUsername("testuser");
        assertThat(token).isNotNull().isNotEmpty();
        assertThat(token.split("\\.")).hasSize(3);
    }

    @Test
    @DisplayName("Should extract username from valid JWT token")
    void shouldExtractUsernameFromValidToken() {
        given(authentication.getPrincipal()).willReturn(userDetails);
        String token = jwtUtils.generateJwtToken(authentication);
        assertThat(jwtUtils.getUserNameFromJwtToken(token)).isEqualTo("testuser");
    }

    @Test
    @DisplayName("Should validate JWT token successfully")
    void shouldValidateJwtTokenSuccessfully() {
        given(authentication.getPrincipal()).willReturn(userDetails);
        String token = jwtUtils.generateJwtToken(authentication);
        assertThat(jwtUtils.validateJwtToken(token)).isTrue();
    }

    @Test
    @DisplayName("Should return false for null token")
    void shouldReturnFalseForNullToken() {
        assertThat(jwtUtils.validateJwtToken(null)).isFalse();
    }

    @Test
    @DisplayName("Should return false for empty token")
    void shouldReturnFalseForEmptyToken() {
        assertThat(jwtUtils.validateJwtToken("")).isFalse();
    }

    @Test
    @DisplayName("Should return false for malformed JWT token")
    void shouldReturnFalseForMalformedToken() {
        assertThat(jwtUtils.validateJwtToken("this.is.not.a.valid.jwt")).isFalse();
    }

    @Test
    @DisplayName("Should return false for expired JWT token")
    void shouldReturnFalseForExpiredToken() {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        String expiredToken = Jwts.builder()
                .subject("testuser")
                .issuedAt(new Date(System.currentTimeMillis() - 86400000))
                .expiration(new Date(System.currentTimeMillis() - 3600000))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
        assertThat(jwtUtils.validateJwtToken(expiredToken)).isFalse();
    }

    @Test
    @DisplayName("Should return false for token with invalid signature")
    void shouldReturnFalseForInvalidSignature() {
        String differentSecret = "aDifferentSecretKeyThatIsAlso32CharactersLong";
        SecretKey differentKey = Keys.hmacShaKeyFor(differentSecret.getBytes(StandardCharsets.UTF_8));
        String tokenWithWrongSignature = Jwts.builder()
                .subject("testuser")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(differentKey, Jwts.SIG.HS256)
                .compact();
        assertThat(jwtUtils.validateJwtToken(tokenWithWrongSignature)).isFalse();
    }

    @Test
    @DisplayName("Should include correct claims in generated token")
    void shouldIncludeCorrectClaimsInToken() {
        given(authentication.getPrincipal()).willReturn(userDetails);
        String token = jwtUtils.generateJwtToken(authentication);

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
        given(authentication.getPrincipal()).willReturn(userDetails);
        long beforeGeneration = System.currentTimeMillis();
        String token = jwtUtils.generateJwtToken(authentication);
        long afterGeneration = System.currentTimeMillis();

        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        var claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        long expectedExpiration = beforeGeneration + jwtExpirationMs;
        long actualExpiration = claims.getExpiration().getTime();

        assertThat(actualExpiration)
                .isGreaterThanOrEqualTo(expectedExpiration - 5000)
                .isLessThanOrEqualTo(afterGeneration + jwtExpirationMs + 5000);
    }
}
