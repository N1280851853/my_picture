package com.whc.picture.user.controller;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.whc.picture.annotation.AuthCheck;
import com.whc.picture.bean.PageVO;
import com.whc.picture.common.BaseResponse;
import com.whc.picture.common.ResultUtils;
import com.whc.picture.constant.UserConstant;
import com.whc.picture.entity.user.UserDO;
import com.whc.picture.exception.BusinessException;
import com.whc.picture.exception.ErrorCode;
import com.whc.picture.user.controller.qo.*;
import com.whc.picture.user.controller.vo.LoginUserVO;
import com.whc.picture.user.controller.vo.ListPageUserVO;
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
import java.util.ArrayList;
import java.util.List;

/**
 * 用户管理
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户登录
     *
     * @param qo
     * @param request
     * @return
     */
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> login(@RequestBody @Validated UserLoginQO qo, HttpServletRequest request) {
        return ResultUtils.success(userService.userLogin(qo, request));
    }

    /**
     * 用户注册
     *
     * @param qo
     * @return
     */
    @PostMapping("/register")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Object> register(@RequestBody @Validated UserRegisterQO qo) {
        // 检测用户账号是否和数据库中已有的重复
        Long count = userService.lambdaQuery()
                .select(UserDO::getId)
                .eq(UserDO::getUserAccount, qo.getUserAccount())
                .count();
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        }

        long userId = userService.userRegister(qo);

        return ResultUtils.success(userId);
    }

    /**
     * 获取登录用户
     *
     * @param request
     * @return
     */
    @PostMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        UserDO loginUser = userService.getLoginUser(request);

        // 设置返回值给前端
        LoginUserVO vo = userService.getUserVO(loginUser);

        return ResultUtils.success(vo);
    }

    /**
     * 退出登录
     *
     * @param request
     * @return
     */
    @PostMapping("/logout")
    public BaseResponse<Object> logout(HttpServletRequest request) {
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

    /**
     * 管理员添加用户
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/savaUser")
    public BaseResponse<Object> savaUser(@RequestBody UserAddQO qo) {
        // 检测用户账号是否和数据库中已有的重复
        Long count = userService.lambdaQuery()
                .select(UserDO::getId)
                .eq(UserDO::getUserAccount, qo.getUserAccount())
                .count();
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        }

        long userId = userService.userAdd(qo);

        return ResultUtils.success(userId);
    }

    /**
     * 根据用户id获取用户(仅管理员使用)
     *
     * @param qo
     * @return
     */
    @PostMapping("/getUserById")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<UserDO> getUserById(@RequestBody @Validated UserDeleteQO qo) {
        Long id = qo.getId();
        UserDO user = userService.lambdaQuery()
                .eq(UserDO::getId, id)
                .one();
        return ResultUtils.success(user);
    }


    /**
     * 根据用户id获取用户信息的包装类
     *
     * @param qo
     * @return
     */
    @PostMapping("/get/userVo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<ListPageUserVO> getUserVOById(@RequestBody @Validated UserDeleteQO qo) {
        Long id = qo.getId();
        UserDO user = userService.lambdaQuery()
                .eq(UserDO::getId, id)
                .one();

        // 4. 设置返回值给前端
        ListPageUserVO vo = new ListPageUserVO();
        vo.setId(user.getId())
                .setUserAccount(user.getUserAccount())
                .setUserName(user.getUserName())
                .setUserAvatar(user.getUserAvatar())
                .setUserProfile(user.getUserProfile())
                .setUserRole(user.getUserRole());

        return ResultUtils.success(vo);
    }

    /**
     * 删除用户(仅管理员使用)
     *
     * @param qo
     * @return
     */
    @PostMapping("/deleteUserById")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Object> deleteUserById(@RequestBody @Validated UserDeleteQO qo) {
        Long id = qo.getId();
        userService.removeById(id);
        return ResultUtils.success();
    }

    /**
     * 更新用户(仅管理员使用)
     *
     * @param qo
     * @return
     */
    @PostMapping("/updateUserById")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Object> updateUserById(@RequestBody @Validated UserUpdateQO qo) {
        userService.userUpdate(qo);
        return ResultUtils.success();
    }

    /**
     * 获取用户列表(分页)
     *
     * @param qo
     * @return
     */
    @PostMapping("/listPageUser")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<PageVO<ListPageUserVO>> listPageUser(@RequestBody @Validated ListPageUserQO qo) {
        Page<UserDO> page = userService.lambdaQuery()
                .select(
                        UserDO::getId,
                        UserDO::getUserAccount,
                        UserDO::getUserName,
                        UserDO::getUserAvatar,
                        UserDO::getUserProfile,
                        UserDO::getUserRole,
                        UserDO::getGmtCreate,
                        UserDO::getGmtModified
                )
                .likeRight(ObjectUtil.isNotEmpty(qo.getUserName()), UserDO::getUserName, qo.getUserName())
                .likeRight(ObjectUtil.isNotEmpty(qo.getUserAccount()), UserDO::getUserAccount, qo.getUserAccount())
                .likeRight(ObjectUtil.isNotEmpty(qo.getUserRole()), UserDO::getUserRole, qo.getUserRole())
                .page(qo.getPage());

        PageVO<ListPageUserVO> vo = new PageVO<>(page.getTotal());
        List<UserDO> users = page.getRecords();
        List<ListPageUserVO> rtList = new ArrayList<>();

        if (ObjectUtil.isNotEmpty(users)) {
            users.forEach(t -> {
                ListPageUserVO rt = new ListPageUserVO();
                rt.setId(t.getId())
                        .setUserAccount(t.getUserAccount())
                        .setUserName(t.getUserName())
                        .setUserAvatar(t.getUserAvatar())
                        .setUserProfile(t.getUserProfile())
                        .setUserRole(t.getUserRole())
                        .setGmtCreate(t.getGmtCreate().format(DateTimeFormatter.ofPattern(DatePattern.NORM_DATETIME_PATTERN)))
                        .setGmtModified(LocalDateTimeUtil.format(t.getGmtModified(), DateTimeFormatter.ofPattern(DatePattern.NORM_DATETIME_PATTERN)));

                rtList.add(rt);
            });
        }

        vo.setList(rtList);

        return ResultUtils.success(vo);
    }

}
