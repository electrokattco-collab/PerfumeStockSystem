package com.perfumestock.backend.controller;

import com.perfumestock.backend.dto.JwtResponse;
import com.perfumestock.backend.dto.LoginRequest;
import com.perfumestock.backend.dto.MessageResponse;
import com.perfumestock.backend.dto.UserRequest;
import com.perfumestock.backend.entity.User;
import com.perfumestock.backend.security.JwtUtils;
import com.perfumestock.backend.security.UserDetailsImpl;
import com.perfumestock.backend.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication controller handling login, logout, and user session.
 * CORS is configured globally in WebSecurityConfig - do not add @CrossOrigin here
 * as it can conflict with credentials support.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserService userService;

    @Value("${jwt.expiration:86400000}")
    private int jwtExpirationMs;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager, 
                          JwtUtils jwtUtils,
                          UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.userService = userService;
    }

    /**
     * Authenticates user and sets JWT as an httpOnly cookie.
     * The token is no longer returned in the response body for security.
     */
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest, 
                                               HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);
        
        // Set JWT as httpOnly cookie
        Cookie jwtCookie = new Cookie("jwt", jwt);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(true); // Requires HTTPS
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(jwtExpirationMs / 1000); // Convert ms to seconds
        jwtCookie.setAttribute("SameSite", "Strict");
        response.addCookie(jwtCookie);
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        // Return user info without token
        return ResponseEntity.ok(new JwtResponse(
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                userDetails.getRole()
        ));
    }

    /**
     * Clears the JWT cookie to log out the user.
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(HttpServletResponse response) {
        // Clear the JWT cookie
        Cookie jwtCookie = new Cookie("jwt", null);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0); // Delete cookie
        jwtCookie.setAttribute("SameSite", "Strict");
        response.addCookie(jwtCookie);

        return ResponseEntity.ok(new MessageResponse("Logged out successfully"));
    }

    /**
     * Returns the currently authenticated user's information.
     * Used by frontend to verify session on app initialization.
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(new MessageResponse("Not authenticated"));
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        return ResponseEntity.ok(new JwtResponse(
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                userDetails.getRole()
        ));
    }

    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRequest userRequest) {
        try {
            User user = userService.createUser(userRequest);
            return ResponseEntity.ok(new MessageResponse("User registered successfully: " + user.getUsername()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        }
    }
}
