package com.perfumestock.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.perfumestock.backend.dto.PageResponse;
import com.perfumestock.backend.dto.UserRequest;
import com.perfumestock.backend.entity.User;
import com.perfumestock.backend.security.AuthEntryPointJwt;
import com.perfumestock.backend.security.AuthTokenFilter;
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
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("UserController Web MVC Tests")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthTokenFilter authTokenFilter;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private AuthEntryPointJwt authEntryPointJwt;

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
        validUserRequest.setPassword("Password123");
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
    @DisplayName("Should return paginated users")
    void shouldReturnAllUsers() throws Exception {
        PageResponse<User> page = new PageResponse<>(
                Arrays.asList(adminUser, managerUser, salesUser),
                0, 50, 3, 1, true, true, false
        );
        given(userService.getAllUsers(any(org.springframework.data.domain.Pageable.class)))
                .willReturn(page);

        mockMvc.perform(get("/api/users")
                        .param("page", "0")
                        .param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()", is(3)))
                .andExpect(jsonPath("$.content[0].username", is("admin")))
                .andExpect(jsonPath("$.totalElements", is(3)))
                .andExpect(jsonPath("$.totalPages", is(1)));
    }

    @Test
    @DisplayName("Should return user by ID")
    void shouldReturnUserById() throws Exception {
        given(userService.getUserById(1L)).willReturn(adminUser);

        mockMvc.perform(get("/api/users/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("admin")));
    }

    @Test
    @DisplayName("Should create user successfully")
    void shouldCreateUser() throws Exception {
        User newUser = createUser(4L, "newuser", "newuser@example.com", User.Role.SALES_REP, true);
        given(userService.createUser(any(UserRequest.class))).willReturn(newUser);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("newuser")));
    }

    @Test
    @DisplayName("Should update user successfully")
    void shouldUpdateUser() throws Exception {
        UserRequest updateRequest = new UserRequest();
        updateRequest.setUsername("updated");
        updateRequest.setEmail("updated@example.com");
        updateRequest.setPassword("Password123");
        updateRequest.setRole(User.Role.ADMIN);

        User updatedUser = createUser(1L, "updated", "updated@example.com", User.Role.ADMIN, true);
        given(userService.updateUser(eq(1L), any(UserRequest.class))).willReturn(updatedUser);

        mockMvc.perform(put("/api/users/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("updated")));
    }

    @Test
    @DisplayName("Should deactivate user successfully")
    void shouldDeactivateUser() throws Exception {
        willDoNothing().given(userService).deleteUser(2L);

        mockMvc.perform(delete("/api/users/{id}", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("User deleted successfully")));
    }

    @Test
    @DisplayName("Should activate user successfully")
    void shouldActivateUser() throws Exception {
        willDoNothing().given(userService).activateUser(2L);

        mockMvc.perform(post("/api/users/{id}/activate", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("User activated")));
    }
}
