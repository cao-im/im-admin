package com.caoim.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.caoim.admin.common.BusinessException;
import com.caoim.admin.dao.AdminMenuMapper;
import com.caoim.admin.dao.AdminRoleMapper;
import com.caoim.admin.dao.AdminRoleMenuMapper;
import com.caoim.admin.dao.AdminUserRoleMapper;
import com.caoim.admin.dto.MenuNode;
import com.caoim.admin.entity.AdminMenu;
import com.caoim.admin.entity.AdminRole;
import com.caoim.admin.entity.AdminRoleMenu;
import com.caoim.admin.entity.AdminUserRole;
import com.caoim.admin.security.AdminHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class RoleMenuService {

    public static final String SUPER_CODE = "super_admin";

    private final AdminRoleMapper adminRoleMapper;
    private final AdminMenuMapper adminMenuMapper;
    private final AdminRoleMenuMapper adminRoleMenuMapper;
    private final AdminUserRoleMapper adminUserRoleMapper;

    public RoleMenuService(AdminRoleMapper adminRoleMapper, AdminMenuMapper adminMenuMapper,
                           AdminRoleMenuMapper adminRoleMenuMapper, AdminUserRoleMapper adminUserRoleMapper) {
        this.adminRoleMapper = adminRoleMapper;
        this.adminMenuMapper = adminMenuMapper;
        this.adminRoleMenuMapper = adminRoleMenuMapper;
        this.adminUserRoleMapper = adminUserRoleMapper;
    }

    public List<AdminRole> listRoles() {
        return adminRoleMapper.selectList(new LambdaQueryWrapper<AdminRole>().orderByAsc(AdminRole::getId));
    }

    public AdminRole createRole(String code, String name, String remark) {
        requireCode(code);
        if (name == null || name.isBlank()) {
            throw new BusinessException(400, "角色名称不能为空");
        }
        Long count = adminRoleMapper.selectCount(new LambdaQueryWrapper<AdminRole>().eq(AdminRole::getCode, code.trim()));
        if (count != null && count > 0) {
            throw new BusinessException(400, "角色编码已存在");
        }
        AdminRole role = new AdminRole();
        role.setCode(code.trim());
        role.setName(name.trim());
        role.setRemark(remark == null ? "" : remark.trim());
        adminRoleMapper.insert(role);
        return role;
    }

    public AdminRole updateRole(Long id, String name, String remark) {
        AdminRole role = requireRole(id);
        if (name != null && !name.isBlank()) {
            role.setName(name.trim());
        }
        if (remark != null) {
            role.setRemark(remark.trim());
        }
        adminRoleMapper.updateById(role);
        return role;
    }

    @Transactional
    public void deleteRole(Long id) {
        AdminRole role = requireRole(id);
        if (SUPER_CODE.equals(role.getCode())) {
            throw new BusinessException(400, "不能删除超级管理员角色");
        }
        Long used = adminUserRoleMapper.selectCount(new LambdaQueryWrapper<AdminUserRole>().eq(AdminUserRole::getRoleId, id));
        if (used != null && used > 0) {
            throw new BusinessException(400, "角色仍有管理员在使用");
        }
        adminRoleMenuMapper.delete(new LambdaQueryWrapper<AdminRoleMenu>().eq(AdminRoleMenu::getRoleId, id));
        adminRoleMapper.deleteById(id);
    }

    public List<Long> roleMenuIds(Long roleId) {
        requireRole(roleId);
        return adminRoleMenuMapper.selectList(new LambdaQueryWrapper<AdminRoleMenu>().eq(AdminRoleMenu::getRoleId, roleId))
                .stream().map(AdminRoleMenu::getMenuId).toList();
    }

    @Transactional
    public void setRoleMenus(Long roleId, List<Long> menuIds) {
        requireRole(roleId);
        adminRoleMenuMapper.delete(new LambdaQueryWrapper<AdminRoleMenu>().eq(AdminRoleMenu::getRoleId, roleId));
        if (menuIds == null) {
            return;
        }
        Set<Long> unique = new HashSet<>(menuIds);
        for (Long menuId : unique) {
            if (adminMenuMapper.selectById(menuId) == null) {
                continue;
            }
            AdminRoleMenu link = new AdminRoleMenu();
            link.setRoleId(roleId);
            link.setMenuId(menuId);
            adminRoleMenuMapper.insert(link);
        }
    }

    public List<MenuNode> menuTree() {
        List<AdminMenu> menus = adminMenuMapper.selectList(new LambdaQueryWrapper<AdminMenu>().orderByAsc(AdminMenu::getSortOrder).orderByAsc(AdminMenu::getId));
        return buildTree(menus, 0L);
    }

    public AdminMenu createMenu(AdminMenu request) {
        if (request.getName() == null || request.getName().isBlank()) {
            throw new BusinessException(400, "菜单名称不能为空");
        }
        AdminMenu menu = new AdminMenu();
        fillMenu(menu, request);
        adminMenuMapper.insert(menu);
        return menu;
    }

    public AdminMenu updateMenu(Long id, AdminMenu request) {
        AdminMenu menu = requireMenu(id);
        fillMenu(menu, request);
        adminMenuMapper.updateById(menu);
        return menu;
    }

    @Transactional
    public void deleteMenu(Long id) {
        requireMenu(id);
        Long children = adminMenuMapper.selectCount(new LambdaQueryWrapper<AdminMenu>().eq(AdminMenu::getParentId, id));
        if (children != null && children > 0) {
            throw new BusinessException(400, "请先删除子菜单");
        }
        adminRoleMenuMapper.delete(new LambdaQueryWrapper<AdminRoleMenu>().eq(AdminRoleMenu::getMenuId, id));
        adminMenuMapper.deleteById(id);
    }

    public List<String> permissionsOf(Long adminId, boolean superAdmin) {
        List<AdminMenu> menus = visibleMenus(adminId, superAdmin);
        return menus.stream()
                .map(AdminMenu::getPermission)
                .filter(permission -> permission != null && !permission.isBlank())
                .distinct()
                .toList();
    }

    public List<MenuNode> sidebar(Long adminId, boolean superAdmin) {
        List<AdminMenu> menus = visibleMenus(adminId, superAdmin).stream()
                .filter(menu -> menu.getMenuType() == null || menu.getMenuType() != 3)
                .toList();
        return buildTree(menus, 0L);
    }

    public void assertCanAssign(List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty() || AdminHolder.current().isSuperAdmin()) {
            return;
        }
        for (Long roleId : roleIds) {
            AdminRole role = adminRoleMapper.selectById(roleId);
            if (role != null && SUPER_CODE.equals(role.getCode())) {
                throw new BusinessException(403, "不能分配超级管理员角色");
            }
        }
    }

    private List<AdminMenu> visibleMenus(Long adminId, boolean superAdmin) {
        List<AdminMenu> all = adminMenuMapper.selectList(null);
        if (superAdmin) {
            return all;
        }
        List<Long> roleIds = adminUserRoleMapper.selectList(new LambdaQueryWrapper<AdminUserRole>().eq(AdminUserRole::getUserId, adminId))
                .stream().map(AdminUserRole::getRoleId).toList();
        if (roleIds.isEmpty()) {
            return List.of();
        }
        Set<Long> menuIds = new HashSet<>(adminRoleMenuMapper.selectList(new LambdaQueryWrapper<AdminRoleMenu>().in(AdminRoleMenu::getRoleId, roleIds))
                .stream().map(AdminRoleMenu::getMenuId).toList());
        return all.stream().filter(menu -> menuIds.contains(menu.getId())).toList();
    }

    private List<MenuNode> buildTree(List<AdminMenu> menus, Long parentId) {
        List<MenuNode> nodes = new ArrayList<>();
        menus.stream()
                .filter(menu -> parentId.equals(menu.getParentId() == null ? 0L : menu.getParentId()))
                .sorted(Comparator.comparing(menu -> menu.getSortOrder() == null ? 0 : menu.getSortOrder()))
                .forEach(menu -> {
                    MenuNode node = new MenuNode();
                    node.setId(menu.getId());
                    node.setParentId(menu.getParentId());
                    node.setName(menu.getName());
                    node.setPath(menu.getPath());
                    node.setPermission(menu.getPermission());
                    node.setIcon(menu.getIcon());
                    node.setMenuType(menu.getMenuType());
                    node.setSortOrder(menu.getSortOrder());
                    node.setChildren(buildTree(menus, menu.getId()));
                    nodes.add(node);
                });
        return nodes;
    }

    private void fillMenu(AdminMenu menu, AdminMenu request) {
        menu.setParentId(request.getParentId() == null ? 0L : request.getParentId());
        menu.setName(request.getName().trim());
        menu.setPath(request.getPath() == null ? "" : request.getPath().trim());
        menu.setPermission(request.getPermission() == null ? "" : request.getPermission().trim());
        menu.setIcon(request.getIcon() == null ? "" : request.getIcon().trim());
        menu.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        menu.setMenuType(request.getMenuType() == null ? 2 : request.getMenuType());
    }

    private AdminRole requireRole(Long id) {
        AdminRole role = adminRoleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(404, "角色不存在");
        }
        return role;
    }

    private AdminMenu requireMenu(Long id) {
        AdminMenu menu = adminMenuMapper.selectById(id);
        if (menu == null) {
            throw new BusinessException(404, "菜单不存在");
        }
        return menu;
    }

    private void requireCode(String code) {
        if (code == null || code.isBlank()) {
            throw new BusinessException(400, "角色编码不能为空");
        }
    }
}
