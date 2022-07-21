package com.alistair.community.controller;

import com.alistair.community.entity.User;
import com.alistair.community.service.UserService;
import com.alistair.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Map;

@Controller
public class LoginController implements CommunityConstant {

    @Autowired
    private UserService userService;


    //访问注册界面
    @RequestMapping(path = "/register", method = RequestMethod.GET)
    public String getRegisterPage(){
        return "/site/register";
    }

    //浏览器提交表单
    @RequestMapping(path = "/register", method = RequestMethod.POST)
    public String register(Model model, User user){
        Map<String, Object> map = userService.register(user);
        if(map == null || map.isEmpty()){
            //注册成功返回给operate——result页面的参数
            model.addAttribute("msg", "注册成功，已向您的邮箱发送激活邮件");
            model.addAttribute("target", "/index");
            return "site/operate-result";
        }else{
            //注册失败返回给register页面的参数
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("userPasswordMsg",map.get("userPasswordMsg"));
            model.addAttribute("userEmailMsg",map.get("userEmailMsg"));

            return "/site/register";
        }

    }

    //访问激活页面
    // http://localhost:8080/community/activation/id/code
    @RequestMapping(path = "/activation/{userId}/{code}", method = RequestMethod.GET)
    public String activation(Model model, @PathVariable("userId") int userId, @PathVariable("code") String code){
        int result = userService.activation(userId, code);
        if(result == ACTIVATION_SUCCESS){
            model.addAttribute("msg","激活成功，您的账号已经可以正常使用了");
            model.addAttribute("target", "/login");
        }else if (result == ACTIVATION_REPEAT){
            model.addAttribute("msg", "无效操作，该用户已经激活过");
            model.addAttribute("target", "/index");
        }else {
            model.addAttribute("msg", "激活失败，您的激活码不正确");
            model.addAttribute("target", "/index");
        }
        return "/site/operate-result";
    }

    //访问登录页面
    @RequestMapping(path = "/login", method = RequestMethod.GET)
    public String getLoginPage(){
        return "/site/login";
    }

}
