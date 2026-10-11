package com.caoim.admin.service;

import com.caoim.admin.dao.AdminAuditLogMapper;
import com.caoim.admin.entity.AdminAuditLog;
import com.caoim.admin.security.AdminHolder;
import com.caoim.admin.security.AdminPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    private final AdminAuditLogMapper adminAuditLogMapper;

    public AuditService(AdminAuditLogMapper adminAuditLogMapper) {
        this.adminAuditLogMapper = adminAuditLogMapper;
    }

    public void record(HttpServletRequest request, String action, String targetType, String targetId, String detail, boolean success) {
        AdminAuditLog log = new AdminAuditLog();
        try {
            AdminPrincipal principal = AdminHolder.current();
            log.setAdminId(principal.getId());
            log.setAdminName(principal.getUsername());
        } catch (Exception ignored) {
            log.setAdminName("");
        }
        log.setAction(action);
        log.setTargetType(targetType);
        log.setTargetId(targetId == null ? "" : targetId);
        log.setDetail(trim(detail));
        log.setIp(clientIp(request));
        log.setResult(success ? 1 : 0);
        adminAuditLogMapper.insert(log);
    }

    public void recordLogin(HttpServletRequest request, Long adminId, String username, boolean success, String detail) {
        AdminAuditLog log = new AdminAuditLog();
        log.setAdminId(adminId);
        log.setAdminName(username == null ? "" : username);
        log.setAction(success ? "登录" : "登录失败");
        log.setTargetType("ADMIN");
        log.setTargetId(adminId == null ? "" : String.valueOf(adminId));
        log.setDetail(trim(detail));
        log.setIp(clientIp(request));
        log.setResult(success ? 1 : 0);
        adminAuditLogMapper.insert(log);
    }

    private String clientIp(HttpServletRequest request) {
        if (request == null) {
            return "";
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String trim(String detail) {
        if (detail == null) {
            return "";
        }
        return detail.length() > 500 ? detail.substring(0, 500) : detail;
    }
}
