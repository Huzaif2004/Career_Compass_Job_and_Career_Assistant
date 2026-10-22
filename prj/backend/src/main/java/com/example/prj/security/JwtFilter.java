package com.example.prj.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Override
protected void doFilterInternal(HttpServletRequest request,
                                HttpServletResponse response,
                                FilterChain filterChain)
        throws ServletException, IOException {
    System.out.println("JwtFilter triggered for path: " + request.getServletPath());
    String path = request.getServletPath();

    // Public endpoints
    if (path.equals("/signup") || path.equals("/login")||path.equals("/test-embed")) {
        filterChain.doFilter(request, response);
        return;
    }

    // Token required for all other endpoints
    String header = request.getHeader("Authorization");

    if (header == null || !header.startsWith("Bearer ")) {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.getWriter().write("Missing or invalid Authorization token");
        return;
    }

    String token = header.substring(7);

    if (jwtUtil.isTokenValid(token)) {
        String email = jwtUtil.extractEmail(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );

        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    filterChain.doFilter(request, response);
}


}
