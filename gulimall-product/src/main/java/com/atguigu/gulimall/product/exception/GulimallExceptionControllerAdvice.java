package com.atguigu.gulimall.product.exception;

import com.atguigu.common.exception.BizCodeEnum;
import com.atguigu.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: z_dd
 * @date: 2023/3/4 20:11
 * @Description: 统一异常处理
 */
@Slf4j
@RestControllerAdvice
public class GulimallExceptionControllerAdvice {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R handleVaildException(MethodArgumentNotValidException e){
        log.error("请求参数校验异常",e);
        Map<String,String> errorMap=new HashMap<>();
        final BindingResult bindingResult = e.getBindingResult();

        bindingResult.getFieldErrors().forEach(field->
            errorMap.put(field.getField(),field.getDefaultMessage())
        );

        return R.error(BizCodeEnum.VAILD_EXCEPTION.getCode(), BizCodeEnum.VAILD_EXCEPTION.getMsg()).put("data",errorMap);
    }

    @ExceptionHandler(Throwable.class)
    public R handleException(Throwable e){
        log.error("其他异常" ,e);
        return R.error(BizCodeEnum.UNKNOW_EXCEPTION.getCode(), BizCodeEnum.UNKNOW_EXCEPTION.getMsg());
    }
}
