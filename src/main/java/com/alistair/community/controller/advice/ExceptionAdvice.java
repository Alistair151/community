package com.alistair.community.controller.advice;

import com.alistair.community.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@ControllerAdvice(annotations = Controller.class)
public class ExceptionAdvice {

    private static final Logger logger = LoggerFactory.getLogger(ExceptionAdvice.class);


    // 同意处理controller发出的异常
    @ExceptionHandler
    public void handleException(Exception e, HttpServletResponse response, HttpServletRequest request) throws IOException {
        // 记录日志
        logger.error("服务器发生异常" + e.getMessage());
        for (StackTraceElement element : e.getStackTrace()) {
            logger.error(element.toString());
        }

        //分为同步请求与异步请求
        String type = request.getHeader("x-requested-with");
        if(type.equals("XMLHttpRequest")) {
            response.setContentType("application/plain;charset=utf-8");
            PrintWriter writer = response.getWriter();
            writer.write(CommunityUtil.getJsonString(1, "服务器异常！请联系后台工作人员"));
        }else {
            //普通的同步请求, 在HomeController中有请求的GET方法
            response.sendRedirect(request.getContextPath() + "/error");
        }

    }

}
