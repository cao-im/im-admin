package com.caoim.admin.security;

import com.caoim.admin.service.AdminAccountService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final AdminJwtUtil adminJwtUtil;
    private final AdminAccountService adminAccountService;

    public JwtAuthenticationFilter(AdminJwtUtil adminJwtUtil, AdminAccountService adminAccountService) {
        this.adminJwtUtil = adminJwtUtil;
        this.adminAccountService = adminAccountService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7).trim();
            if (adminJwtUtil.valid(token)) {
                AdminPrincipal principal = adminAccountService.loadByUsername(adminJwtUtil.username(token));
                if (principal != null && principal.isEnabled()) {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            principal, null, principal.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else if (principal != null) {
                    write(response, 403, "账号已停用");
                    return;
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    private void write(HttpServletResponse response, int code, String message) throws IOException {
        response.setStatus(200);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":" + code + ",\"message\":\"" + message + "\",\"data\":null}");
    }
}
