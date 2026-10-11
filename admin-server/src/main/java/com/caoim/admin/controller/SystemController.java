package com.caoim.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.caoim.admin.dao.AdminAuditLogMapper;
import com.caoim.admin.entity.AdminAuditLog;
import com.caoim.admin.entity.AdminMenu;
import com.caoim.admin.entity.AdminRole;
import com.caoim.admin.entity.AdminUser;
import com.caoim.admin.security.RequirePermission;
import com.caoim.admin.service.AdminAccountService;
import com.caoim.admin.service.AuditService;
import com.caoim.admin.service.RoleMenuService;
import com.caoim.admin.dto.MenuNode;
import com.caoim.imcore.common.Result;
import com.caoim.imcore.dto.PageResult;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/system")
public class SystemController {

    private final AdminAccountService adminAccountService;
    private final RoleMenuService roleMenuService;
    private final AuditService auditService;
    private final AdminAuditLogMapper adminAuditLogMapper;

    public SystemController(AdminAccountService adminAccountService, RoleMenuService roleMenuService,
                            AuditService auditService, AdminAuditLogMapper adminAuditLogMapper) {
        this.adminAccountService = adminAccountService;
        this.roleMenuService = roleMenuService;
        this.auditService = auditService;
        this.adminAuditLogMapper = adminAuditLogMapper;
    }

    @GetMapping("/admins")
    @RequirePermission("system:admin:list")
    public Result<PageResult<Map<String, Object>>> admins(@RequestParam(required = false) String keyword,
                                                          @RequestParam(defaultValue = "1") int page,
                                                          @RequestParam(defaultValue = "20") int size) {
        return Result.success(adminAccountService.page(keyword, page, size));
    }

    @PostMapping("/admins")
    @RequirePermission("system:admin:edit")
    public Result<AdminUser> createAdmin(@RequestBody AdminRequest request, HttpServletRequest httpRequest) {
        AdminUser user = adminAccountService.create(request.getUsername(), request.getPassword(), request.getNickname(), request.getSuperAdmin(), request.getRoleIds());
        auditService.record(httpRequest, "新增管理员", "ADMIN", String.valueOf(user.getId()), user.getUsername(), true);
        user.setPassword(null);
        return Result.success(user);
    }

    @PutMapping("/admins/{id}")
    @RequirePermission("system:admin:edit")
    public Result<AdminUser> updateAdmin(@PathVariable Long id, @RequestBody AdminRequest request, HttpServletRequest httpRequest) {
        AdminUser user = adminAccountService.update(id, request.getNickname(), request.getSuperAdmin());
        auditService.record(httpRequest, "修改管理员", "ADMIN", String.valueOf(id), user.getUsername(), true);
        user.setPassword(null);
        return Result.success(user);
    }

    @PutMapping("/admins/{id}/status")
    @RequirePermission("system:admin:edit")
    public Result<Void> adminStatus(@PathVariable Long id, @RequestParam Integer status, HttpServletRequest request) {
        adminAccountService.setStatus(id, status);
        auditService.record(request, status != null && status == 0 ? "停用管理员" : "启用管理员", "ADMIN", String.valueOf(id), "", true);
        return Result.success();
    }

    @PutMapping("/admins/{id}/password")
    @RequirePermission("system:admin:edit")
    public Result<Void> resetPassword(@PathVariable Long id, @RequestBody AdminRequest request, HttpServletRequest httpRequest) {
        adminAccountService.resetPassword(id, request.getPassword());
        auditService.record(httpRequest, "重置密码", "ADMIN", String.valueOf(id), "", true);
        return Result.success();
    }

    @PutMapping("/admins/{id}/roles")
    @RequirePermission("system:admin:edit")
    public Result<Void> assignRoles(@PathVariable Long id, @RequestBody AdminRequest request, HttpServletRequest httpRequest) {
        adminAccountService.assignRoles(id, request.getRoleIds());
        auditService.record(httpRequest, "分配角色", "ADMIN", String.valueOf(id), String.valueOf(request.getRoleIds()), true);
        return Result.success();
    }

    @DeleteMapping("/admins/{id}")
    @RequirePermission("system:admin:edit")
    public Result<Void> deleteAdmin(@PathVariable Long id, HttpServletRequest request) {
        adminAccountService.delete(id);
        auditService.record(request, "删除管理员", "ADMIN", String.valueOf(id), "", true);
        return Result.success();
    }

    @GetMapping("/roles")
    @RequirePermission("system:role:list")
    public Result<List<AdminRole>> roles() {
        return Result.success(roleMenuService.listRoles());
    }

