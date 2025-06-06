package com.whc.picture.user.controller.qo;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 更新用户信息请求接口
 */
@Data
public class UserUpdateQO implements Serializable {

    private static final long serialVersionUID = 1593282031093231029L;

    /**
     * id
     */
    @NotNull
    private Long id;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 用户角色：user/admin
     */
    private String userRole;

}
