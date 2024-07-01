package com.atguigu.gulimall.gateway.config;

import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import com.alibaba.fastjson.JSON;
import com.atguigu.common.exception.BizCodeEnum;
import com.atguigu.common.utils.R;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

/**
 * @author : z_dd
 * @date : 2024/7/25 21:13
 **/
@Configuration
public class SentinelGatewayConfig {
    public SentinelGatewayConfig(){
        GatewayCallbackManager.setBlockHandler((serverWebExchange, throwable) -> {
            final R error = R.error(BizCodeEnum.TOO_MANY_REQUEST.getCode(), BizCodeEnum.TOO_MANY_REQUEST.getMsg());
            final String jsonString = JSON.toJSONString(error);
            final Mono<ServerResponse> body = ServerResponse.ok().body(Mono.just(jsonString), String.class);
            return body;
        });
    }
}
