package com.atguigu.gulimall.product.service;

import com.atguigu.common.utils.PageUtils;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author IceBlue
 * @email icebule.top@qq.com
 * @date 2023-01-27 19:33:38
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<CategoryEntity> listWithTree();

    void removeMenuByIds(List<Long> catIds);

    /** 获取分类的三级路径
     * @Author: z_dd
     * @Date: 2023/3/12 13:55
     * @param catelogId:
     * @return: java.lang.Long[]
     **/
    Long[] findCatelogPath(Long catelogId);

    void updateDetail(CategoryEntity category);
}

