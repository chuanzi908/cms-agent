package com.ylf.cms.security.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ylf.cms.security.user.entity.SysRolePermission;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色-权限关联。复合主键表，只用 BaseMapper 里按条件的那几个方法：
 * selectList / selectCount / insert / delete(Wrapper)。
 * selectById、updateById、deleteById 依赖单列主键，这张表上没有，别调。
 */
@Mapper
public interface SysRolePermissionMapper extends BaseMapper<SysRolePermission> {
}
