package com.caoim.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("admin_role_menu")
public class AdminRoleMenu {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long roleId;
    private Long menuId;
}
