package com.whc.picture.user.controller;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjectUtil;
import com.whc.picture.annotation.AuthCheck;
import com.whc.picture.common.BaseResponse;
import com.whc.picture.common.ResultUtils;
import com.whc.picture.constant.UserConstant;
import com.whc.picture.entity.User;
import com.whc.picture.exception.BusinessException;
import com.whc.picture.exception.ErrorCode;
import com.whc.picture.user.controller.qo.UserLoginQO;
import com.whc.picture.user.controller.qo.UserRegisterQO;
import com.whc.picture.user.controller.vo.LoginUserVO;
import com.whc.picture.user.service.UserService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    // 用户登录
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> login(@RequestBody @Validated UserLoginQO qo, HttpServletRequest request) {
        return ResultUtils.success(userService.userLogin(qo, request));
    }

    // 用户注册
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Object> register(@RequestBody @Validated UserRegisterQO qo) {
        // 检测用户账号是否和数据库中已有的重复
        Long count = userService.lambdaQuery()
                .select(User::getId)
                .eq(User::getUserAccount, qo.getUserAccount())
                .count();
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        }

        long userId = userService.userRegister(qo);

        return ResultUtils.success(userId);
    }

    /**
     * 获取登录用户
     * @param request
     * @return
     */
    @PostMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);

        // 4. 设置返回值给前端
        LoginUserVO vo = new LoginUserVO();
        vo.setId(loginUser.getId())
                .setUserAccount(loginUser.getUserAccount())
                .setUserName(loginUser.getUserName())
                .setUserAvatar(loginUser.getUserAvatar())
                .setUserProfile(loginUser.getUserProfile())
                .setUserRole(loginUser.getUserRole())
                .setGmtCreate(loginUser.getGmtCreate().format(DateTimeFormatter.ofPattern(DatePattern.NORM_DATETIME_MINUTE_PATTERN)))
                .setGmtModified(LocalDateTimeUtil.format(loginUser.getGmtModified(), DatePattern.NORM_DATETIME_MINUTE_PATTERN));

        return ResultUtils.success(vo);
    }

    // 退出登录
    @PostMapping("/logout")
    public BaseResponse logout(HttpServletRequest request) {
        HttpSession session = request.getSession();
        // 判断是否已登录
        Object userObj = session.getAttribute(UserConstant.USER_LOGIN_STATE);
        if (ObjectUtil.isEmpty(userObj)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }

        // 移除登录态
        session.removeAttribute(UserConstant.USER_LOGIN_STATE);
        return ResultUtils.success();
    }

}
