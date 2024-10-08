package com.atguigu.common.constant;

import lombok.Getter;

/**
 * @author: z_dd
 * @date: 2023/3/18 15:04
 * @Description: 商品模块常量
 */
public class ProductConstant {

    @Getter
    public enum AttrEnum {
        ATTR_TYPE_BASE(1, "基本属性"), ATTR_TYPE_SALE(0, "销售属性");
        private int code;
        private String msg;

        AttrEnum(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }
    }

    @Getter
    public enum SuatusEnum {
        NEW_SPU(0, "新建"), SPU_UP(1, "上架"), SPU_DOWN(2, "下架");
        private int code;
        private String msg;

        SuatusEnum(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }
    }

}
