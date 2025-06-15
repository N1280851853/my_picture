//package com.whc.picture.aop;
//
//import cn.hutool.core.util.ObjectUtil;
//import com.whc.picture.constant.UserConstant;
//import com.whc.picture.entity.user.User;
//import com.whc.picture.exception.BusinessException;
//import com.whc.picture.exception.ErrorCode;
//import org.springframework.lang.Nullable;
//import org.springframework.stereotype.Component;
//import org.springframework.web.servlet.HandlerInterceptor;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import javax.servlet.http.HttpSession;
//import java.util.ArrayList;
//import java.util.List;
//
//@Component
//public class LoginInterceptor implements HandlerInterceptor {
//
//    /**
//     * 免登录的特殊URL
//     */
//    private static final List<String> arrowUrl = new ArrayList<>();
//
//    public static final ThreadLocal<User> EMPLOYEE_THREAD_LOCAL = new ThreadLocal<>();
//
//    static {
//        arrowUrl.add("/api/health");
//        arrowUrl.add("/api/user/login");
//    }
//
//    @Override
//    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//
//        String uri = request.getRequestURI();
//        if (!arrowUrl.contains(uri)) {
//            HttpSession session = request.getSession();
//            User user = (User) session.getAttribute(UserConstant.USER_LOGIN_STATE);
//            if (ObjectUtil.isNotEmpty(user)) {
//                EMPLOYEE_THREAD_LOCAL.set(user);
//                return HandlerInterceptor.super.preHandle(request, response, handler);
//            }
//
//            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
//        }
//        return HandlerInterceptor.super.preHandle(request, response, handler);
//    }
//
//    @Override
//    public void afterCompletion(HttpServletRequest request,
//                                HttpServletResponse response, Object handler,
//                                @Nullable Exception ex) {
//        //请求结束，本地线程删除用户信息
//        EMPLOYEE_THREAD_LOCAL.remove();
//    }
//
//    public static User getCurrentEmployee() {
//        return EMPLOYEE_THREAD_LOCAL.get();
//    }
//}
