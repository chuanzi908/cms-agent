package com.ylf.cms.security.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ylf.cms.security.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统用户Mapper（M1 内部，禁止被其他模块import）
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
}
