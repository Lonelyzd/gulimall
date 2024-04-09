package com.atguigu.gulimall.order.config;

import org.springframework.context.annotation.Configuration;

/**
 * @author : z_dd
 * @date : 2024/3/13 21:09
 **/

@Configuration
public class MySeataConfig {
   /* @Bean
    public DataSource dataSource(DataSourceProperties dataSourceProperties) {
        final HikariDataSource dataSource = dataSourceProperties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();

        if (StringUtils.hasText(dataSourceProperties.getName())) {
            dataSource.setPoolName(dataSourceProperties.getName());
        }
        return new DataSourceProxy(dataSource);
    }*/
}
