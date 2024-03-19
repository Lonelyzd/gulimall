package com.atguigu.gulimall.ware.config;

import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.zaxxer.hikari.HikariDataSource;
import io.seata.rm.datasource.DataSourceProxy;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;

/**
 * @author: z_dd
 * @date: 2023/3/12 20:41
 * @Description:
 */
@Configuration
@EnableTransactionManagement    //开启事务
@MapperScan("com.atguigu.gulimall.ware.dao")
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

    @Bean
    public DataSource dataSource(DataSourceProperties dataSourceProperties) {
        final HikariDataSource dataSource = dataSourceProperties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();

        if (StringUtils.hasText(dataSourceProperties.getName())) {
            dataSource.setPoolName(dataSourceProperties.getName());
        }
        return new DataSourceProxy(dataSource);
    }
}
