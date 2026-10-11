package com.caoim.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.caoim.admin.dao.AdminMenuMapper;
import com.caoim.admin.dao.AdminRoleMapper;
import com.caoim.admin.dao.AdminRoleMenuMapper;
import com.caoim.admin.dao.AdminUserMapper;
import com.caoim.admin.dao.AdminUserRoleMapper;
import com.caoim.admin.entity.AdminMenu;
import com.caoim.admin.entity.AdminRole;
import com.caoim.admin.entity.AdminRoleMenu;
import com.caoim.admin.entity.AdminUser;
import com.caoim.admin.entity.AdminUserRole;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
@Order(0)
public class DataInitializer implements ApplicationRunner {

    private final AdminUserMapper adminUserMapper;
    private final AdminRoleMapper adminRoleMapper;
    private final AdminMenuMapper adminMenuMapper;
    private final AdminUserRoleMapper adminUserRoleMapper;
    private final AdminRoleMenuMapper adminRoleMenuMapper;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(AdminUserMapper adminUserMapper, AdminRoleMapper adminRoleMapper, AdminMenuMapper adminMenuMapper,
                           AdminUserRoleMapper adminUserRoleMapper, AdminRoleMenuMapper adminRoleMenuMapper,
                           PasswordEncoder passwordEncoder) {
        this.adminUserMapper = adminUserMapper;
        this.adminRoleMapper = adminRoleMapper;
        this.adminMenuMapper = adminMenuMapper;
        this.adminUserRoleMapper = adminUserRoleMapper;
        this.adminRoleMenuMapper = adminRoleMenuMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (adminUserMapper.selectCount(null) > 0) {
            return;
        }
        List<Long> allMenus = new ArrayList<>();
        List<Long> operatorMenus = new ArrayList<>();
        add(allMenus, operatorMenus, menu(0L, "概览", "/dashboard", "dashboard:view", "Odometer", 1, 2), true);
        Long users = add(allMenus, operatorMenus, menu(0L, "用户管理", "/im/users", "im:user:list", "User", 2, 2), true);
        add(allMenus, operatorMenus, menu(users, "启停账号", "", "im:user:enable", "", 1, 3), true);
        Long groups = add(allMenus, operatorMenus, menu(0L, "群组管理", "/im/groups", "im:group:list", "ChatLineRound", 3, 2), true);
        add(allMenus, operatorMenus, menu(groups, "解散群", "", "im:group:dismiss", "", 1, 3), true);
        add(allMenus, operatorMenus, menu(0L, "消息检索", "/im/messages", "im:message:list", "ChatDotRound", 4, 2), true);
        Long words = add(allMenus, operatorMenus, menu(0L, "敏感词", "/im/sensitive", "im:sensitive:list", "Warning", 5, 2), true);
        add(allMenus, operatorMenus, menu(words, "编辑敏感词", "", "im:sensitive:edit", "", 1, 3), false);
        Long config = add(allMenus, operatorMenus, menu(0L, "系统配置", "/im/config", "im:config:list", "Setting", 6, 2), false);
        add(allMenus, operatorMenus, menu(config, "保存配置", "", "im:config:edit", "", 1, 3), false);
        Long admins = add(allMenus, operatorMenus, menu(0L, "管理员", "/system/admins", "system:admin:list", "UserFilled", 7, 2), false);
        add(allMenus, operatorMenus, menu(admins, "编辑管理员", "", "system:admin:edit", "", 1, 3), false);
        Long roles = add(allMenus, operatorMenus, menu(0L, "角色", "/system/roles", "system:role:list", "Stamp", 8, 2), false);
        add(allMenus, operatorMenus, menu(roles, "编辑角色", "", "system:role:edit", "", 1, 3), false);
        Long menus = add(allMenus, operatorMenus, menu(0L, "菜单", "/system/menus", "system:menu:list", "Menu", 9, 2), false);
        add(allMenus, operatorMenus, menu(menus, "编辑菜单", "", "system:menu:edit", "", 1, 3), false);
        add(allMenus, operatorMenus, menu(0L, "审计日志", "/system/audit", "system:audit:list", "Document", 10, 2), true);

        AdminRole superRole = role(RoleMenuService.SUPER_CODE, "超级管理员", "拥有全部权限");
        AdminRole operator = role("operator", "运营", "用户、群、消息和审计");
        link(superRole.getId(), allMenus);
        link(operator.getId(), operatorMenus);

        AdminUser admin = new AdminUser();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setNickname("超级管理员");
        admin.setStatus(1);
        admin.setSuperAdmin(1);
        admin.setDeleted(0);
        adminUserMapper.insert(admin);
        AdminUserRole userRole = new AdminUserRole();
        userRole.setUserId(admin.getId());
        userRole.setRoleId(superRole.getId());
        adminUserRoleMapper.insert(userRole);
    }

    private Long add(List<Long> all, List<Long> operator, AdminMenu menu, boolean forOperator) {
        adminMenuMapper.insert(menu);
        all.add(menu.getId());
        if (forOperator) {
            operator.add(menu.getId());
        }
        return menu.getId();
    }

    private AdminMenu menu(Long parentId, String name, String path, String permission, String icon, int sort, int type) {
        AdminMenu menu = new AdminMenu();
        menu.setParentId(parentId);
        menu.setName(name);
        menu.setPath(path);
        menu.setPermission(permission);
        menu.setIcon(icon);
        menu.setSortOrder(sort);
        menu.setMenuType(type);
        return menu;
    }

    private AdminRole role(String code, String name, String remark) {
        AdminRole existing = adminRoleMapper.selectOne(new LambdaQueryWrapper<AdminRole>().eq(AdminRole::getCode, code));
        if (existing != null) {
            return existing;
        }
        AdminRole role = new AdminRole();
        role.setCode(code);
        role.setName(name);
        role.setRemark(remark);
        adminRoleMapper.insert(role);
        return role;
    }

    private void link(Long roleId, List<Long> menuIds) {
        for (Long menuId : menuIds) {
            AdminRoleMenu link = new AdminRoleMenu();
            link.setRoleId(roleId);
            link.setMenuId(menuId);
            adminRoleMenuMapper.insert(link);
        }
    }
}