    @PostMapping("/roles")
    @RequirePermission("system:role:edit")
    public Result<AdminRole> createRole(@RequestBody RoleRequest request, HttpServletRequest httpRequest) {
        AdminRole role = roleMenuService.createRole(request.getCode(), request.getName(), request.getRemark());
        auditService.record(httpRequest, "新增角色", "ROLE", String.valueOf(role.getId()), role.getName(), true);
        return Result.success(role);
    }

    @PutMapping("/roles/{id}")
    @RequirePermission("system:role:edit")
    public Result<AdminRole> updateRole(@PathVariable Long id, @RequestBody RoleRequest request, HttpServletRequest httpRequest) {
        AdminRole role = roleMenuService.updateRole(id, request.getName(), request.getRemark());
        auditService.record(httpRequest, "修改角色", "ROLE", String.valueOf(id), role.getName(), true);
        return Result.success(role);
    }

    @GetMapping("/roles/{id}/menus")
    @RequirePermission("system:role:list")
    public Result<List<Long>> roleMenus(@PathVariable Long id) {
        return Result.success(roleMenuService.roleMenuIds(id));
    }

    @PutMapping("/roles/{id}/menus")
    @RequirePermission("system:role:edit")
    public Result<Void> setRoleMenus(@PathVariable Long id, @RequestBody RoleRequest request, HttpServletRequest httpRequest) {
        roleMenuService.setRoleMenus(id, request.getMenuIds());
        auditService.record(httpRequest, "分配菜单", "ROLE", String.valueOf(id), "", true);
        return Result.success();
    }

    @DeleteMapping("/roles/{id}")
    @RequirePermission("system:role:edit")
    public Result<Void> deleteRole(@PathVariable Long id, HttpServletRequest request) {
        roleMenuService.deleteRole(id);
        auditService.record(request, "删除角色", "ROLE", String.valueOf(id), "", true);
        return Result.success();
    }

    @GetMapping("/menus")
    @RequirePermission("system:menu:list")
    public Result<List<MenuNode>> menus() {
        return Result.success(roleMenuService.menuTree());
    }

    @PostMapping("/menus")
    @RequirePermission("system:menu:edit")
    public Result<AdminMenu> createMenu(@RequestBody AdminMenu request, HttpServletRequest httpRequest) {
        AdminMenu menu = roleMenuService.createMenu(request);
        auditService.record(httpRequest, "新增菜单", "MENU", String.valueOf(menu.getId()), menu.getName(), true);
        return Result.success(menu);
    }

    @PutMapping("/menus/{id}")
    @RequirePermission("system:menu:edit")
    public Result<AdminMenu> updateMenu(@PathVariable Long id, @RequestBody AdminMenu request, HttpServletRequest httpRequest) {
        AdminMenu menu = roleMenuService.updateMenu(id, request);
        auditService.record(httpRequest, "修改菜单", "MENU", String.valueOf(id), menu.getName(), true);
        return Result.success(menu);
    }

    @DeleteMapping("/menus/{id}")
    @RequirePermission("system:menu:edit")
    public Result<Void> deleteMenu(@PathVariable Long id, HttpServletRequest request) {
        roleMenuService.deleteMenu(id);
        auditService.record(request, "删除菜单", "MENU", String.valueOf(id), "", true);
        return Result.success();
    }

    @GetMapping("/audits")
    @RequirePermission("system:audit:list")
    public Result<PageResult<AdminAuditLog>> audits(@RequestParam(required = false) String keyword,
                                                    @RequestParam(defaultValue = "1") int page,
                                                    @RequestParam(defaultValue = "20") int size) {
        LambdaQueryWrapper<AdminAuditLog> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            String value = keyword.trim();
            wrapper.and(query -> query.like(AdminAuditLog::getAdminName, value).or().like(AdminAuditLog::getAction, value));
        }
        wrapper.orderByDesc(AdminAuditLog::getId);
        int pageNo = Math.max(page, 1);
        int pageSize = size < 1 ? 20 : Math.min(size, 100);
        Page<AdminAuditLog> result = adminAuditLogMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
        PageResult<AdminAuditLog> pageResult = new PageResult<>();
        pageResult.setTotal(result.getTotal());
        pageResult.setPage(result.getCurrent());
        pageResult.setSize(result.getSize());
        pageResult.setRecords(result.getRecords());
        return Result.success(pageResult);
    }

    @Data
    public static class AdminRequest {
        private String username;
        private String password;
        private String nickname;
        private Integer superAdmin;
        private List<Long> roleIds;
    }

    @Data
    public static class RoleRequest {
        private String code;
        private String name;
        private String remark;
        private List<Long> menuIds;
    }
}
