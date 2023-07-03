package com.atguigu.gulimall.product.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 二级分类
 *
 * @author: z_dd
 * @date: 2023/4/20 21:31
 * @Description:
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Catelog2Vo {

    private String catalog1Id;

    private String id;

    private String name;

    private List<Catelog3Vo> catalog3List;


    /**
     * 三级分类
     *
     * @author: z_dd
     * @date: 2023/4/20 21:31
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Catelog3Vo {
        private String catalog2Id;

        private String id;

        private String name;
    }
}
