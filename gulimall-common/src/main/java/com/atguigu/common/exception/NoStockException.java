package com.atguigu.common.exception;

/**
 * 库存不足异常
 *
 * @author : z_dd
 * @date : 2024/3/3 12:10
 **/
public class NoStockException extends RuntimeException {
    public NoStockException(Long skuId) {
        super("商品id:" + skuId + "没有足够库存");
    }
    public NoStockException(String msg) {
        super(msg);
    }


}
