package br.com.uds.tools.ged.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldNotFilter_whenPathIsApiAuth_returnsTrue() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/auth/login");
        assertThat(filter.shouldNotFilter(request)).isTrue();
    }

    @Test
    void shouldNotFilter_whenPathIsApiSetup_returnsTrue() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/setup/initial-admin");
        assertThat(filter.shouldNotFilter(request)).isTrue();
    }

    @Test
    void shouldNotFilter_whenPathIsApiDocuments_returnsFalse() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/documents");
        assertThat(filter.shouldNotFilter(request)).isFalse();
    }

    @Test
    void doFilterInternal_whenNoAuthorizationHeader_continuesChain() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(same(request), same(response));
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    void doFilterInternal_whenAuthorizationNotBearer_continuesChain() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Basic xyz");

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(same(request), same(response));
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    void doFilterInternal_whenValidToken_setsAuthenticationAndContinuesChain() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
        when(jwtService.getUsername("valid-token")).thenReturn("admin");
        when(jwtService.getUserId("valid-token")).thenReturn(1L);
        when(jwtService.getRole("valid-token")).thenReturn(br.com.uds.tools.ged.domain.Role.ADMIN);

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(same(request), same(response));
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("admin");
    }

    @Test
    void doFilterInternal_whenInvalidToken_returns401AndDoesNotContinueChain() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid-token");
        when(jwtService.getUsername("invalid-token")).thenThrow(new RuntimeException("Invalid JWT"));
        StringWriter writer = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(writer));

        filter.doFilter(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType("application/json");
        assertThat(writer.toString()).contains("Token inválido ou expirado");
        verify(filterChain, never()).doFilter(any(), any());
    }
}
