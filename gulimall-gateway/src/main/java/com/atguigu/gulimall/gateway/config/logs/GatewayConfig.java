package com.atguigu.gulimall.gateway.config.logs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.server.WebFilter;

@Configuration
@ConditionalOnProperty(prefix = "spring.cloud.gateway", name = "log-switch", havingValue = "true", matchIfMissing = false)
public class GatewayConfig {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE) //过滤器顺序
    public WebFilter webFilter() {
        System.out.println(111);
        return (exchange, chain) -> chain.filter(new PayloadServerWebExchangeDecorator(exchange));
    }

}

