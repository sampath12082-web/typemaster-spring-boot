package com.typingtutor.security;

import com.typingtutor.entity.Role;
import com.typingtutor.entity.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Regression tests for JwtAuthFilter's email-verification gate.
 *
 * Bug: the filter blocked every request for users whose emailVerified=false,
 * including users who have no email at all.
 * Fix: the block only applies when the user has a non-blank email that hasn't
 * been verified yet.
 */
@ExtendWith(MockitoExtension.class)
class JwtAuthFilterEmailTest {

    @Mock JwtUtil jwtUtil;
    @Mock UserDetailsServiceImpl userDetailsService;
    @Mock HttpServletRequest request;
    @Mock HttpServletResponse response;
    @Mock FilterChain filterChain;

    @InjectMocks JwtAuthFilter filter;

    private StringWriter responseBody;

    @BeforeEach
    void setUp() throws Exception {
        SecurityContextHolder.clearContext();
        responseBody = new StringWriter();
        when(request.getHeader("Authorization")).thenReturn("Bearer test-token");
        when(jwtUtil.extractUsername("test-token")).thenReturn("testuser");
        // lenient: authEndpoint test overrides this with its own stub
        lenient().when(request.getRequestURI()).thenReturn("/api/lessons");
    }

    @Test
    void noEmailUser_requestPassesThrough() throws Exception {
        UserPrincipal principal = principalFor("", false);
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(principal);
        when(jwtUtil.isTokenValid(any(), any())).thenReturn(true);

        filter.doFilterInternal(request, response, filterChain);

        // Filter chain must proceed — not blocked
        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(HttpServletResponse.SC_FORBIDDEN);
    }

    @Test
    void nullEmailUser_requestPassesThrough() throws Exception {
        UserPrincipal principal = principalFor(null, false);
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(principal);
        when(jwtUtil.isTokenValid(any(), any())).thenReturn(true);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(HttpServletResponse.SC_FORBIDDEN);
    }

    @Test
    void userWithEmailUnverified_requestBlocked() throws Exception {
        UserPrincipal principal = principalFor("user@example.com", false);
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(principal);
        when(jwtUtil.isTokenValid(any(), any())).thenReturn(true);
        when(response.getWriter()).thenReturn(new PrintWriter(responseBody));

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        assertThat(responseBody.toString()).contains("EMAIL_NOT_VERIFIED");
        // Filter chain must NOT proceed when blocked
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void userWithEmailVerified_requestPassesThrough() throws Exception {
        UserPrincipal principal = principalFor("user@example.com", true);
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(principal);
        when(jwtUtil.isTokenValid(any(), any())).thenReturn(true);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(HttpServletResponse.SC_FORBIDDEN);
    }

    @Test
    void authEndpoint_unverifiedEmailUser_notBlocked() throws Exception {
        // /api/auth/* is always allowed through even for unverified-email users
        when(request.getRequestURI()).thenReturn("/api/auth/me");
        UserPrincipal principal = principalFor("user@example.com", false);
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(principal);
        when(jwtUtil.isTokenValid(any(), any())).thenReturn(true);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(HttpServletResponse.SC_FORBIDDEN);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private UserPrincipal principalFor(String email, boolean emailVerified) {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail(email);
        user.setEmailVerified(emailVerified);
        user.setActive(true);
        user.setRole(Role.USER);
        user.setPasswordChanged(true);
        return new UserPrincipal(user);
    }
}
