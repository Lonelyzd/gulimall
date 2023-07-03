package com.atguigu.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.gulimall.search.config.GuliMallElasticSearchConfig;
import com.atguigu.gulimall.search.constant.EsConstant;
import com.atguigu.gulimall.search.service.ProductSaveService;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: z_dd
 * @date: 2023/4/16 15:46
 * @Description:
 */
@Slf4j
@Service
public class ProductSaveServiceImpl implements ProductSaveService {

    @Autowired
    RestHighLevelClient restHighLevelClient;

    @Override
    public boolean productStatusUp(List<SkuEsModel> skuEsModelList) throws IOException {
        //保存到ES
        //1.给ES中索引product,建立好映射关系，见src/product-mapping.txt

        //2.给ES中保存这些数据
        BulkRequest bulkRequest = new BulkRequest();
        for (SkuEsModel esModel : skuEsModelList) {
            final IndexRequest request = new IndexRequest(EsConstant.PRODUCT_INDEX);
            request.id(esModel.getSkuId().toString());
            final String jsonString = JSON.toJSONString(esModel);
            request.source(jsonString, XContentType.JSON);
            bulkRequest.add(request);
        }

        final BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, GuliMallElasticSearchConfig.COMMON_OPTIONS);

        //TODO 1.如果商品上架错误
        final boolean b = bulk.hasFailures();

        final List<String> collect = Arrays
                .stream(bulk.getItems())
                .map(BulkItemResponse::getId)
                .collect(Collectors.toList());
        log.info("商品上架完成：{}", collect);
        return b;
    }
}
