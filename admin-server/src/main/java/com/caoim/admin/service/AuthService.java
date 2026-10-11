package com.caoim.admin.service;

import com.caoim.admin.common.BusinessException;
import com.caoim.admin.entity.AdminUser;
import com.caoim.admin.security.AdminHolder;
import com.caoim.admin.security.AdminJwtUtil;
import com.caoim.admin.security.AdminPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    private final AdminAccountService adminAccountService;
    private final PasswordEncoder passwordEncoder;
    private final AdminJwtUtil adminJwtUtil;
    private final AuditService auditService;

    public AuthService(AdminAccountService adminAccountService, PasswordEncoder passwordEncoder,
                       AdminJwtUtil adminJwtUtil, AuditService auditService) {
        this.adminAccountService = adminAccountService;
        this.passwordEncoder = passwordEncoder;
        this.adminJwtUtil = adminJwtUtil;
        this.auditService = auditService;
    }

    public Map<String, Object> login(String username, String password, HttpServletRequest request) {
        AdminUser user;
        try {
            user = adminAccountService.requireByUsername(username);
        } catch (BusinessException e) {
            auditService.recordLogin(request, null, username, false, "账号不存在");
            throw e;
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            auditService.recordLogin(request, user.getId(), user.getUsername(), false, "账号已停用");
            throw new BusinessException(403, "账号已停用");
        }
        if (!passwordEncoder.matches(password == null ? "" : password, user.getPassword())) {
            auditService.recordLogin(request, user.getId(), user.getUsername(), false, "密码错误");
            throw new BusinessException(401, "账号或密码错误");
        }
        boolean superAdmin = user.getSuperAdmin() != null && user.getSuperAdmin() == 1;
        String token = adminJwtUtil.generate(user.getId(), user.getUsername(), superAdmin);
        auditService.recordLogin(request, user.getId(), user.getUsername(), true, "登录成功");
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("profile", adminAccountService.profile(user));
        return result;
    }

    public Map<String, Object> me() {
        AdminPrincipal principal = AdminHolder.current();
        AdminUser user = adminAccountService.requireByUsername(principal.getUsername());
        return adminAccountService.profile(user);
    }
}
