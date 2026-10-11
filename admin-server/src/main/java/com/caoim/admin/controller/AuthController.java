package com.caoim.admin.controller;

import com.caoim.admin.security.AdminHolder;
import com.caoim.admin.service.AdminAccountService;
import com.caoim.admin.service.AuditService;
import com.caoim.admin.service.AuthService;
import com.caoim.imcore.common.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final AdminAccountService adminAccountService;
    private final AuditService auditService;

    public AuthController(AuthService authService, AdminAccountService adminAccountService, AuditService auditService) {
        this.authService = authService;
        this.adminAccountService = adminAccountService;
        this.auditService = auditService;
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        return Result.success(authService.login(request.getUsername(), request.getPassword(), httpRequest));
    }

    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        auditService.record(request, "退出", "ADMIN", String.valueOf(AdminHolder.current().getId()), "", true);
        return Result.success();
    }

    @GetMapping("/me")
    public Result<Map<String, Object>> me() {
        return Result.success(authService.me());
    }

    @PutMapping("/password")
    public Result<Void> password(@RequestBody PasswordRequest request, HttpServletRequest httpRequest) {
        adminAccountService.changeOwnPassword(request.getOldPassword(), request.getNewPassword());
        auditService.record(httpRequest, "修改密码", "ADMIN", String.valueOf(AdminHolder.current().getId()), "", true);
        return Result.success();
    }

    @Data
    public static class LoginRequest {
        @NotBlank(message = "请输入账号")
        private String username;
        @NotBlank(message = "请输入密码")
        private String password;
    }

    @Data
    public static class PasswordRequest {
        private String oldPassword;
        private String newPassword;
    }
}
