package com.atguigu.gulimall.gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
     * 日志过滤器，用于记录日志
     * @author mx
     * @date 2024/3/24 17:17
     */
    @Slf4j
//    @Component
    public class AccessLogFilter implements GlobalFilter, Ordered {

        private final List<HttpMessageReader<?>> messageReaders = HandlerStrategies.withDefaults().messageReaders();
    
        @Override
        public int getOrder() {
            return -100;
        }
    
        @Override
        @SuppressWarnings("unchecked")
        public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    
            ServerHttpRequest request = exchange.getRequest();
    
            // 请求路径
            String requestPath = request.getPath().pathWithinApplication().value();
    
            Route route = getGatewayRoute(exchange);


//            String ipAddress = WebUtils.getServerHttpRequestIpAddress(request);
//
//            GatewayLog gatewayLog = new GatewayLog();
//            gatewayLog.setSchema(request.getURI().getScheme());
//            gatewayLog.setRequestMethod(request.getMethodValue());
//            gatewayLog.setRequestPath(requestPath);
//            gatewayLog.setTargetServer(`route.getId()`);
//            gatewayLog.setRequestTime(new Date());
//            gatewayLog.setIp(ipAddress);
//
//            MediaType mediaType = request.getHeaders().getContentType();
//
//            if(MediaType.APPLICATION_FORM_URLENCODED.isCompatibleWith(mediaType) || MediaType.APPLICATION_JSON.isCompatibleWith(mediaType)){
//                return writeBodyLog(exchange, chain, gatewayLog);
//            }else{
//                return writeBasicLog(exchange, chain, gatewayLog);
//            }

            return null;
        }

        private Route getGatewayRoute(ServerWebExchange exchange) {
            return exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        }


    }