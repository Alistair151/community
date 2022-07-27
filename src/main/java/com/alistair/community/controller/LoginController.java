package com.alistair.community.controller;

import com.alistair.community.entity.User;
import com.alistair.community.service.UserService;
import com.alistair.community.util.CommunityConstant;
import com.google.code.kaptcha.Producer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

@Controller
public class LoginController implements CommunityConstant {

    //日志对象
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private UserService userService;

    @Value("${server.servlet.context-path}")
    private String contextPath;


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

    //验证码的Bean
    @Autowired
    private Producer kaptchaProducer;


    //生成验证码
    @RequestMapping(path = "/kaptcha", method = RequestMethod.GET)
    public void getKaptcha(HttpServletResponse response, HttpSession session){
        //生成验证码(字符串)
        String text = kaptchaProducer.createText();
        //生成图片
        BufferedImage image = kaptchaProducer.createImage(text);
        //将验证码存入session
        session.setAttribute("kaptcha", text);
        //将图片输出给浏览器
        response.setContentType("image/png");
        try {
            OutputStream os = response.getOutputStream();
            ImageIO.write(image,"png", os);
        } catch (IOException e) {
            logger.error("响应验证码失败"+e.getMessage());
        }
    }

    /**
     *
     * @param username 账号
     * @param password 密码
     * @param code 验证码
     * @param rememberMe 用户登录时是否点击了“记住我”
     * @param model “将参数传入前端页面时需要model”
     * @param session “验证码存在session中”
     * @param response ticket用cookie来保存，cookie有response来创建
     * @return 前端页面的路径
     */
    @RequestMapping(path = "/login", method = RequestMethod.POST)
    public String login(String username, String password, String code, boolean rememberMe,
                        Model model, HttpSession session , HttpServletResponse response){

        //检查验证码
        String kaptcha = (String) session.getAttribute("kaptcha");
        if(StringUtils.isBlank(kaptcha) || StringUtils.isBlank(code) || !kaptcha.equalsIgnoreCase(code)){
            model.addAttribute("codeMsg", "验证码不正确");
            //验证码错误，回到登录界面
            return "/site/login";
        }

        //设置过期时间
        int expiredSecond = rememberMe?REMEMBER_EXPIRED_SECOND:DEFAULT_EXPIRED_SECOND;

        //检查账号密码
        Map<String, Object> map = userService.login(username, password, expiredSecond);
        if(map.containsKey("ticket")){
            //包含ticket说明账号密码没问题
            //将ticket以cookie的方式发给客户端
            Cookie cookie = new Cookie("ticket", map.get("ticket").toString());
            cookie.setPath(contextPath);
            cookie.setMaxAge(expiredSecond);
            response.addCookie(cookie);
            return "redirect:/index";
        }else{
            //没有ticket有错误
            model.addAttribute("usernameMsg", map.get("usernameMsg"));
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "/site/login";
        }

    }

    @RequestMapping(path = "/logout", method = RequestMethod.GET)
    public String logout(@CookieValue("ticket") String ticket){
        userService.logout(ticket);
        //重定向时默认get请求
        return "redirect:/login";
    }
}
