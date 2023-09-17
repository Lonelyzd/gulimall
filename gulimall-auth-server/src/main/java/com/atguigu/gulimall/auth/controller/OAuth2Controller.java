package com.atguigu.gulimall.auth.controller;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.HttpUtils;
import com.atguigu.common.utils.R;
import com.atguigu.gulimall.auth.feign.MemberFeignService;
import com.atguigu.gulimall.auth.vo.MemberResponseVo;
import com.atguigu.gulimall.auth.vo.SocialUser;
import com.google.common.collect.ImmutableBiMap;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;

/**
 * @author: z_dd
 * @date: 2023/9/10 19:51
 * @Description:
 */
@Controller
public class OAuth2Controller {

    @Autowired
    private MemberFeignService memberFeignService;

    /**
     * QQ鉴权 因为QQ的回调接口参数前面有'#'，导致SpringMVC接收不到参数，所以先用这个接口中转处理下
     *
     * @param request
     * @param response
     * @throws Exception
     */
    @RequestMapping("/success")
    public void authQQ(HttpServletRequest request, HttpServletResponse response) throws Exception {
        // QQ登录有点特殊，参数放在#后面，后台无法获取#后面的参数，只能用JS做中间转换
        String html = "<!DOCTYPE html>" +
                "<html lang=\"zh-cn\">" +
                "<head>" +
                "   <title>QQ登录重定向页</title>" +
                "   <meta charset=\"utf-8\"/>" +
                "</head>" +
                "<body>" +
                "   <script type=\"text/javascript\">" +
                "   location.href = location.href.replace('#', '&').replace('success', 'success1');" +
                "   </script>" +
                "</body>" +
                "</html>";
        response.getWriter().print(html);
    }

    /**
     * 真正的登录验证
     *
     * @param accessToken:
     * @param expiresIn:
     * @Author: z_dd
     * @Date: 2023/9/10 19:57
     * @return: java.lang.String
     * @Description:
     **/
    @GetMapping("/success1")
    @ResponseBody
    public String success(@RequestParam("access_token") String accessToken, @RequestParam("expires_in") String expiresIn, HttpSession session) throws Exception {

        final HttpResponse httpResponse = HttpUtils.doGet("https://graph.qq.com", "/oauth2.0/me", "GET", new HashMap<>(), ImmutableBiMap.of("access_token", accessToken));

        if (httpResponse.getStatusLine().getStatusCode() == 200) {
            final String entityString = EntityUtils.toString(httpResponse.getEntity());
            final JSONObject jsonObject = ConvertToJson(entityString);

            final SocialUser socialUser = JSONObject.parseObject(jsonObject.toJSONString(), SocialUser.class);
            socialUser.setAccessToken(accessToken);

            //知道当前是哪个社交用户登录
            //1、 当前用户如果是第一次进入网站，自动注册（为当前社交用户进行注册）
            System.out.println(entityString);

            final R r = memberFeignService.oauthLogin(socialUser);
            final MemberResponseVo data = r.getData(new TypeReference<MemberResponseVo>() {
            });

            session.setAttribute("loginUser", data);

            return "redirect:http://icebule.top";

        } else {
            return "redirect:http://auth.gulimall.com/login.html";
        }
    }

    //QQ接口返回类型是text/plain，此处将其转为json
    public static JSONObject ConvertToJson(String string) {
        string = string.substring(string.indexOf("(") + 1, string.length());
        string = string.substring(0, string.indexOf(")"));
        JSONObject jsonObject = JSONObject.parseObject(string);
        return jsonObject;
    }
    /**
     *
     * // 根据accessToken换取openId
     * // 错误示例：callback( {"error":100016,"error_description":"access token check failed"} );
     * // 正确示例：callback( {"client_id":"10XXXXX49","openid":"CF2XXXXXXXX9F4C"} );
     * String result = HttpsUtil.get("https://graph.qq.com/oauth2.0/me?access_token=" + accessToken);
     * Map<String, Object> resp = parseQQAuthResponse(result); // 这个方法就是把结果转Map
     *
     * Integer errorCode = (Integer)resp.get("error");
     * String errorMsg = (String)resp.get("error_description");
     * String openId = (String)resp.get("openid");
     * if(errorCode != null) return new ErrorResult(errorCode, "获取QQ用户openId失败："+errorMsg);
     *
     * 获取用户头像昵称等信息:
     * // 获取用户昵称、头像等信息，{ret: 0, msg: '', nickname: '', ...} ret不为0表示失败
     * result = HttpsUtil.get("https://graph.qq.com/user/get_user_info?access_token="+accessToken+"&oauth_consumer_key="+appId+"&openid="+openId);
     * resp = JsonUtil.parseJsonToMap(result);
     *
     * Integer ret = (Integer)resp.get("ret");
     * String msg = (String)resp.get("msg");
     * if(ret != 0) return new ErrorResult("获取用户QQ信息失败："+msg);
     *
     * // 用户昵称可能存在4个字节的utf-8字符，MySQL默认不支持，直接插入会报错，所以过滤掉
     * String nickname = StringUtil.filterUtf8Mb4((String)resp.get("nickname")).trim(); // 这个方法可以自行百度
     * // figureurl_qq_2=QQ的100*100头像，figureurl_2=QQ 100&100空间头像，QQ头像不一定有，空间头像一定有
     * String avatar = (String)resp.get("figureurl_qq_2");
     * if(StringUtil.isBlank(avatar)) avatar = (String)resp.get("figureurl_2");
     * String gender = (String)resp.get("gender");
     *
     **/


}
