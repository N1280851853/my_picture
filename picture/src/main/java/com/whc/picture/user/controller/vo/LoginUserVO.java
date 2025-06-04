package com.whc.picture.user.controller.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 用户登录成功后返回给前端的(脱敏)数据
 */
@Data
@Accessors(chain = true)
public class LoginUserVO implements Serializable {

    private static final long serialVersionUID = -7023285285660530576L;
    /**
     * id
     */
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

    /**
     * 创建时间
     */
    private String gmtCreate;

    /**
     * 更新时间
     */
    private String gmtModified;

}
