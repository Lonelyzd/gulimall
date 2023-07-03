package com.atguigu.gulimall.search.service;

import com.atguigu.gulimall.search.vo.SearchParam;
import com.atguigu.gulimall.search.vo.SearchResult;

/**
 * @author: z_dd
 * @date: 2023/6/14 21:26
 * @Description:
 */
public interface MallSearchService {
    SearchResult search(SearchParam param);
}
