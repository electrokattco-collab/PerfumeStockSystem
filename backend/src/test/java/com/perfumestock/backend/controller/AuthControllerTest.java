package com.perfumestock.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.perfumestock.backend.dto.LoginRequest;
import com.perfumestock.backend.dto.UserRequest;
import com.perfumestock.backend.entity.User;
import com.perfumestock.backend.security.JwtUtils;
import com.perfumestock.backend.security.UserDetailsImpl;
import com.perfumestock.backend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@DisplayName("AuthController Web MVC Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private UserService userService;

    private LoginRequest validLoginRequest;
    private UserRequest validUserRequest;
    private UserDetailsImpl userDetails;
    private User createdUser;

    @BeforeEach
    void setUp() {
        validLoginRequest = new LoginRequest();
        validLoginRequest.setUsername("admin");
        validLoginRequest.setPassword("admin123");

        validUserRequest = new UserRequest();
        validUserRequest.setUsername("newuser");
        validUserRequest.setEmail("newuser@example.com");
        validUserRequest.setPassword("password123");
        validUserRequest.setRole(User.Role.SALES_REP);

        userDetails = new UserDetailsImpl(
                1L,
                "admin",
                "admin@example.com",
                "encodedPassword",
                User.Role.ADMIN,
                true,
                null
        );

        createdUser = new User();
        createdUser.setId(4L);
        createdUser.setUsername("newuser");
        createdUser.setEmail("newuser@example.com");
        createdUser.setRole(User.Role.SALES_REP);
        createdUser.setActive(true);
    }

    @Test
    @DisplayName("Should authenticate user successfully with valid credentials")
    void shouldAuthenticateUserSuccessfully() throws Exception {
        // Given
        Authentication authentication = mock(Authentication.class);
        given(authentication.getPrincipal()).willReturn(userDetails);
        given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .willReturn(authentication);
        given(jwtUtils.generateJwtToken(authentication)).willReturn("mocked.jwt.token");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.username", is("admin")))
                .andExpect(jsonPath("$.email", is("admin@example.com")))
                .andExpect(jsonPath("$.role", is("ADMIN")))
                .andExpect(cookie().exists("jwt"))
                .andExpect(cookie().httpOnly("jwt", true))
                .andExpect(cookie().secure("jwt", true));

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtils, times(1)).generateJwtToken(authentication);
    }

    @Test
    @DisplayName("Should return 401 when authentication fails with invalid credentials")
    void shouldReturn401WhenAuthenticationFails() throws Exception {
        // Given
        LoginRequest invalidLoginRequest = new LoginRequest();
        invalidLoginRequest.setUsername("admin");
        invalidLoginRequest.setPassword("wrongpassword");

        given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .willThrow(new BadCredentialsException("Invalid credentials"));

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidLoginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 400 when login request has blank username")
    void shouldReturn400WhenUsernameIsBlank() throws Exception {
        // Given
        LoginRequest invalidRequest = new LoginRequest();
        invalidRequest.setUsername("");
        invalidRequest.setPassword("password123");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when login request has blank password")
    void shouldReturn400WhenPasswordIsBlank() throws Exception {
        // Given
        LoginRequest invalidRequest = new LoginRequest();
        invalidRequest.setUsername("admin");
        invalidRequest.setPassword("");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 when login request body is empty")
    void shouldReturn400WhenLoginRequestIsEmpty() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should logout user successfully and clear JWT cookie")
    void shouldLogoutUserSuccessfully() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/auth/logout")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Logged out successfully")))
                .andExpect(cookie().exists("jwt"))
                .andExpect(cookie().value("jwt", ""))
                .andExpect(cookie().maxAge("jwt", 0));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("Should return current user info when authenticated")
    void shouldReturnCurrentUserWhenAuthenticated() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/auth/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("admin")))
                .andExpect(jsonPath("$.role", is("ADMIN")));
    }

    @Test
    @DisplayName("Should return 401 when getting current user without authentication")
    void shouldReturn401WhenGettingCurrentUserUnauthenticated() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/auth/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message", is("Not authenticated")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should register user successfully when authenticated as ADMIN")
    void shouldRegisterUserSuccessfullyWhenAdmin() throws Exception {
        // Given
        given(userService.createUser(any(UserRequest.class))).willReturn(createdUser);

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("User registered successfully: newuser")));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    @DisplayName("Should return 403 when MANAGER tries to register user")
    void shouldReturn403WhenManagerTriesToRegister() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SALES_REP")
    @DisplayName("Should return 403 when SALES_REP tries to register user")
    void shouldReturn403WhenSalesRepTriesToRegister() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 401 when unauthenticated user tries to register")
    void shouldReturn401WhenUnauthenticatedTriesToRegister() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 400 when registering user with duplicate username")
    void shouldReturn400WhenRegisteringWithDuplicateUsername() throws Exception {
        // Given
        given(userService.createUser(any(UserRequest.class)))
                .willThrow(new RuntimeException("Username already taken: newuser"));

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Username already taken: newuser")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 400 when registering user with invalid data")
    void shouldReturn400WhenRegisteringWithInvalidData() throws Exception {
        // Given
        UserRequest invalidRequest = new UserRequest();
        invalidRequest.setUsername("ab"); // Too short
        invalidRequest.setEmail("invalid-email");
        invalidRequest.setPassword("12345"); // Too short
        invalidRequest.setRole(null);

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should require CSRF token for login")
    void shouldRequireCsrfForLogin() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should require CSRF token for logout")
    void shouldRequireCsrfForLogout() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
