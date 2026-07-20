package com.perfumestock.backend.service;

import com.perfumestock.backend.dto.UserRequest;
import com.perfumestock.backend.entity.User;
import com.perfumestock.backend.exception.DuplicateResourceException;
import com.perfumestock.backend.exception.ResourceNotFoundException;
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
    @DisplayName("Should return all users")
    void shouldReturnAllUsers() {
        given(userRepository.findAll()).willReturn(Arrays.asList(adminUser, managerUser, salesUser));
        List<User> result = userService.getAllUsers();
        assertThat(result).hasSize(3);
        then(userRepository).should(times(1)).findAll();
    }

    @Test
    @DisplayName("Should return user by ID when exists")
    void shouldReturnUserById() {
        given(userRepository.findById(1L)).willReturn(Optional.of(adminUser));
        User result = userService.getUserById(1L);
        assertThat(result.getUsername()).isEqualTo("admin");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when user not found")
    void shouldThrowWhenUserNotFound() {
        given(userRepository.findById(999L)).willReturn(Optional.empty());
        assertThatThrownBy(() -> userService.getUserById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should create user successfully")
    void shouldCreateUser() {
        given(userRepository.existsByUsername("newuser")).willReturn(false);
        given(userRepository.existsByEmail("newuser@example.com")).willReturn(false);
        given(passwordEncoder.encode(anyString())).willReturn("encodedPassword");
        given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

        User result = userService.createUser(validUserRequest);

        assertThat(result.getUsername()).isEqualTo("newuser");
        then(userRepository).should(times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException for taken username")
    void shouldThrowForTakenUsername() {
        given(userRepository.existsByUsername("admin")).willReturn(true);
        validUserRequest.setUsername("admin");

        assertThatThrownBy(() -> userService.createUser(validUserRequest))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException for taken email")
    void shouldThrowForTakenEmail() {
        given(userRepository.existsByUsername("newuser")).willReturn(false);
        given(userRepository.existsByEmail("admin@example.com")).willReturn(true);
        validUserRequest.setEmail("admin@example.com");

        assertThatThrownBy(() -> userService.createUser(validUserRequest))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    @DisplayName("Should update user successfully")
    void shouldUpdateUser() {
        UserRequest updateRequest = new UserRequest();
        updateRequest.setUsername("manager");
        updateRequest.setEmail("newemail@example.com");
        updateRequest.setPassword("password123");
        updateRequest.setRole(User.Role.MANAGER);

        given(userRepository.findById(2L)).willReturn(Optional.of(managerUser));
        given(userRepository.existsByEmail("newemail@example.com")).willReturn(false);
        given(passwordEncoder.encode(anyString())).willReturn("encodedPassword");
        given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

        User result = userService.updateUser(2L, updateRequest);

        assertThat(result.getEmail()).isEqualTo("newemail@example.com");
    }

    @Test
    @DisplayName("Should not re-encode password when not provided")
    void shouldNotReencodePasswordWhenNotProvided() {
        String originalPassword = managerUser.getPassword();
        UserRequest updateRequest = new UserRequest();
        updateRequest.setUsername("manager");
        updateRequest.setEmail("newemail@example.com");
        updateRequest.setPassword("");
        updateRequest.setRole(User.Role.MANAGER);

        given(userRepository.findById(2L)).willReturn(Optional.of(managerUser));
        given(userRepository.existsByEmail("newemail@example.com")).willReturn(false);
        given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

        User result = userService.updateUser(2L, updateRequest);

        assertThat(result.getPassword()).isEqualTo(originalPassword);
        then(passwordEncoder).should(never()).encode(anyString());
    }

    @Test
    @DisplayName("Should deactivate user successfully")
    void shouldDeactivateUser() {
        given(userRepository.findById(2L)).willReturn(Optional.of(managerUser));
        given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

        userService.deleteUser(2L);

        assertThat(managerUser.isActive()).isFalse();
        then(userRepository).should(times(1)).save(managerUser);
    }

    @Test
    @DisplayName("Should activate user successfully")
    void shouldActivateUser() {
        managerUser.setActive(false);
        given(userRepository.findById(2L)).willReturn(Optional.of(managerUser));
        given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

        userService.activateUser(2L);

        assertThat(managerUser.isActive()).isTrue();
    }
}
