package com.ylf.cms.security.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ylf.cms.security.user.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

    @Select("select r.* from sys_role r join sys_user_role ur on ur.role_id = r.id "
            + "where ur.user_id = #{userId} and r.deleted = 0 order by r.sort asc, r.id asc")
    List<SysRole> selectByUserId(@Param("userId") Long userId);
}
