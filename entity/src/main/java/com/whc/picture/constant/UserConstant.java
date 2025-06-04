package com.whc.picture.constant;

/**
 * 用户常量
 * 这里使用接口类来定义常量，因为接口类里的常量默认就是 "static final" 的
 */
public interface UserConstant {

    /**
     * 用户登录态键
     */
    String USER_LOGIN_STATE = "user_login_state";

    // region 权限

    /**
     * 默认角色
     */
    String DEFAULT_ROLE = "user";

    /**
     * 管理员角色
     */
    String ADMIN_ROLE = "admin";

    // endregion

}
