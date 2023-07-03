package com.atguigu.gulimall.search;

import com.alibaba.fastjson.JSON;
import com.atguigu.gulimall.search.config.GuliMallElasticSearchConfig;
import lombok.Data;
import lombok.ToString;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.aggregations.metrics.AvgAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.naming.directory.SearchResult;
import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallSearchApplicationTests {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Test
    public void contextLoads() {
        System.out.println(restHighLevelClient);
    }

    @Test
    public void indexData() throws IOException {
        IndexRequest indexRequest = new IndexRequest("users");
        User user = new User();
        user.setUserName("张三");
        user.setAge(20);
        user.setGender("男");
        String jsonString = JSON.toJSONString(user);
        //设置要保存的内容
        indexRequest.source(jsonString, XContentType.JSON);
        //执行创建索引和保存数据
        IndexResponse index = restHighLevelClient.index(indexRequest, GuliMallElasticSearchConfig.COMMON_OPTIONS);
        System.out.println(index);
    }

    /**
     * {
     * "query":{
     * "match":{
     * "address":{
     * "query":"Mill"
     * }
     * }
     * },
     * "aggregations":{
     * "ageAgg":{
     * "terms":{
     * "field":"age",
     * "size":100
     * }
     * },
     * "balanceAvg":{
     * "avg":{
     * "field":"balance"
     * }
     * }
     * }
     * }
     **/
    @Test
    public void searchData() throws IOException {
        
        //1.创建检索请求
        SearchRequest searchRequest = new SearchRequest();
        //指定索引
        searchRequest.indices("bank");
        //指定DSL，检索条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //1.1 构造检索条件
//        searchSourceBuilder.query();
//        searchSourceBuilder.from();
//        searchSourceBuilder.size();
//        searchSourceBuilder.aggregation();
        searchSourceBuilder.query(QueryBuilders.matchQuery("address", "Mill"));

        //1.2 按照年龄的值分布进行聚合
        final TermsAggregationBuilder ageAgg = AggregationBuilders.terms("ageAgg").field("age").size(100);
        searchSourceBuilder.aggregation(ageAgg);
        //1.3计算平均薪资
        final AvgAggregationBuilder ageAvg = AggregationBuilders.avg("balanceAvg").field("balance");
        searchSourceBuilder.aggregation(ageAvg);


        System.out.println(searchSourceBuilder);
        searchRequest.source(searchSourceBuilder);

        //2.执行检索
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, GuliMallElasticSearchConfig.COMMON_OPTIONS);

        //3.分析结果
        System.out.println(searchResponse);

        //3.1 获取所有查到的数据
        final SearchHits hits = searchResponse.getHits();
        final SearchHit[] searchHits = hits.getHits();

        for (SearchHit searchHit : searchHits) {
            //"_index":"bank",
            //"_type":"account",
            //"_id":"970",
            //"_score":5.4032025,
            //"_source"
            final String sourceAsString = searchHit.getSourceAsString();
            final Account account = JSON.parseObject(sourceAsString, Account.class);
            System.out.println(account);
        }

        //3.2 获取这次检索到的分析信息
        final Aggregations aggregations = searchResponse.getAggregations();
//        for (Aggregation aggregation : aggregations.asList()) {
//            System.out.println(aggregation.getName());
//        }
        final Terms ageAgg1 = aggregations.get("ageAgg");
        for (Terms.Bucket bucket : ageAgg1.getBuckets()) {
            final String keyAsString = bucket.getKeyAsString();
            System.out.println("年龄：" + keyAsString + "===>" + bucket.getDocCount());
        }
        final Avg ageAvg1 = aggregations.get("balanceAvg");

        System.out.println("平均薪资==" + ageAvg1.getValue());
    }

    @Data
    class User {
        private String userName;
        private Integer age;
        private String gender;
    }

    @Data
    @ToString
    static class Account {

        private int account_number;
        private int balance;
        private String firstname;
        private String lastname;
        private int age;
        private String gender;
        private String address;
        private String employer;
        private String email;
        private String city;
        private String state;
    }

}
