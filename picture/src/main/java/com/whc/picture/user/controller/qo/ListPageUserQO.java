package com.whc.picture.user.controller.qo;

import com.whc.picture.bean.PageQO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 获取用户分页请求列表
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ListPageUserQO extends PageQO implements Serializable {

    private static final long serialVersionUID = 1593282031093231029L;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户角色：user/admin
     */
    private String userRole;

}
