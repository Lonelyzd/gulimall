package com.atguigu.gulimall.search.coltroller;

import com.atguigu.common.exception.BizCodeEnum;
import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

/**
 * @author: z_dd
 * @date: 2023/4/16 15:39
 * @Description:
 */
@Slf4j
@RestController
@RequestMapping("/search/save")
public class ElasticSaveColtroller {

    @Autowired
    ProductSaveService productSaveService;


    /**
     * 商品上架
     *
     * @param skuEsModelList:
     * @Author: z_dd
     * @Date: 2023/4/16 15:45
     * @return: com.atguigu.common.utils.R
     **/
    @PostMapping("/product")
    public R productStatusUp(@RequestBody List<SkuEsModel> skuEsModelList) {
        boolean bool = true;
        try {
            bool = productSaveService.productStatusUp(skuEsModelList);
        } catch (IOException e) {
            log.error("ElasticSaveColtroller商品上架错误！", e);
            return R.error(BizCodeEnum.PRODUCT_UP_EXCEPTION.getCode(), BizCodeEnum.PRODUCT_UP_EXCEPTION.getMsg());
        }

        if (!bool) {
            return R.ok();
        } else {
            return R.error(BizCodeEnum.PRODUCT_UP_EXCEPTION.getCode(), BizCodeEnum.PRODUCT_UP_EXCEPTION.getMsg());
        }
    }
}
