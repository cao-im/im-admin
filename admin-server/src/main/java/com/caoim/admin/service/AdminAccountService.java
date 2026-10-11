package com.caoim.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.caoim.admin.common.BusinessException;
import com.caoim.admin.dao.AdminRoleMapper;
import com.caoim.admin.dao.AdminUserMapper;
import com.caoim.admin.dao.AdminUserRoleMapper;
import com.caoim.admin.entity.AdminRole;
import com.caoim.admin.entity.AdminUser;
import com.caoim.admin.entity.AdminUserRole;
import com.caoim.admin.security.AdminHolder;
import com.caoim.admin.security.AdminPrincipal;
import com.caoim.imcore.dto.PageResult;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminAccountService {

    private final AdminUserMapper adminUserMapper;
    private final AdminUserRoleMapper adminUserRoleMapper;
    private final AdminRoleMapper adminRoleMapper;
    private final RoleMenuService roleMenuService;
    private final PasswordEncoder passwordEncoder;

    public AdminAccountService(AdminUserMapper adminUserMapper, AdminUserRoleMapper adminUserRoleMapper,
                               AdminRoleMapper adminRoleMapper, RoleMenuService roleMenuService,
                               PasswordEncoder passwordEncoder) {
        this.adminUserMapper = adminUserMapper;
        this.adminUserRoleMapper = adminUserRoleMapper;
        this.adminRoleMapper = adminRoleMapper;
        this.roleMenuService = roleMenuService;
        this.passwordEncoder = passwordEncoder;
    }

    public AdminPrincipal loadByUsername(String username) {
        AdminUser user = adminUserMapper.selectOne(new LambdaQueryWrapper<AdminUser>().eq(AdminUser::getUsername, username));
        if (user == null) {
            return null;
        }
        return toPrincipal(user);
    }

    public AdminUser requireByUsername(String username) {
        AdminUser user = adminUserMapper.selectOne(new LambdaQueryWrapper<AdminUser>().eq(AdminUser::getUsername, username));
        if (user == null) {
            throw new BusinessException(401, "账号或密码错误");
        }
        return user;
    }

    public PageResult<Map<String, Object>> page(String keyword, int page, int size) {
        LambdaQueryWrapper<AdminUser> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            String value = keyword.trim();
            wrapper.and(query -> query.like(AdminUser::getUsername, value).or().like(AdminUser::getNickname, value));
        }
        wrapper.orderByDesc(AdminUser::getId);
        Page<AdminUser> result = adminUserMapper.selectPage(new Page<>(Math.max(page, 1), clamp(size)), wrapper);
        List<Map<String, Object>> records = result.getRecords().stream().map(this::toView).toList();
        PageResult<Map<String, Object>> pageResult = new PageResult<>();
        pageResult.setTotal(result.getTotal());
        pageResult.setPage(result.getCurrent());
        pageResult.setSize(result.getSize());
        pageResult.setRecords(records);
        return pageResult;
    }

    @Transactional
    public AdminUser create(String username, String password, String nickname, Integer superAdmin, List<Long> roleIds) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new BusinessException(400, "账号和密码不能为空");
        }
        if (password.length() < 6) {
            throw new BusinessException(400, "密码至少 6 位");
        }
        Long count = adminUserMapper.selectCount(new LambdaQueryWrapper<AdminUser>().eq(AdminUser::getUsername, username.trim()));
        if (count != null && count > 0) {
            throw new BusinessException(400, "账号已存在");
        }
        boolean makeSuper = superAdmin != null && superAdmin == 1;
        if (makeSuper && !AdminHolder.current().isSuperAdmin()) {
            throw new BusinessException(403, "不能创建超级管理员");
        }
        roleMenuService.assertCanAssign(roleIds);
        AdminUser user = new AdminUser();
        user.setUsername(username.trim());
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(nickname == null || nickname.isBlank() ? username.trim() : nickname.trim());
        user.setStatus(1);
        user.setSuperAdmin(makeSuper ? 1 : 0);
        user.setDeleted(0);
        adminUserMapper.insert(user);
        replaceRoles(user.getId(), roleIds);
        return user;
    }

    public AdminUser update(Long id, String nickname, Integer superAdmin) {
        AdminUser user = require(id);
        if (nickname != null && !nickname.isBlank()) {
            user.setNickname(nickname.trim());
        }
        if (superAdmin != null) {
            if (!AdminHolder.current().isSuperAdmin()) {
                throw new BusinessException(403, "不能修改超级管理员标记");
            }
            if (user.getSuperAdmin() != null && user.getSuperAdmin() == 1 && superAdmin == 0 && countSuper() <= 1) {
                throw new BusinessException(400, "至少保留一个超级管理员");
            }
            user.setSuperAdmin(superAdmin == 1 ? 1 : 0);
        }
        adminUserMapper.updateById(user);
        return user;
    }

    public void setStatus(Long id, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(400, "状态无效");
        }
        AdminUser user = require(id);
        if (status == 0) {
            if (user.getId().equals(AdminHolder.current().getId())) {
                throw new BusinessException(400, "不能停用自己");
            }
            if (user.getSuperAdmin() != null && user.getSuperAdmin() == 1 && countSuper() <= 1) {
                throw new BusinessException(400, "至少保留一个超级管理员");
            }
        }
        user.setStatus(status);
        adminUserMapper.updateById(user);
    }

    public void resetPassword(Long id, String password) {
        if (password == null || password.length() < 6) {
            throw new BusinessException(400, "密码至少 6 位");
        }
        AdminUser user = require(id);
        user.setPassword(passwordEncoder.encode(password));
        adminUserMapper.updateById(user);
    }

    public void changeOwnPassword(String oldPassword, String newPassword) {
        if (newPassword == null || newPassword.length() < 6) {
            throw new BusinessException(400, "新密码至少 6 位");
        }
        AdminUser user = require(AdminHolder.current().getId());
        if (!passwordEncoder.matches(oldPassword == null ? "" : oldPassword, user.getPassword())) {
            throw new BusinessException(400, "原密码错误");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        adminUserMapper.updateById(user);
    }

    @Transactional
    public void assignRoles(Long id, List<Long> roleIds) {
        require(id);
        roleMenuService.assertCanAssign(roleIds);
        replaceRoles(id, roleIds);
    }

    public void delete(Long id) {
        AdminUser user = require(id);
        if (user.getId().equals(AdminHolder.current().getId())) {
            throw new BusinessException(400, "不能删除自己");
        }
        if (user.getSuperAdmin() != null && user.getSuperAdmin() == 1 && countSuper() <= 1) {
            throw new BusinessException(400, "至少保留一个超级管理员");
        }
        adminUserRoleMapper.delete(new LambdaQueryWrapper<AdminUserRole>().eq(AdminUserRole::getUserId, id));
        adminUserMapper.deleteById(id);
    }

    public List<Long> roleIds(Long userId) {
        return adminUserRoleMapper.selectList(new LambdaQueryWrapper<AdminUserRole>().eq(AdminUserRole::getUserId, userId))
                .stream().map(AdminUserRole::getRoleId).toList();
    }

    public Map<String, Object> profile(AdminUser user) {
        boolean superAdmin = user.getSuperAdmin() != null && user.getSuperAdmin() == 1;
        Map<String, Object> profile = new HashMap<>();
        profile.put("id", user.getId());
        profile.put("username", user.getUsername());
        profile.put("nickname", user.getNickname());
        profile.put("superAdmin", superAdmin);
        profile.put("permissions", roleMenuService.permissionsOf(user.getId(), superAdmin));
        profile.put("menus", roleMenuService.sidebar(user.getId(), superAdmin));
        profile.put("roleIds", roleIds(user.getId()));
        return profile;
    }

    private AdminPrincipal toPrincipal(AdminUser user) {
        boolean superAdmin = user.getSuperAdmin() != null && user.getSuperAdmin() == 1;
        boolean enabled = user.getStatus() == null || user.getStatus() == 1;
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        if (superAdmin) {
            authorities.add(new SimpleGrantedAuthority("ROLE_SUPER"));
        }
        for (String permission : roleMenuService.permissionsOf(user.getId(), superAdmin)) {
            authorities.add(new SimpleGrantedAuthority(permission));
        }
        return new AdminPrincipal(user.getId(), user.getUsername(), user.getPassword(), user.getNickname(),
                superAdmin, enabled, authorities);
    }

    private Map<String, Object> toView(AdminUser user) {
        Map<String, Object> view = new HashMap<>();
        view.put("id", user.getId());
        view.put("username", user.getUsername());
        view.put("nickname", user.getNickname());
        view.put("status", user.getStatus());
        view.put("superAdmin", user.getSuperAdmin());
        view.put("createTime", user.getCreateTime());
        List<Long> roleIds = roleIds(user.getId());
        view.put("roleIds", roleIds);
        if (roleIds.isEmpty()) {
            view.put("roleNames", List.of());
        } else {
            view.put("roleNames", adminRoleMapper.selectBatchIds(roleIds).stream().map(AdminRole::getName).toList());
        }
        return view;
    }

    private void replaceRoles(Long userId, List<Long> roleIds) {
        adminUserRoleMapper.delete(new LambdaQueryWrapper<AdminUserRole>().eq(AdminUserRole::getUserId, userId));
        if (roleIds == null) {
            return;
        }
        for (Long roleId : roleIds.stream().distinct().toList()) {
            if (adminRoleMapper.selectById(roleId) == null) {
                continue;
            }
            AdminUserRole link = new AdminUserRole();
            link.setUserId(userId);
            link.setRoleId(roleId);
            adminUserRoleMapper.insert(link);
        }
    }

    private AdminUser require(Long id) {
        AdminUser user = adminUserMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(404, "管理员不存在");
        }
        return user;
    }

    private long countSuper() {
        Long count = adminUserMapper.selectCount(new LambdaQueryWrapper<AdminUser>().eq(AdminUser::getSuperAdmin, 1).eq(AdminUser::getStatus, 1));
        return count == null ? 0 : count;
    }

    private int clamp(int size) {
        if (size < 1) {
            return 20;
        }
        return Math.min(size, 100);
    }
}
