package com.alistair.community.controller.interceptor;

import com.alistair.community.entity.LoginTicket;
import com.alistair.community.entity.User;
import com.alistair.community.service.UserService;
import com.alistair.community.util.CookieUtil;
import com.alistair.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

@Component
public class LoginTicketInterceptor implements HandlerInterceptor {


    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    //每次请求都要获取用户信息
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //从cookie中获取凭证
        String ticket = CookieUtil.getValue(request, "ticket");


        if (ticket != null) {
            //查询凭证
            LoginTicket loginTicket = userService.findLoginTicket(ticket);
            //检查凭证是否有效
            if(loginTicket != null && loginTicket.getStatus() == 0
                    && loginTicket.getExpired().after(new Date())){
                User user = userService.findUserById(loginTicket.getUserId());
                //在本次请求中持有用户
                hostHolder.setUsers(user);
            }
        }
        return true;
    }

    //postHandle是在模板方法前调用的，能够将用户信息作用于模板引擎
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) throws Exception {
        //得到当前线程持有的user
        User user = hostHolder.getUsers();
        if(user != null && modelAndView != null){
            //将user加入到model中
            modelAndView.addObject("loginUser", user);
        }
    }

    //模板引擎作用完后调用方法，将实体清理掉
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) throws Exception {
        hostHolder.clear();
    }


}
