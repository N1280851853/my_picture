package com.whc.picture.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.whc.picture.entity.User;
import com.whc.picture.user.controller.qo.UserLoginQO;
import com.whc.picture.user.controller.qo.UserRegisterQO;
import com.whc.picture.user.controller.vo.LoginUserVO;

import javax.servlet.http.HttpServletRequest;

/**
* @author simceredx
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2025-05-28 20:12:17
*/
public interface UserService extends IService<User> {

    /**
     * 用户登录
     * @param userLoginQO
     * @param request
     * @return
     */
    LoginUserVO userLogin(UserLoginQO userLoginQO, HttpServletRequest request);

    /**
     * 用户注册
     * @return
     */
    long userRegister(UserRegisterQO qo);

    /**
     * 获取当前登录的用户
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 获取加密后的密码
     * @param userPassword
     * @return
     */
    String getEncryptPassword(String userPassword);
}
