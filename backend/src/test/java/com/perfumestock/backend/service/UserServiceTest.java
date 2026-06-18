package com.perfumestock.backend.service;

import com.perfumestock.backend.dto.UserRequest;
import com.perfumestock.backend.entity.User;
import com.perfumestock.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User adminUser;
    private User managerUser;
    private User salesUser;
    private UserRequest validUserRequest;

    @BeforeEach
    void setUp() {
        adminUser = createUser(1L, "admin", "admin@example.com", "encodedPassword", User.Role.ADMIN, true);
        managerUser = createUser(2L, "manager", "manager@example.com", "encodedPassword", User.Role.MANAGER, true);
        salesUser = createUser(3L, "sales", "sales@example.com", "encodedPassword", User.Role.SALES_REP, true);

        validUserRequest = new UserRequest();
        validUserRequest.setUsername("newuser");
        validUserRequest.setEmail("newuser@example.com");
        validUserRequest.setPassword("password123");
        validUserRequest.setRole(User.Role.SALES_REP);
    }

    private User createUser(Long id, String username, String email, String password, User.Role role, boolean active) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole(role);
        user.setActive(active);
        return user;
    }

    @Test
    @DisplayName("Should return all users when getAllUsers is called")
    void shouldReturnAllUsers() {
        // Given
        List<User> expectedUsers = Arrays.asList(adminUser, managerUser, salesUser);
        given(userRepository.findAll()).willReturn(expectedUsers);

        // When
        List<User> actualUsers = userService.getAllUsers();

        // Then
        assertThat(actualUsers)
                .isNotNull()
                .hasSize(3)
                .containsExactly(adminUser, managerUser, salesUser);
        then(userRepository).should(times(1)).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no users exist")
    void shouldReturnEmptyListWhenNoUsers() {
        // Given
        given(userRepository.findAll()).willReturn(List.of());

        // When
        List<User> actualUsers = userService.getAllUsers();

        // Then
        assertThat(actualUsers).isEmpty();
        then(userRepository).should(times(1)).findAll();
    }

    @Test
    @DisplayName("Should return user by ID when user exists")
    void shouldReturnUserByIdWhenExists() {
        // Given
        Long userId = 1L;
        given(userRepository.findById(userId)).willReturn(Optional.of(adminUser));

        // When
        User actualUser = userService.getUserById(userId);

        // Then
        assertThat(actualUser)
                .isNotNull()
                .satisfies(user -> {
                    assertThat(user.getId()).isEqualTo(userId);
                    assertThat(user.getUsername()).isEqualTo("admin");
                    assertThat(user.getEmail()).isEqualTo("admin@example.com");
                    assertThat(user.getRole()).isEqualTo(User.Role.ADMIN);
                });
        then(userRepository).should(times(1)).findById(userId);
    }

    @Test
    @DisplayName("Should throw RuntimeException when user not found by ID")
    void shouldThrowExceptionWhenUserNotFoundById() {
        // Given
        Long userId = 999L;
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUserById(userId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found with id: " + userId);
        then(userRepository).should(times(1)).findById(userId);
    }

    @Test
    @DisplayName("Should return user by username when user exists")
    void shouldReturnUserByUsernameWhenExists() {
        // Given
        String username = "manager";
        given(userRepository.findByUsername(username)).willReturn(Optional.of(managerUser));

        // When
        User actualUser = userService.getUserByUsername(username);

        // Then
        assertThat(actualUser)
                .isNotNull()
                .satisfies(user -> {
                    assertThat(user.getUsername()).isEqualTo(username);
                    assertThat(user.getRole()).isEqualTo(User.Role.MANAGER);
                });
        then(userRepository).should(times(1)).findByUsername(username);
    }

    @Test
    @DisplayName("Should throw RuntimeException when user not found by username")
    void shouldThrowExceptionWhenUserNotFoundByUsername() {
        // Given
        String username = "nonexistent";
        given(userRepository.findByUsername(username)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUserByUsername(username))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found: " + username);
    }

    @Test
    @DisplayName("Should create user successfully with valid data")
    void shouldCreateUserSuccessfully() {
        // Given
        String encodedPassword = "encodedPassword123";
        given(userRepository.existsByUsername(validUserRequest.getUsername())).willReturn(false);
        given(userRepository.existsByEmail(validUserRequest.getEmail())).willReturn(false);
        given(passwordEncoder.encode(validUserRequest.getPassword())).willReturn(encodedPassword);
        given(userRepository.save(any(User.class))).willAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(4L);
            return savedUser;
        });

        // When
        User createdUser = userService.createUser(validUserRequest);

        // Then
        assertThat(createdUser)
                .isNotNull()
                .satisfies(user -> {
                    assertThat(user.getId()).isEqualTo(4L);
                    assertThat(user.getUsername()).isEqualTo(validUserRequest.getUsername());
                    assertThat(user.getEmail()).isEqualTo(validUserRequest.getEmail());
                    assertThat(user.getPassword()).isEqualTo(encodedPassword);
                    assertThat(user.getRole()).isEqualTo(validUserRequest.getRole());
                    assertThat(user.isActive()).isTrue();
                });
        then(userRepository).should(times(1)).existsByUsername(validUserRequest.getUsername());
        then(userRepository).should(times(1)).existsByEmail(validUserRequest.getEmail());
        then(passwordEncoder).should(times(1)).encode(validUserRequest.getPassword());
        then(userRepository).should(times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw RuntimeException when username already exists")
    void shouldThrowExceptionWhenUsernameExists() {
        // Given
        given(userRepository.existsByUsername(validUserRequest.getUsername())).willReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.createUser(validUserRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Username already taken: " + validUserRequest.getUsername());
        then(userRepository).should(times(1)).existsByUsername(validUserRequest.getUsername());
        then(userRepository).should(never()).existsByEmail(anyString());
        then(userRepository).should(never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw RuntimeException when email already exists")
    void shouldThrowExceptionWhenEmailExists() {
        // Given
        given(userRepository.existsByUsername(validUserRequest.getUsername())).willReturn(false);
        given(userRepository.existsByEmail(validUserRequest.getEmail())).willReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.createUser(validUserRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Email already in use: " + validUserRequest.getEmail());
        then(userRepository).should(times(1)).existsByUsername(validUserRequest.getUsername());
        then(userRepository).should(times(1)).existsByEmail(validUserRequest.getEmail());
        then(userRepository).should(never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should update user successfully with valid data")
    void shouldUpdateUserSuccessfully() {
        // Given
        Long userId = 2L;
        UserRequest updateRequest = new UserRequest();
        updateRequest.setUsername("updatedManager");
        updateRequest.setEmail("updated@example.com");
        updateRequest.setPassword("newpassword123");
        updateRequest.setRole(User.Role.ADMIN);

        given(userRepository.findById(userId)).willReturn(Optional.of(managerUser));
        given(userRepository.existsByUsername(updateRequest.getUsername())).willReturn(false);
        given(userRepository.existsByEmail(updateRequest.getEmail())).willReturn(false);
        given(passwordEncoder.encode(updateRequest.getPassword())).willReturn("newEncodedPassword");
        given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

        // When
        User updatedUser = userService.updateUser(userId, updateRequest);

        // Then
        assertThat(updatedUser)
                .isNotNull()
                .satisfies(user -> {
                    assertThat(user.getUsername()).isEqualTo(updateRequest.getUsername());
                    assertThat(user.getEmail()).isEqualTo(updateRequest.getEmail());
                    assertThat(user.getRole()).isEqualTo(updateRequest.getRole());
                    assertThat(user.getPassword()).isEqualTo("newEncodedPassword");
                });
        then(userRepository).should(times(1)).findById(userId);
        then(userRepository).should(times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should update user without changing password when password is empty")
    void shouldUpdateUserWithoutChangingPasswordWhenEmpty() {
        // Given
        Long userId = 2L;
        String originalPassword = managerUser.getPassword();
        UserRequest updateRequest = new UserRequest();
        updateRequest.setUsername("updatedManager");
        updateRequest.setEmail(managerUser.getEmail());
        updateRequest.setPassword(""); // Empty password
        updateRequest.setRole(User.Role.MANAGER);

        given(userRepository.findById(userId)).willReturn(Optional.of(managerUser));
        given(userRepository.existsByUsername(updateRequest.getUsername())).willReturn(false);
        given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

        // When
        User updatedUser = userService.updateUser(userId, updateRequest);

        // Then
        assertThat(updatedUser.getPassword()).isEqualTo(originalPassword);
        then(passwordEncoder).should(never()).encode(anyString());
    }

    @Test
    @DisplayName("Should update user without changing password when password is null")
    void shouldUpdateUserWithoutChangingPasswordWhenNull() {
        // Given
        Long userId = 2L;
        String originalPassword = managerUser.getPassword();
        UserRequest updateRequest = new UserRequest();
        updateRequest.setUsername("updatedManager");
        updateRequest.setEmail(managerUser.getEmail());
        updateRequest.setPassword(null); // Null password
        updateRequest.setRole(User.Role.MANAGER);

        given(userRepository.findById(userId)).willReturn(Optional.of(managerUser));
        given(userRepository.existsByUsername(updateRequest.getUsername())).willReturn(false);
        given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

        // When
        User updatedUser = userService.updateUser(userId, updateRequest);

        // Then
        assertThat(updatedUser.getPassword()).isEqualTo(originalPassword);
        then(passwordEncoder).should(never()).encode(anyString());
    }

    @Test
    @DisplayName("Should allow updating user with same username")
    void shouldAllowUpdateWithSameUsername() {
        // Given
        Long userId = 2L;
        UserRequest updateRequest = new UserRequest();
        updateRequest.setUsername("manager"); // Same username
        updateRequest.setEmail("newemail@example.com");
        updateRequest.setPassword("password123");
        updateRequest.setRole(User.Role.MANAGER);

        given(userRepository.findById(userId)).willReturn(Optional.of(managerUser));
        given(userRepository.existsByEmail(updateRequest.getEmail())).willReturn(false);
        given(passwordEncoder.encode(anyString())).willReturn("encodedPassword");
        given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

        // When
        User updatedUser = userService.updateUser(userId, updateRequest);

        // Then
        assertThat(updatedUser.getUsername()).isEqualTo("manager");
        then(userRepository).should(never()).existsByUsername(anyString());
    }

    @Test
    @DisplayName("Should throw RuntimeException when updating user with taken username")
    void shouldThrowExceptionWhenUpdatingWithTakenUsername() {
        // Given
        Long userId = 2L;
        UserRequest updateRequest = new UserRequest();
        updateRequest.setUsername("admin"); // Already taken
        updateRequest.setEmail(managerUser.getEmail());
        updateRequest.setPassword("password123");
        updateRequest.setRole(User.Role.MANAGER);

        given(userRepository.findById(userId)).willReturn(Optional.of(managerUser));
        given(userRepository.existsByUsername("admin")).willReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.updateUser(userId, updateRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Username already taken: admin");
    }

    @Test
    @DisplayName("Should throw RuntimeException when updating non-existent user")
    void shouldThrowExceptionWhenUpdatingNonExistentUser() {
        // Given
        Long userId = 999L;
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.updateUser(userId, validUserRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found with id: " + userId);
    }

    @Test
    @DisplayName("Should deactivate user successfully")
    void shouldDeactivateUserSuccessfully() {
        // Given
        Long userId = 2L;
        given(userRepository.findById(userId)).willReturn(Optional.of(managerUser));
        given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

        // When
        userService.deleteUser(userId);

        // Then
        assertThat(managerUser.isActive()).isFalse();
        then(userRepository).should(times(1)).findById(userId);
        then(userRepository).should(times(1)).save(managerUser);
    }

    @Test
    @DisplayName("Should throw RuntimeException when deactivating non-existent user")
    void shouldThrowExceptionWhenDeactivatingNonExistentUser() {
        // Given
        Long userId = 999L;
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.deleteUser(userId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found with id: " + userId);
        then(userRepository).should(never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should activate user successfully")
    void shouldActivateUserSuccessfully() {
        // Given
        Long userId = 2L;
        managerUser.setActive(false); // Start inactive
        given(userRepository.findById(userId)).willReturn(Optional.of(managerUser));
        given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

        // When
        userService.activateUser(userId);

        // Then
        assertThat(managerUser.isActive()).isTrue();
        then(userRepository).should(times(1)).findById(userId);
        then(userRepository).should(times(1)).save(managerUser);
    }

    @Test
    @DisplayName("Should throw RuntimeException when activating non-existent user")
    void shouldThrowExceptionWhenActivatingNonExistentUser() {
        // Given
        Long userId = 999L;
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.activateUser(userId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found with id: " + userId);
    }
}
