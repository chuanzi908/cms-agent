package com.ylf.cms.security.user.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class AssignRolesRequest {

    /** 传空数组表示"收掉这个人的全部角色"，与 null 区分：null 视为参数缺失 */
    @NotNull(message = "角色列表不能为空")
    private List<Long> roleIds;
}
