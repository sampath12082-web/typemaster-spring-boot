package com.typingtutor.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

class JwtAuthFilterTest {

    @Mock HttpServletRequest request;
    @Mock HttpServletResponse response;
    @Mock FilterChain filterChain;
    @Mock JwtUtil jwtUtil;
    @Mock UserDetailsServiceImpl userDetailsService;

    @InjectMocks JwtAuthFilter jwtAuthFilter;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void invalidTokenReturnsUnauthorized() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer badtoken");
        doThrow(new io.jsonwebtoken.JwtException("Invalid"))
                .when(jwtUtil).extractUsername("badtoken");

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
        verify(filterChain, never()).doFilter(request, response);
    }
}
