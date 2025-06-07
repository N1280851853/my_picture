package com.whc.picture.user.service.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whc.picture.constant.UserConstant;
import com.whc.picture.constant.UserRoleEnum;
import com.whc.picture.entity.User;
import com.whc.picture.exception.BusinessException;
import com.whc.picture.exception.ErrorCode;
import com.whc.picture.exception.ThrowUtils;
import com.whc.picture.user.controller.qo.UserAddQO;
import com.whc.picture.user.controller.qo.UserLoginQO;
import com.whc.picture.user.controller.qo.UserRegisterQO;
import com.whc.picture.user.controller.qo.UserUpdateQO;
import com.whc.picture.user.controller.vo.LoginUserVO;
import com.whc.picture.user.mapper.UserMapper;
import com.whc.picture.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.time.format.DateTimeFormatter;

/**
* @author simceredx
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2025-05-28 20:12:17
*/
@Slf4j
@Service
@Transactional
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public LoginUserVO userLogin(UserLoginQO qo, HttpServletRequest request) {

        String userAccount = qo.getUserAccount();
        String userPassword = qo.getUserPassword();
        // 1. 对用户传递的密码进行加密
        String encryptPassword = getEncryptPassword(userPassword);

        // 2. 查询用户是否存在
        User user = this.lambdaQuery()
                .select(
                        User::getId,
                        User::getUserAccount,
                        User::getUserName,
                        User::getUserAvatar,
                        User::getUserProfile,
                        User::getUserRole,
                        User::getGmtCreate,
                        User::getGmtModified
                )
                .eq(User::getUserAccount, userAccount)
                .eq(User::getUserPassword, encryptPassword)
                .one();

        if (ObjectUtil.isEmpty(user)) {
            log.error("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或者密码错误");
        }

        // 3. 保存用户登录信息
        HttpSession session = request.getSession();
        session.setAttribute(UserConstant.USER_LOGIN_STATE, user);

        // 4. 设置返回值给前端
        LoginUserVO vo = new LoginUserVO();
        vo.setId(user.getId())
                .setUserAccount(user.getUserAccount())
                .setUserName(user.getUserName())
                .setUserAvatar(user.getUserAvatar())
                .setUserProfile(user.getUserProfile())
                .setUserRole(user.getUserRole())
                .setGmtCreate(user.getGmtCreate().format(DateTimeFormatter.ofPattern(DatePattern.NORM_DATETIME_MINUTE_PATTERN)))
                .setGmtModified(LocalDateTimeUtil.format(user.getGmtModified(), DatePattern.NORM_DATETIME_MINUTE_PATTERN));

        return vo;
    }

    @Override
    public long userRegister(UserRegisterQO qo) {
        String userPassword = qo.getUserPassword();
        String checkPassword = qo.getCheckPassword();
        // 1. 校验参数
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }

        // 2. 密码一定要加密
        String encryptPassword = getEncryptPassword(userPassword);

        // 3. 插入数据到数据库中
        User user = new User();
        user.setUserAccount(qo.getUserAccount())
                .setUserPassword(encryptPassword)
                // 给一个默认用户名称
                .setUserName("无名")
                .setUserRole(UserRoleEnum.USER.getValue());

        boolean save = this.save(user);
        ThrowUtils.throwIf(!save, ErrorCode.OPERATION_ERROR);
        return user.getId();
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 判断是否登录
        HttpSession session = request.getSession();
        Object userObj = session.getAttribute(UserConstant.USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (ObjectUtil.isEmpty(currentUser) || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 从数据库中再查询一次(防止用户信息已经更改了，但是走session缓存的话依然获取的是之前的用户信息)
        Long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (ObjectUtil.isEmpty(currentUser)) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }

        return currentUser;
    }


    // 将用户密码加密后进行存储。可以封装一个方法，便于后续复用：
    @Override
    public String getEncryptPassword(String userPassword) {

        // 加盐，混淆密码
        final String SALT = "whc";
        String pwd = SALT + userPassword;

        return DigestUtils.md5DigestAsHex(pwd.getBytes());
    }

    @Override
    public long userAdd(UserAddQO qo) {

        // 3. 插入数据到数据库中
        User user = new User();
        // 获取加密后的默认密码
        String encryptPassword = getEncryptPassword(DEFAULT_PASSWORD);

        user.setUserAccount(qo.getUserAccount())
                .setUserPassword(encryptPassword)
                // 给一个默认用户名称
                .setUserName(qo.getUserName())
                .setUserAvatar(qo.getUserAvatar())
                .setUserProfile(qo.getUserProfile())
                .setUserRole(qo.getUserRole());

        boolean res = this.save(user);
        ThrowUtils.throwIf(!res, ErrorCode.OPERATION_ERROR);

        return user.getId();
    }

    @Override
    public long userUpdate(UserUpdateQO qo) {
        User user = new User();
        user.setId(qo.getId())
                .setUserAccount(qo.getUserAccount())
                .setUserName(qo.getUserName())
                .setUserAvatar(qo.getUserAvatar())
                .setUserProfile(qo.getUserProfile())
                .setUserRole(qo.getUserRole());
        boolean b = this.updateById(user);
        ThrowUtils.throwIf(!b, ErrorCode.OPERATION_ERROR);
        return user.getId();
    }


}




