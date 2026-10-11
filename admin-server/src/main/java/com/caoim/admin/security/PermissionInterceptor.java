package com.caoim.admin.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class PermissionInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod method)) {
            return true;
        }
        RequirePermission permission = method.getMethodAnnotation(RequirePermission.class);
        if (permission == null) {
            return true;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            write(response, 401, "未登录");
            return false;
        }
        String required = permission.value();
        boolean allowed = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> "ROLE_SUPER".equals(authority) || required.equals(authority));
        if (!allowed) {
            write(response, 403, "没有权限");
            return false;
        }
        return true;
    }

    private void write(HttpServletResponse response, int code, String message) throws Exception {
        response.setStatus(200);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":" + code + ",\"message\":\"" + message + "\",\"data\":null}");
    }
}
