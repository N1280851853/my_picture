package com.whc.picture.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


/**
 * 全局跨域配置
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    //@Autowired
    //private LoginInterceptor loginInterceptor;

    /**
     * 拦截器的执行顺序：
     * 1、加入的顺序就是拦截器的执行顺序；
     * 2、先顺时针执行preHandle， 再逆时针执行postHandle，最后逆时针执行afterCompletion;
     */
    //@Override
    //public void addInterceptors(InterceptorRegistry registry) {
    //    // 身份验证
    //    registry.addInterceptor(loginInterceptor);
    //}

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 覆盖所有请求
        registry.addMapping("/**")
                // 允许发送 Cookie
                .allowCredentials(true)
                // 放行哪些域名（必须用 patterns，否则 * 会和 allowCredentials 冲突）
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("*");
    }

}
