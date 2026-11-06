package com.ylf.cms.security.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ylf.cms.security.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 系统用户Mapper（M1 内部，禁止被其他模块import）
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 查询用户角色编码列表
     */
    @Select("SELECT r.code FROM sys_role r " +
            "JOIN sys_user_role ur ON ur.role_id = r.id " +
            "WHERE ur.user_id = #{userId} AND r.deleted = 0")
    List<String> findRoleCodes(@Param("userId") Long userId);

    /**
     * 查询用户权限编码列表
     */
    @Select("SELECT p.code FROM sys_permission p " +
            "JOIN sys_role_permission rp ON rp.permission_id = p.id " +
            "JOIN sys_user_role ur ON ur.role_id = rp.role_id " +
            "WHERE ur.user_id = #{userId} AND p.deleted = 0")
    List<String> findPermissionCodes(@Param("userId") Long userId);
}
