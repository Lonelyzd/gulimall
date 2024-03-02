package com.atguigu.gulimall.order.vo;

import com.atguigu.gulimall.order.entity.OrderEntity;
import lombok.Data;

/**
 * @author : z_dd
 * @date : 2024/2/20 23:50
 **/
@Data
public class SubmitOrderResponseVo {

    private OrderEntity order;

    private Integer code;//0成功 错误状态码
}
