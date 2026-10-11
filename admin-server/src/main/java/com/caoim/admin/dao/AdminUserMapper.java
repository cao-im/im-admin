package com.caoim.admin.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.caoim.admin.entity.AdminUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AdminUserMapper extends BaseMapper<AdminUser> {
}
