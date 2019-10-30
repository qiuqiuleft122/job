package com.icicle.framework.exception;

import entity.Result;
import entity.StatusCode;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @Author Max
 * @Date 16:10 2019/8/26
 * @Description：全局异常处理
 **/
@ControllerAdvice
public class BaseExceptionHandler {

    /**
     * 异常处理
     * @param e
     * @return
     */
    @ExceptionHandler(value = Exception.class)
    @ResponseBody  //返回渲染页面处理  将对象转成json
    public Result error(Exception e){
        e.printStackTrace();
        return new Result(false, StatusCode.ERROR,e.getMessage());
    }
}
