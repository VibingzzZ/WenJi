package com.example.exception;

import com.example.common.Result;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常处理
     * @param e 业务异常对象
     * @return 异常结果
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        e.printStackTrace();
        return Result.errorWithCode(e.getMessage(), e.getCode());
    }
    
    /**
     * 全局异常处理
     * @param e 异常对象
     * @return 异常结果
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        e.printStackTrace();
        //如果有异常就返回异常信息，反之就返回操作失败
        return Result.errorWithCode(StringUtils.hasLength(e.getMessage()) ? e.getMessage() : "操作失败", 500);
    }
}
