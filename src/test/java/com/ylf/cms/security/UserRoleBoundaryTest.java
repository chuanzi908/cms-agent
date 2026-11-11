package com.ylf.cms.security;

import com.ylf.cms.common.exception.BizException;
import com.ylf.cms.common.result.ResultCode;
import com.ylf.cms.security.user.entity.SysRole;
import com.ylf.cms.security.user.entity.SysUser;
import com.ylf.cms.security.user.mapper.SysRoleMapper;
import com.ylf.cms.security.user.mapper.SysUserMapper;
import com.ylf.cms.security.user.mapper.SysUserRoleMapper;
import com.ylf.cms.security.user.service.UserRoleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * 判据：用户角色是"全量替换"，且校验失败时不许动已有数据。
 *
 * 能失败：把 assignRoles 里"先校验再删"改成"先删再校验"，invalidRoleDoesNotTouchData
 * 立刻变红（deleteByUserId 被调用了）；把去重去掉，duplicatesInsertedOnce 变红。
 */
class UserRoleBoundaryTest {

    private final SysUserMapper userMapper = mock(SysUserMapper.class);
    private final SysRoleMapper roleMapper = mock(SysRoleMapper.class);
    private final SysUserRoleMapper userRoleMapper = mock(SysUserRoleMapper.class);
    private final UserRoleService service = new UserRoleService(userMapper, roleMapper, userRoleMapper);

    @Test
    @DisplayName("没有任何角色：返回空集合，不去查角色表")
    void noRoleReturnsEmpty() {
        when(userRoleMapper.selectRoleIds(7L)).thenReturn(Collections.emptyList());

        List<SysRole> roles = service.rolesOf(7L);

        assertTrue(roles.isEmpty(), "没有关联关系时应该是空集合，而不是 null");
        verifyNoInteractions(roleMapper);
    }

    @Test
    @DisplayName("全量替换：先清空，再按去重后的顺序插入")
    void duplicatesInsertedOnce() {
        when(userMapper.selectById(7L)).thenReturn(new SysUser());
        when(roleMapper.selectCount(any())).thenReturn(2L);

        service.assignRoles(7L, Arrays.asList(3L, 1L, 3L, null));

        verify(userRoleMapper).deleteByUserId(7L);
        verify(userRoleMapper).insert(7L, 3L);
        verify(userRoleMapper).insert(7L, 1L);
        verify(userRoleMapper, times(2)).insert(anyLong(), anyLong());
    }

    @Test
    @DisplayName("角色 id 无效：拒绝，且不清空原有角色")
    void invalidRoleDoesNotTouchData() {
        when(userMapper.selectById(7L)).thenReturn(new SysUser());
        when(roleMapper.selectCount(any())).thenReturn(1L);

        BizException ex = assertThrows(BizException.class, () -> service.assignRoles(7L, Arrays.asList(3L, 99L)));

        assertEquals(ResultCode.BAD_REQUEST, ex.getResultCode());
        verify(userRoleMapper, never()).deleteByUserId(anyLong());
        verify(userRoleMapper, never()).insert(anyLong(), anyLong());
    }

    @Test
    @DisplayName("用户不存在：404，且不碰关联表")
    void unknownUserRejected() {
        when(userMapper.selectById(404L)).thenReturn(null);

        BizException ex = assertThrows(BizException.class, () -> service.assignRoles(404L, List.of(1L)));

        assertEquals(ResultCode.NOT_FOUND, ex.getResultCode());
        verifyNoInteractions(userRoleMapper);
    }
}
