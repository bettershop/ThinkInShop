package com.laiketui.core.config;

import com.laiketui.core.domain.Result;
import com.laiketui.core.exception.LaiKeAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;


//@ControllerAdvice
public class GlobalLaiKeAPIExceptionHandler
{

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

//    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<Result> handleException(HttpServletRequest request, Exception e)
    {

        // 打印请求上下文信息
        String path   = request.getRequestURI();
        String method = request.getMethod();

        // 处理业务自定义异常
        Object filterException = request.getAttribute("filterException");
        if (filterException instanceof LaiKeAPIException)
        {
            LaiKeAPIException ex = (LaiKeAPIException) filterException;
            logger.error("业务异常：请求路径 [{} {}]，code={}, message={}", method, path, ex.getCode(), ex.getMessage(), ex);
            return new ResponseEntity<>(Result.error(ex.getCode(), ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        else if (e instanceof LaiKeAPIException)
        {
            LaiKeAPIException ex = (LaiKeAPIException) e;
            logger.error("业务异常：请求路径 [{} {}]，code={}, message={}", method, path, ex.getCode(), ex.getMessage(), ex);
            return new ResponseEntity<>(Result.error(ex.getCode(), ex.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // 未知系统异常
        logger.error("系统异常：请求路径 [{} {}]，message={}", method, path, e.getMessage(), e);
        return new ResponseEntity<>(Result.error("20019", e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);

    }
}
