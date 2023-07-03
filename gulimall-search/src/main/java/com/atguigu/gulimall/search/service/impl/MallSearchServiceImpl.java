package com.atguigu.gulimall.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.common.to.es.SkuEsModel;
import com.atguigu.gulimall.search.config.GuliMallElasticSearchConfig;
import com.atguigu.gulimall.search.constant.EsConstant;
import com.atguigu.gulimall.search.service.MallSearchService;
import com.atguigu.gulimall.search.vo.SearchParam;
import com.atguigu.gulimall.search.vo.SearchResult;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.*;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.nested.NestedAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author: z_dd
 * @date: 2023/6/14 21:26
 * @Description:
 */
@Service
public class MallSearchServiceImpl implements MallSearchService {

    @Autowired
    RestHighLevelClient client;


    /**
     * @param param: 请求参数封装
     * @Author: z_dd
     * @Date: 2023/6/14 21:29
     * @return: com.atguigu.gulimall.search.vo.SearchResult
     **/
    @Override
    public SearchResult search(SearchParam param) {
        SearchResult result = null;

        //1.动态构建出查询需要的DSL语句
        SearchRequest searchRequest = builderSearchRequest(param);

        try {
            //2.执行检索请求
            final SearchResponse search = client.search(searchRequest, GuliMallElasticSearchConfig.COMMON_OPTIONS);

            //3.分析响应数据封装成我们需要的格式
            result = builderSearchResult(search, param);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return result;
    }


    /**
     * 准备检索请求
     * #模糊匹配 过滤（按照属性、分类、品牌、价格区间、库存）
     *
     * @Author: z_dd
     * @Date: 2023/6/19 21:30
     * @return: org.elasticsearch.action.search.SearchRequest
     * @Description:
     **/
    private SearchRequest builderSearchRequest(SearchParam param) {

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder(); //构建DSL语句

        /**
         * 模糊匹配 过滤（按照属性、分类、品牌、价格区间、库存）
         */
        // 1、构建bool - query
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        // 1.1 bool - must - 模糊匹配
        if (StringUtils.isNotBlank(param.getKeyword())) {
            boolQuery.must(QueryBuilders.matchQuery("skuTitle", param.getKeyword()));
        }

        // 1.2 bool - filter - term 按照三级分类id来查询
        if (param.getCatalog3Id() != null) {
            boolQuery.filter(QueryBuilders.termQuery("catalogId", param.getCatalog3Id()));
        }

        // 1.3 bool - filter - terms 按照品牌id来查询
        if (!CollectionUtils.isEmpty(param.getBrandId())) {
            boolQuery.filter(QueryBuilders.termsQuery("brandId", param.getBrandId()));
        }

        // 1.4 bool - filter - nested 按照所有指定的属性来进行查询 attr=1_5寸:8寸这样的设计
        final List<String> attrs = param.getAttrs();
        if (!CollectionUtils.isEmpty(attrs)) {
            for (String attr : attrs) {
                BoolQueryBuilder nestedBoolQuery = QueryBuilders.boolQuery();
                final String[] attrAll = attr.split("_");
                final String[] values = attrAll[1].split(":");

                nestedBoolQuery.must(QueryBuilders.termQuery("attrs.attrId", attrAll[0]));
                nestedBoolQuery.must(QueryBuilders.termsQuery("attrs.attrValue", values));
                // 每一个必须都生成一个nested查询
                NestedQueryBuilder nestedQuery = QueryBuilders.nestedQuery("attrs", nestedBoolQuery, ScoreMode.None);
                boolQuery.filter(nestedQuery);
            }
        }
        // 1.5 bool - filter -term 按照库存是否存在
        if(param.getHasStock()!=null) {
            boolQuery.filter(QueryBuilders.termsQuery("hasStock", Objects.equals(param.getHasStock(), 1)));
        }
        // 1.6 bool - filter -range 按照价格区间:1_500;  1_;  _500
        final String skuPrice = param.getSkuPrice();
        if (StringUtils.isNotBlank(skuPrice)) {
            RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("skuPrice");
            String[] s = param.getSkuPrice().split("_");
            if (s.length == 2) {
                // 区间
                rangeQuery.gte(s[0]).lte(s[1]);
            } else if (s.length == 1) {
                if (param.getSkuPrice().startsWith("_")) {
                    rangeQuery.lte(s[0]);
                }
                if (param.getSkuPrice().endsWith("_")) {
                    rangeQuery.gte(s[0]);
                }
            }
            boolQuery.filter(rangeQuery);
        }

        //把以前所有条件都拿来进行封装
        sourceBuilder.query(boolQuery);


        /**
         * 排序、分页、高亮
         */
        //2.1、排序
        if (!StringUtils.isEmpty(param.getSort())) {
            String sort = param.getSort();
            //sort=hotScore_asc/desc
            String[] s = sort.split("_");
            SortOrder order = s[1].equalsIgnoreCase("asc") ? SortOrder.ASC : SortOrder.DESC;
            sourceBuilder.sort(s[0], order);
        }
        //2.2 分页 pageSize:5
        // pageNum:1 from 0 size:5 [0,1,2,3,4]
        // pageNum:2 from 5 size:5
        // from (pageNum - 1)*size
        sourceBuilder.from((param.getPageNum() - 1) * EsConstant.PRODUCT_PAGESIZE);
        sourceBuilder.size(EsConstant.PRODUCT_PAGESIZE);
        //2.3、高亮
        if (!StringUtils.isEmpty(param.getKeyword())) {
            HighlightBuilder builder = new HighlightBuilder();
            builder.field("skuTitle");
            builder.preTags("<b style='color:red'>");
            builder.postTags("</b>");
            sourceBuilder.highlighter(builder);
        }

        /**
         * 聚合分析
         */
        //1、品牌聚合
        final TermsAggregationBuilder brandAgg = AggregationBuilders.terms("brand_agg");
        brandAgg.field("brandId").size(50);
        //1.1 子聚合-品牌名称
        brandAgg.subAggregation(AggregationBuilders.terms("brand_name_agg").field("brandName").size(1));
        //1.2子聚合-品牌logo
        brandAgg.subAggregation(AggregationBuilders.terms("brand_img_agg").field("brandImg").size(1));
        // TODO 1、聚合brand
        sourceBuilder.aggregation(brandAgg);

        //2、分类聚合
        final TermsAggregationBuilder catalogAgg = AggregationBuilders.terms("catalog_agg").field("catalogId").size(20);
        //2.1 分类名称
        catalogAgg.subAggregation(AggregationBuilders.terms("catalog_name_agg").field("catalogName").size(1));
        // TODO 2、聚合catalog
        sourceBuilder.aggregation(catalogAgg);

        //3、属性聚合
        final NestedAggregationBuilder attrAgg = AggregationBuilders.nested("attr_agg", "attrs");

        //3.1 聚合出当前所有的attrId
        final TermsAggregationBuilder attrIdAgg = AggregationBuilders.terms("attr_id_agg").field("attrs.attrId");

        //3.2 所有属性值
        attrIdAgg.subAggregation(AggregationBuilders.terms("attr_name_agg").field("attrs.attrName").size(1));
        attrIdAgg.subAggregation(AggregationBuilders.terms("attr_value_agg").field("attrs.attrValue").size(50));
        attrAgg.subAggregation(attrIdAgg);

        // TODO 3、聚合attr
        sourceBuilder.aggregation(attrAgg);

        System.out.println("构建结果=" + sourceBuilder.toString());

        final SearchRequest searchRequest = new SearchRequest(new String[]{EsConstant.PRODUCT_INDEX}, sourceBuilder);
        return searchRequest;
    }


    /**
     * 解析并构建放回数据
     *
     * @param search:
     * @Author: z_dd
     * @Date: 2023/6/19 21:31
     * @return: com.atguigu.gulimall.search.vo.SearchResult
     * @Description:
     **/
    private SearchResult builderSearchResult(SearchResponse search, SearchParam param) {
        SearchResult result = new SearchResult();

        final SearchHits hits = search.getHits();
        final SearchHit[] hitsDatas = hits.getHits();
        List<SkuEsModel> esModels = new ArrayList<>();
        if (hitsDatas != null && hitsDatas.length > 0) {
            for (SearchHit hitsData : hitsDatas) {
                String sourceAsString = hitsData.getSourceAsString();
                SkuEsModel skuEsModel = JSON.parseObject(sourceAsString, SkuEsModel.class);
                if (!StringUtils.isEmpty(param.getKeyword())) {
                    HighlightField skuTitle = hitsData.getHighlightFields().get("skuTitle");
                    String string = skuTitle.getFragments()[0].string();
                    skuEsModel.setSkuTitle(string);
                }
                esModels.add(skuEsModel);
            }
        }
        result.setProducts(esModels);

        //2、当前所有商品涉及到的所有属性信息
        final Aggregations aggregations = search.getAggregations();
        final ParsedNested attrAgg = aggregations.get("attr_agg");
        final ParsedLongTerms attrIdAgg = attrAgg.getAggregations().get("attr_id_agg");
        List<SearchResult.AttrVo> attrVoList = new ArrayList<>();
        for (Terms.Bucket bucket : attrIdAgg.getBuckets()) {
            SearchResult.AttrVo attrVo = new SearchResult.AttrVo();
            //1、得到属性的id
            final long attrId = bucket.getKeyAsNumber().longValue();

            //2.得到属性的名字
            final String attrName = ((ParsedStringTerms) bucket.getAggregations().get("attr_name_agg")).getBuckets().get(0).getKeyAsString();
            //3.得到属性的值
            final List<String> attrValue = ((ParsedStringTerms) bucket.getAggregations().get("attr_value_agg")).getBuckets().stream().map(MultiBucketsAggregation.Bucket::getKeyAsString).collect(Collectors.toList());
            attrVo.setAttrId(attrId);
            attrVo.setAttrName(attrName);
            attrVo.setAttrValue(attrValue);

            attrVoList.add(attrVo);
        }
        result.setAttrs(attrVoList);

        //3、当前所有商品的分类信息
        final ParsedLongTerms catalogAgg = search.getAggregations().get("catalog_agg");
        List<SearchResult.CatalogVo> catalogVos = new ArrayList<>();
        for (Terms.Bucket bucket : catalogAgg.getBuckets()) {
            SearchResult.CatalogVo catalogVo = new SearchResult.CatalogVo();
            // 得到分类id
            final long catalogId = bucket.getKeyAsNumber().longValue();
            catalogVo.setCatalogId(catalogId);

            // 得到分类名
            final ParsedStringTerms catalogNameAgg = bucket.getAggregations().get("catalog_name_agg");
            final String catalogName = catalogNameAgg.getBuckets().get(0).getKeyAsString();
            catalogVo.setCatalogName(catalogName);
            catalogVos.add(catalogVo);
        }
        result.setCatalogs(catalogVos);

        //4、当前所有商品的品牌信息
        List<SearchResult.BrandVo> brandVos = new ArrayList<>();
        final ParsedLongTerms brandAgg = search.getAggregations().get("brand_agg");
        for (Terms.Bucket bucket : brandAgg.getBuckets()) {

            SearchResult.BrandVo brandVo = new SearchResult.BrandVo();
            // 4.1、得到品牌的id
            final Long brandId = bucket.getKeyAsNumber().longValue();
            brandVo.setBrandId(brandId);

            // 4.2、得到品牌的姓名
            final String brandName = ((ParsedStringTerms) bucket.getAggregations().get("brand_name_agg")).getBuckets().get(0).getKeyAsString();
            brandVo.setBrandName(brandName);

            // 4.3、得到品牌的图片
            String brandImg = ((ParsedStringTerms) bucket.getAggregations().get("brand_img_agg")).getBuckets().get(0).getKeyAsString();
            brandVo.setBrandImg(brandImg);

            brandVos.add(brandVo);
        }
        result.setBrands(brandVos);

        //5、分页信息 - 总记录数
        long total = hits.getTotalHits().value;
        result.setTotal(total);
        //6、分页信息 - 页码
        result.setPageNum(param.getPageNum());
        //7、分页信息 - 总页码
        int totalPages = (int) total % EsConstant.PRODUCT_PAGESIZE == 0 ? (int) total / EsConstant.PRODUCT_PAGESIZE : ((int) total / EsConstant.PRODUCT_PAGESIZE + 1);
        result.setTotalPages(totalPages);

        List<Integer> pageNavs = new ArrayList<>();
        for(int i = 1; i <= totalPages; i++) {
            pageNavs.add(i);
        }
        result.setPageNavs(pageNavs);

        return result;
    }
}
