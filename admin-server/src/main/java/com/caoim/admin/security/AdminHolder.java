package com.caoim.admin.security;

import com.caoim.admin.common.BusinessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class AdminHolder {
    private AdminHolder() {
    }

    public static AdminPrincipal current() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AdminPrincipal principal) {
            return principal;
        }
        throw new BusinessException(401, "未登录");
    }
}
