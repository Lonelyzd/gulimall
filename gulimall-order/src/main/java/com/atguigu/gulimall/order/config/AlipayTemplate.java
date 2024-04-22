package com.atguigu.gulimall.order.config;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.atguigu.gulimall.order.vo.PayVo;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "alipay")
@Component
@Data
public class AlipayTemplate {

    //在支付宝创建的应用的id
    private   String app_id = "9021000135677606";

    // 商户私钥，您的PKCS8格式RSA2私钥
    private  String merchant_private_key ="MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC+euY6z1HILOqjp8DlMzJOMBnrdjpY8raRiZwZ8UgBX9PEGyxA5K0Fad9x/kE5TvgS54WkesBDOXlPf4wWc6gTp4zGIG1AWyYOSZv+inkXMC221ZEYpb/i7tlGINC1DY3/CdObEoB03vZSjglzf00kSJK4pk/3R23tZyXHziadpI6znU0VquXGH5leWV5tjOmMbRxYWMDFNvDsZ6So9doO/OnPN6RpnUhXUQ6apaF0PlMGwxbInHBcX6oJ6yDiFRqn2+WpvFJ1zldpDO4y9MyFY7meT1MrrdJBSC/XMt/zPA9/IvrnHvYIm/gkE1netKAQXuOHIda7hbaEgb4T5o2rAgMBAAECggEBAIlgVd+gWllGm8uAV0x2ihd99ZOuZxJzqlx4pM7JB6eGF1LeTiJXSroWy4KJXz0XyS7JL8eaiENzlhxR3xTqQRQ7yPmztVeu5Vl0mT8RW5fvtWihys297RCcNrOvQxbxObHwwl6bD246x+hubD6SV26NsNPfjtjG+W847zSVZ6BVJsAbeCj+OS6fqXDT5eM3A1EXNTeTD8Lb3i8X4Wup90LBDn1jq02SzNk4pA7RURuDSm8igGNCVXvVH39KA5eXqZd0+7DOJFrMPd8fFa8yClCuRZ+lZbkuPgsHQk8ku9LXo6uBE+zG3uZiwi9uUEJ1XYX8x+FMtJ282kVcXN5dKUECgYEA/dBIUW4yANLsze6p/Q1vIzX7PkZQqbfW0yYPem4f8x/EYUEl01s/XmEwDzUI9/rEGBnkEBEDWGZUdRV5QVT7CBQ3Y8itrmCuzvyS9C26CEt7jq0+zRZhB3aykL+Lotywx5N0tr8jAPbHzPf/Geey4ZE++VPblSgmKRXe6Xn205ECgYEAwB7zqgIw1jwxWodIDA3cmAdrlUMiCXrTvt4hvZsbCisGOK+YQK/rXCUL4Ogqllyg/Qx/GQ69dCHg/HOIOfLt+oilCvEGYWTKpbT40ZIxIN789tEoaZSMHHiOHkW6cGcr/0XgmKNe6YbD8xef/8pCtKHBALsm2o7ZJSX69f5O93sCgYAHS+GIVb6iP24HTYEdgTFctcSPOb4/4sUONN3lCx1oA9XFZfYMaWcc83ZKOaAUYOUaPEROIe8Sr35mD0P5GNROmyYsfCxiAu2Djcc7tcTPUDwxEdHoW2hWqpbfimIdQHLkidylFEGRWYbM4aQ3vm1qBp7k1ABP/WN7cuGBnLv3wQKBgGR5S3EjyCqiPCVeDn3PWghgCRF8lHZkuAxf20FvtopwycYnfvaFig8ciMPMZ0lPlBoCyQ1vugDcHu+n9BxqQa7+e2HnfzC8J4bVyDLBw9OvCpiB9iKRP108ZwbJ7KEQM+BCyRzKg76ZbizZtCFvw8b3uRoKxl0Sy9kzrbWsk9u1AoGAK5+fxZ3aYXHFcPTP3MjzGUKRc5LgTwV2YuvD6ffzSrkkrJc4DCyJjzTO4FilLFmXi3qM6wO/VNS0vmlalFOP59UfEtOM+tTB+5+AmmTywQ/hA+Maes3YRqrW52jZ2oPWBwINb5zomsHXGw/lkEjouYek0pqAy6dI/gzThmqOcGc=";
    // 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    private  String alipay_public_key ="MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAh1TKyososBpS1IDJ60LYIQRLt6wgkoX3P31QcgOKfpAeqKDEsKPfY9/OBpDKgi00mnk3wqdzNp8u8JPnWEVVpgc8z0g4FAnUWplmoF/UzOn74Fb0Mj75vI8d73l9x+3Nb5uNKpAm40TOO3knSI3Mjgys6bIJ60gmpjRi6c/uvg+RTufg0B0J8W5VjO5WLMgPKy/9U0tCBWv30Hxh3kRDohTM+ufNHch3cv8cMSJBVi8K03NCUBacXNV32+iRbbrJjmgwSFYdCC8CldiMOjg6mTBUXyKizG4aYXCPfxsdKAeaq3gcqt7ukom7xEPN3AjpLAYARUkENuXMy0xfvaSwRQIDAQAB";
    // 服务器[异步通知]页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    // 支付宝会悄悄的给我们发送一个请求，告诉我们支付成功的信息
    private  String notify_url ="http://cn-hk-bgp-4.of-7af93c01.shop:45378/payed/notify";;

    // 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    //同步通知，支付成功，一般跳转到成功页
    private  String return_url="http://member.gulimall.com/memberOrder.html";;

    // 签名方式
    private  String sign_type = "RSA2";

    // 字符编码格式
    private  String charset = "utf-8";

    // 支付宝网关； https://openapi.alipaydev.com/gateway.do
    private  String gatewayUrl = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";

    private String timeout="30m";

    public  String pay(PayVo vo) throws AlipayApiException {

        //AlipayClient alipayClient = new DefaultAlipayClient(AlipayTemplate.gatewayUrl, AlipayTemplate.app_id, AlipayTemplate.merchant_private_key, "json", AlipayTemplate.charset, AlipayTemplate.alipay_public_key, AlipayTemplate.sign_type);
        //1、根据支付宝的配置生成一个支付客户端
        AlipayClient alipayClient = new DefaultAlipayClient(gatewayUrl,
                app_id, merchant_private_key, "json",
                charset, alipay_public_key, sign_type);

        //2、创建一个支付请求 //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(return_url);
        alipayRequest.setNotifyUrl(notify_url);

        //商户订单号，商户网站订单系统中唯一订单号，必填
        String out_trade_no = vo.getOut_trade_no();
        //付款金额，必填
        String total_amount = vo.getTotal_amount();
        //订单名称，必填
        String subject = vo.getSubject();
        //商品描述，可空
        String body = vo.getBody();

        alipayRequest.setBizContent("{\"out_trade_no\":\""+ out_trade_no +"\","
                + "\"total_amount\":\""+ total_amount +"\","
                + "\"subject\":\""+ subject +"\","
                + "\"body\":\""+ body +"\","
                + "\"timeout_express\":\""+ timeout +"\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

        String result = alipayClient.pageExecute(alipayRequest).getBody();

        //会收到支付宝的响应，响应的是一个页面，只要浏览器显示这个页面，就会自动来到支付宝的收银台页面
        System.out.println("支付宝的响应："+result);

        return result;

    }
}
