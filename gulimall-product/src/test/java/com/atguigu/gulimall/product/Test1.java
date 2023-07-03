package com.atguigu.gulimall.product;

import com.atguigu.gulimall.product.app.Date;
import org.junit.Test;

import java.time.LocalDateTime;

/**
 * @author: z_dd
 * @date: 2023/5/10 19:50
 * @Description:
 */
public class Test1 {
    @Test
    public void tt(){
        final LocalDateTime now = Date.now();
        System.out.println(now);
    }
}
