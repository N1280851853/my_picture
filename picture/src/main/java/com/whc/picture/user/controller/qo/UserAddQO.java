package com.whc.picture.user.controller.qo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author： whc
 * @create： 2025/6/4 20:32
 */
@Data
public class UserAddQO implements Serializable {

    private static final long serialVersionUID = 1593282031093231029L;

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
