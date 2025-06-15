package com.whc.picture.aop;

import com.whc.picture.annotation.AuthCheck;
import com.whc.picture.constant.UserRoleEnum;
import com.whc.picture.entity.user.UserDO;
import com.whc.picture.exception.BusinessException;
import com.whc.picture.exception.ErrorCode;
import com.whc.picture.user.service.UserService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Aspect
@Component
public class AuthInterceptor {

    @Resource
    private UserService userService;

    /**
     * 进行拦截
     */
    @Around("@annotation(authCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, AuthCheck authCheck) throws Throwable {
        // 获取用户必须要有的权限
        String mustRole = authCheck.mustRole();

        // 获取当前请求接口的所有参数
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();

        // 获取当前登录用户
        UserDO loginUser = userService.getLoginUser(request);
        UserRoleEnum mustRoleEnum = UserRoleEnum.getUserRokeEnum(mustRole);
        // 如果不需要权限，放行
        if (mustRoleEnum == null) {
            return joinPoint.proceed();
        }

        // 获取当前登录用户的角色
        UserRoleEnum userRoleEnum = UserRoleEnum.getUserRokeEnum(loginUser.getUserRole());
        if (userRoleEnum == null) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        // 如果当前接口需要的权限不等于 用户所持有的权限  则报无权限异常
        if (mustRoleEnum != userRoleEnum) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        // 放行
        return joinPoint.proceed();
    }

}
