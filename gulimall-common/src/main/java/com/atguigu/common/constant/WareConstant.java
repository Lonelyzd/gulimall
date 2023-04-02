package com.atguigu.common.constant;

/**
 * @author: z_dd
 * @date: 2023/3/31 21:39
 * @Description: 商品模块枚举
 */
public class WareConstant {

    //采购单状态
    public enum PurchaseStatusEnum {
        CREATED(0, "新建"),ASSIGNED(1, "已分配"),
        RECEIVE(2, "已领取"),FINISH(3, "已完成"),
        HASERROR(4, "有异常"),
        ;

        private int code;
        private String msg;

        PurchaseStatusEnum(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }
    }


    //采购需求状态
    public enum PurchaseDetialStatusEnum {
        CREATED(0, "新建"),ASSIGNED(1, "已分配"),
        BUSING(2, "正在采购"),FINISH(3, "已完成"),
        HASERROR(4, "采购失败"),
        ;

        private int code;
        private String msg;

        PurchaseDetialStatusEnum(int code, String msg) {
            this.code = code;
            this.msg = msg;
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }
    }
}
