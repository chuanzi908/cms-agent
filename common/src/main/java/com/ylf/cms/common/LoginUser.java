package com.ylf.cms.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 登录用户信息（公共层，M1维护）
 * 由M1登录拦截器填充，各模块只读
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 用户ID */
    private Long userId;

    /** 用户名 */
    private String username;

    /** 昵称 */
    private String nickname;

    /** 角色编码 */
    private List<String> roles;

    /** 权限编码 */
    private List<String> permissions;
}
