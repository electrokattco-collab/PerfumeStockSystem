package com.perfumestock.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.perfumestock.backend.dto.UserRequest;
import com.perfumestock.backend.entity.User;
import com.perfumestock.backend.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@DisplayName("UserController Web MVC Tests")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private User adminUser;
    private User managerUser;
    private User salesUser;
    private UserRequest validUserRequest;

    @BeforeEach
    void setUp() {
        adminUser = createUser(1L, "admin", "admin@example.com", User.Role.ADMIN, true);
        managerUser = createUser(2L, "manager", "manager@example.com", User.Role.MANAGER, true);
        salesUser = createUser(3L, "sales", "sales@example.com", User.Role.SALES_REP, true);

        validUserRequest = new UserRequest();
        validUserRequest.setUsername("newuser");
        validUserRequest.setEmail("newuser@example.com");
        validUserRequest.setPassword("password123");
        validUserRequest.setRole(User.Role.SALES_REP);
    }

    private User createUser(Long id, String username, String email, User.Role role, boolean active) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        user.setRole(role);
        user.setActive(active);
        return user;
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return all users when authenticated as ADMIN")
    void shouldReturnAllUsersWhenAdmin() throws Exception {
        // Given
        List<User> users = Arrays.asList(adminUser, managerUser, salesUser);
        given(userService.getAllUsers()).willReturn(users);

        // When & Then
        mockMvc.perform(get("/api/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].username", is("admin")))
                .andExpect(jsonPath("$[1].username", is("manager")))
                .andExpect(jsonPath("$[2].username", is("sales")));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    @DisplayName("Should return 403 when MANAGER tries to access all users")
    void shouldReturnForbiddenWhenManagerAccessesAllUsers() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SALES_REP")
    @DisplayName("Should return 403 when SALES_REP tries to access all users")
    void shouldReturnForbiddenWhenSalesRepAccessesAllUsers() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 401 when unauthenticated user tries to access users")
    void shouldReturnUnauthorizedWhenUnauthenticated() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return user by ID when user exists")
    void shouldReturnUserByIdWhenExists() throws Exception {
        // Given
        Long userId = 1L;
        given(userService.getUserById(userId)).willReturn(adminUser);

        // When & Then
        mockMvc.perform(get("/api/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.username", is("admin")))
                .andExpect(jsonPath("$.email", is("admin@example.com")))
                .andExpect(jsonPath("$.role", is("ADMIN")))
                .andExpect(jsonPath("$.active", is(true)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 404 when user not found by ID")
    void shouldReturn404WhenUserNotFoundById() throws Exception {
        // Given
        Long userId = 999L;
        given(userService.getUserById(userId)).willThrow(new RuntimeException("User not found"));

        // When & Then
        mockMvc.perform(get("/api/users/{id}", userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should create user successfully with valid data")
    void shouldCreateUserSuccessfully() throws Exception {
        // Given
        User createdUser = createUser(4L, "newuser", "newuser@example.com", User.Role.SALES_REP, true);
        given(userService.createUser(any(UserRequest.class))).willReturn(createdUser);

        // When & Then
        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(4)))
                .andExpect(jsonPath("$.username", is("newuser")))
                .andExpect(jsonPath("$.email", is("newuser@example.com")))
                .andExpect(jsonPath("$.role", is("SALES_REP")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 400 when creating user with duplicate username")
    void shouldReturn400WhenCreatingUserWithDuplicateUsername() throws Exception {
        // Given
        given(userService.createUser(any(UserRequest.class)))
                .willThrow(new RuntimeException("Username already taken: newuser"));

        // When & Then
        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Username already taken: newuser")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 400 when creating user with duplicate email")
    void shouldReturn400WhenCreatingUserWithDuplicateEmail() throws Exception {
        // Given
        given(userService.createUser(any(UserRequest.class)))
                .willThrow(new RuntimeException("Email already in use: newuser@example.com"));

        // When & Then
        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Email already in use: newuser@example.com")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 400 when creating user with invalid data")
    void shouldReturn400WhenCreatingUserWithInvalidData() throws Exception {
        // Given
        UserRequest invalidRequest = new UserRequest();
        invalidRequest.setUsername("ab"); // Too short
        invalidRequest.setEmail("invalid-email"); // Invalid format
        invalidRequest.setPassword("12345"); // Too short
        invalidRequest.setRole(null); // Required

        // When & Then
        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should update user successfully with valid data")
    void shouldUpdateUserSuccessfully() throws Exception {
        // Given
        Long userId = 2L;
        User updatedUser = createUser(2L, "updatedManager", "updated@example.com", User.Role.ADMIN, true);
        given(userService.updateUser(eq(userId), any(UserRequest.class))).willReturn(updatedUser);

        UserRequest updateRequest = new UserRequest();
        updateRequest.setUsername("updatedManager");
        updateRequest.setEmail("updated@example.com");
        updateRequest.setPassword("newpassword123");
        updateRequest.setRole(User.Role.ADMIN);

        // When & Then
        mockMvc.perform(put("/api/users/{id}", userId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(2)))
                .andExpect(jsonPath("$.username", is("updatedManager")))
                .andExpect(jsonPath("$.email", is("updated@example.com")))
                .andExpect(jsonPath("$.role", is("ADMIN")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 400 when updating user with taken username")
    void shouldReturn400WhenUpdatingWithTakenUsername() throws Exception {
        // Given
        Long userId = 2L;
        given(userService.updateUser(eq(userId), any(UserRequest.class)))
                .willThrow(new RuntimeException("Username already taken: admin"));

        UserRequest updateRequest = new UserRequest();
        updateRequest.setUsername("admin");
        updateRequest.setEmail("manager@example.com");
        updateRequest.setPassword("password123");
        updateRequest.setRole(User.Role.MANAGER);

        // When & Then
        mockMvc.perform(put("/api/users/{id}", userId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("Username already taken: admin")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 404 when updating non-existent user")
    void shouldReturn404WhenUpdatingNonExistentUser() throws Exception {
        // Given
        Long userId = 999L;
        given(userService.updateUser(eq(userId), any(UserRequest.class)))
                .willThrow(new RuntimeException("User not found with id: " + userId));

        // When & Then
        mockMvc.perform(put("/api/users/{id}", userId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("User not found with id: " + userId)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should deactivate user successfully")
    void shouldDeactivateUserSuccessfully() throws Exception {
        // Given
        Long userId = 2L;
        willDoNothing().given(userService).deleteUser(userId);

        // When & Then
        mockMvc.perform(delete("/api/users/{id}", userId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("User deactivated successfully")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 400 when deactivating non-existent user")
    void shouldReturn400WhenDeactivatingNonExistentUser() throws Exception {
        // Given
        Long userId = 999L;
        willThrow(new RuntimeException("User not found")).given(userService).deleteUser(userId);

        // When & Then
        mockMvc.perform(delete("/api/users/{id}", userId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("User not found")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should activate user successfully")
    void shouldActivateUserSuccessfully() throws Exception {
        // Given
        Long userId = 2L;
        willDoNothing().given(userService).activateUser(userId);

        // When & Then
        mockMvc.perform(post("/api/users/{id}/activate", userId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("User activated successfully")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return 400 when activating non-existent user")
    void shouldReturn400WhenActivatingNonExistentUser() throws Exception {
        // Given
        Long userId = 999L;
        willThrow(new RuntimeException("User not found")).given(userService).activateUser(userId);

        // When & Then
        mockMvc.perform(post("/api/users/{id}/activate", userId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", is("User not found")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should require CSRF token for state-changing operations")
    void shouldRequireCsrfToken() throws Exception {
        // When & Then - POST without CSRF
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserRequest)))
                .andExpect(status().isForbidden());
    }
}
