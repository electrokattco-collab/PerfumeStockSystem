package com.perfumestock.backend.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Simple in-memory rate limiter using a sliding window of requests per IP.
 * Limits API requests to prevent abuse.
 * In production, replace with Redis-based rate limiting (e.g., Bucket4j + Redis).
 */
@Component
@Order(1)
public class RateLimitingFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitingFilter.class);

    @Value("${app.rate-limit.max-requests:100}")
    private int maxRequests;

    @Value("${app.rate-limit.window-seconds:60}")
    private int windowSeconds;

    private final ConcurrentHashMap<String, SlidingWindow> windows = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (maxRequests <= 0) {
            chain.doFilter(request, response);
            return;
        }

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String clientIp = getClientIp(httpRequest);
        String key = httpRequest.getRequestURI() + ":" + clientIp;

        // Skip rate limiting for static assets and actuator health
        String path = httpRequest.getRequestURI();
        if (path.startsWith("/actuator/health") || path.startsWith("/api/images/")
                || path.startsWith("/api/barcodes/") || path.startsWith("/swagger")
                || path.startsWith("/v3/api-docs")) {
            chain.doFilter(request, response);
            return;
        }

        SlidingWindow window = windows.computeIfAbsent(
                key, k -> new SlidingWindow(maxRequests, windowSeconds));

        if (window.tryAcquire()) {
            httpResponse.setHeader("X-RateLimit-Limit", String.valueOf(maxRequests));
            httpResponse.setHeader("X-RateLimit-Remaining", String.valueOf(window.available()));
            chain.doFilter(request, response);
        } else {
            httpResponse.setStatus(429);
            httpResponse.setContentType("application/json");
            httpResponse.setHeader("X-RateLimit-Limit", String.valueOf(maxRequests));
            httpResponse.setHeader("X-RateLimit-Remaining", "0");
            httpResponse.setHeader("Retry-After", String.valueOf(windowSeconds));
            httpResponse.getWriter().write(
                    "{\"status\":429,\"message\":\"Too many requests. Please try again in " +
                    windowSeconds + " seconds.\"}");
            log.warn("Rate limit exceeded for IP: {}", clientIp);
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    @Override
    public void init(FilterConfig filterConfig) {}

    @Override
    public void destroy() {
        windows.clear();
    }

    /**
     * Sliding window rate limiter per key.
     */
    static class SlidingWindow {
        private final int maxRequests;
        private final long windowNanos;
        private final ConcurrentHashMap<Long, Integer> buckets;

        SlidingWindow(int maxRequests, int windowSeconds) {
            this.maxRequests = maxRequests;
            this.windowNanos = TimeUnit.SECONDS.toNanos(windowSeconds);
            this.buckets = new ConcurrentHashMap<>();
        }

        synchronized boolean tryAcquire() {
            long now = System.nanoTime();
            long cutoff = now - windowNanos;

            // Remove expired buckets
            buckets.entrySet().removeIf(e -> e.getKey() < cutoff);

            int total = 0;
            for (int v : buckets.values()) total += v;
            if (total >= maxRequests) {
                return false;
            }

            long bucketKey = now / 100_000_000; // ~100ms granularity
            buckets.merge(bucketKey, 1, Integer::sum);
            return true;
        }

        int available() {
            long now = System.nanoTime();
            long cutoff = now - windowNanos;
            int total = 0;
            for (var entry : buckets.entrySet()) {
                if (entry.getKey() >= cutoff) total += entry.getValue();
            }
            return Math.max(0, maxRequests - total);
        }
    }
}
