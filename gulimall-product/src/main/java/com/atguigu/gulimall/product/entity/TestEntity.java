package com.atguigu.gulimall.product.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * @author : z_dd
 * @date : 2023/9/26 19:16
 **/
@Data
@TableName("pms_spu_info")
public class TestEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;

    private String position;

    private String level;

    private Integer salary;
}
