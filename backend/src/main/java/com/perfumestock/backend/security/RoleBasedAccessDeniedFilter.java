package com.perfumestock.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class RoleBasedAccessDeniedFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RoleBasedAccessDeniedFilter.class);
    private final AccessDeniedHandler accessDeniedHandler;

    public RoleBasedAccessDeniedFilter(AccessDeniedHandler accessDeniedHandler) {
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (AccessDeniedException e) {
            log.warn("Access denied for request: {}", request.getRequestURI());
            if (!response.isCommitted()) {
                accessDeniedHandler.handle(request, response, e);
            }
        }
    }
}
