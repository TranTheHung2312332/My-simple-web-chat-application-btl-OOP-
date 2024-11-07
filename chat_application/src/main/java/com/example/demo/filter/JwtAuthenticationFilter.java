package com.example.demo.filter;

import com.example.demo.handler.AppException;
import com.example.demo.service.AuthenticationService;
import com.nimbusds.jose.JOSEException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;

import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.text.ParseException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final AuthenticationService authService;

    public JwtAuthenticationFilter(AuthenticationService authService) {
        this.authService = authService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if(authHeader != null && authHeader.startsWith("Bearer ")){
            String token = authHeader.substring(7);
            try {
                authService.setAuthentication(token);
            } catch (ParseException | JOSEException e) {
                throw new AppException("Invalid token", HttpStatus.BAD_REQUEST);
            }
        }

        filterChain.doFilter(request, response); // Tiếp tục chuỗi lọc
    }
}
