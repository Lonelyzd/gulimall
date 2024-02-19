package com.atguigu.gulimall.order.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @author : z_dd
 * @date : 2024/2/13 17:13
 **/
@Controller
public class HelloController {

    @GetMapping("/{page}.html")
    public String listPage(@PathVariable String page) {
        return page;
    }
}
