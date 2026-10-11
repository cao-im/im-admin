package com.caoim.admin.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("admin_audit_log")
public class AdminAuditLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long adminId;
    private String adminName;
    private String action;
    private String targetType;
    private String targetId;
    private String detail;
    private String ip;
    private Integer result;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
