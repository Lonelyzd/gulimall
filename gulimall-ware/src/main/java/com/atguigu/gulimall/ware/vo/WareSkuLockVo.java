package com.atguigu.gulimall.ware.vo;

import lombok.Data;

import java.util.List;

/**锁库存
 * @author : z_dd
 * @date : 2024/3/2 21:28
 **/
@Data
public class WareSkuLockVo {
    private String orderSn;

    private List<OrderItemVo> locks;//需要锁住的所有库存信息
}
