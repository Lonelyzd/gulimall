package com.atguigu.gulimall.product.config;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author: z_dd
 * @date: 2023/3/12 20:41
 * @Description:
 */
@Configuration
@EnableTransactionManagement    //开启事务
@MapperScan("com.atguigu.gulimall.product.dao")
public class MybatisConfig {
    //引入分页插件
    @Bean
    public PaginationInterceptor paginationInterceptor() {
        PaginationInterceptor paginationInterceptor = new PaginationInterceptor();
        //设置请求的页面大于最后页后操作，true调回到首页，false继续请求，默认fales
        // paginationInterceptor.setOverflow(false)
        //设置最大单页限制数量，默认500条，-1不受限制
        // paginationInterceptor.setLimit(500)
        return paginationInterceptor;
    }
}
