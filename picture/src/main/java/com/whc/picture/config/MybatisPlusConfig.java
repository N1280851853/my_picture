package com.whc.picture.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.apache.ibatis.reflection.MetaObject;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.time.LocalDateTime;

/**
 * Mybatis-Plus配置文件
 *
 * @EnableTransactionManagement 因为跟操作数据库相关，所以也放在Mybatis的配置文件这。
 */
@MapperScan(basePackages = "com.whc.picture.**.mapper")
@Configuration
@EnableTransactionManagement
public class MybatisPlusConfig {
    
    /**
     * 引入MybatisPlus的分页插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
    
    /**
     * 遵循阿里巴巴开发手册：每个表中增加create_time, update_time两个字段，Mybatis-plus提供了自动填充的接口。
     * 规则：
     * 1. 插入时，createTime跟updateTime相同，且同时更新；
     * 2. 更新时，只更新updateTime；
     */
    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MetaObjectHandler() {
            @Override
            public void insertFill(MetaObject metaObject) {
                this.setFieldValByName("gmtCreate", LocalDateTime.now(), metaObject);
                this.setFieldValByName("gmtModified", LocalDateTime.now(), metaObject);
            }
            
            @Override
            public void updateFill(MetaObject metaObject) {
                this.setFieldValByName("gmtModified", LocalDateTime.now(), metaObject);
            }
        };
    }
}
