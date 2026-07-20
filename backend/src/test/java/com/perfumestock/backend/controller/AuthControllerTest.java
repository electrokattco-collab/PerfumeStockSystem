package com.perfumestock.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.perfumestock.backend.dto.LoginRequest;
import com.perfumestock.backend.dto.UserRequest;
import com.perfumestock.backend.entity.User;
import com.perfumestock.backend.exception.DuplicateResourceException;
import com.perfumestock.backend.security.AuthEntryPointJwt;
import com.perfumestock.backend.security.AuthTokenFilter;
import com.perfumestock.backend.security.JwtUtils;
import com.perfumestock.backend.security.UserDetailsImpl;
import com.perfumestock.backend.security.UserDetailsServiceImpl;
import com.perfumestock.backend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
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

    @MockBean
    private AuthTokenFilter authTokenFilter;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private AuthEntryPointJwt authEntryPointJwt;

    private LoginRequest validLoginRequest;
    private UserRequest validUserRequest;
    private UserDetailsImpl userDetails;
    private User createdUser;

    @BeforeEach
    void setUp() {
        validLoginRequest = new LoginRequest();
        validLoginRequest.setUsername("admin");
        validLoginRequest.setPassword("Admin1234");

        validUserRequest = new UserRequest();
        validUserRequest.setUsername("newuser");
        validUserRequest.setEmail("newuser@example.com");
        validUserRequest.setPassword("Password123");
        validUserRequest.setRole(User.Role.SALES_REP);

        userDetails = new UserDetailsImpl(
                1L, "admin", "admin@example.com", "encodedPassword",
                User.Role.ADMIN, true, null
        );

        createdUser = new User();
        createdUser.setId(4L);
        createdUser.setUsername("newuser");
        createdUser.setEmail("newuser@example.com");
        createdUser.setRole(User.Role.SALES_REP);
        createdUser.setActive(true);
    }

    @Test
    @DisplayName("Should authenticate user and set JWT cookie")
    void shouldAuthenticateUserSuccessfully() throws Exception {
        Authentication authentication = mock(Authentication.class);
        given(authentication.getPrincipal()).willReturn(userDetails);
        given(authenticationManager.authenticate(any())).willReturn(authentication);
        given(jwtUtils.generateJwtToken(authentication)).willReturn("mocked.jwt.token");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.username", is("admin")))
                .andExpect(jsonPath("$.role", is("ADMIN")))
                .andExpect(cookie().exists("jwt"))
                .andExpect(cookie().httpOnly("jwt", true));

        verify(authenticationManager, times(1)).authenticate(any());
    }

    @Test
    @DisplayName("Should return 401 when login fails")
    void shouldReturn401WhenLoginFails() throws Exception {
        given(authenticationManager.authenticate(any()))
                .willThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validLoginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should logout and clear JWT cookie")
    void shouldLogoutUserSuccessfully() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Logged out successfully")));
    }

    @Test
    @DisplayName("Should register user successfully")
    void shouldRegisterUser() throws Exception {
        given(userService.createUser(any(UserRequest.class))).willReturn(createdUser);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("User registered successfully: newuser")));
    }

    @Test
    @DisplayName("Should return 400 when registering with invalid data")
    void shouldReturn400WhenRegisteringWithInvalidData() throws Exception {
        UserRequest invalidRequest = new UserRequest();
        invalidRequest.setUsername("ab");
        invalidRequest.setEmail("invalid-email");
        invalidRequest.setPassword("short");
        invalidRequest.setRole(null);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 409 when registering with duplicate username")
    void shouldReturn409WhenRegisteringWithDuplicateUsername() throws Exception {
        given(userService.createUser(any(UserRequest.class)))
                .willThrow(new DuplicateResourceException("User", "username", "newuser"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserRequest)))
                .andExpect(status().isConflict());
    }
}
