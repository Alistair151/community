package com.alistair.community.controller;

import com.alistair.community.annotation.LoginRequired;
import com.alistair.community.entity.User;
import com.alistair.community.service.LikeService;
import com.alistair.community.service.UserService;
import com.alistair.community.util.CommunityUtil;
import com.alistair.community.util.HostHolder;
import com.alistair.community.util.RedisKeyUtil;
import com.sun.org.apache.xpath.internal.operations.Mod;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @LoginRequired
    @RequestMapping(value = "/setting", method = RequestMethod.GET)
    public String getSettingPage() {
        return "/site/setting";
    }

    @LoginRequired
    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model) {
        if(headerImage == null) {
            model.addAttribute("error", "您还没有选择图片");
            return "/site/setting";
        }

        String filename = headerImage.getOriginalFilename();
        String fileType = filename.substring(filename.lastIndexOf("."));
        if(StringUtils.isBlank(fileType)) {
            model.addAttribute("error","文件格式不正确");
            return "/site/setting";
        }

        //生成随机的文件名
        filename = CommunityUtil.generateUUID() + fileType;
        //确定文件存在的路径
        File dest = new File(uploadPath + "/" + filename);
        try {
            //存储文件
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败：" + e.getMessage());
            throw new RuntimeException("上传文件失败，服务器发生故障", e);
        }

        //更新当前头像的路径（web访问路径）
        //http://localhost:8080/community/user/header/xx.png
        User user = hostHolder.getUsers();
        String headUrl = domain + contextPath + "/user/header/" + filename;
        userService.updateHeader(user.getId(), headUrl);
        return "redirect:/index";
    }

    //通过web地址访问本地的文件
    //返回二进制，手动通过流输出，所以返回void
    @RequestMapping(path = "/header/{filename}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("filename") String filename, HttpServletResponse response) {
        // 服务器存放路径
        filename = uploadPath + "/" + filename;
        // 向浏览器输出图片，文件的后缀
        String fileType = filename.substring(filename.lastIndexOf("."));
        // 响应图片
        response.setContentType("image/" + fileType);
        try (FileInputStream fileInputStream = new FileInputStream(filename)){
            OutputStream os = response.getOutputStream();
            byte[] buffer = new byte[1024];
            int b;
            while((b= fileInputStream.read(buffer)) != -1){
                os.write(buffer, 0 , b);
            }

        } catch (IOException e) {
            logger.error("读取图像失败" + e.getMessage());
        }
    }

    //修改密码，提交表单
    @LoginRequired
    @RequestMapping(path = "/change", method = RequestMethod.POST)
    public String updatePassword(String oldPassword, String newPassword, Model model) {
        User user = hostHolder.getUsers();
        Map<String, Object> map = userService.updatePassword(user, oldPassword, newPassword);
        if(map != null && !map.isEmpty()){
            model.addAttribute("oldMsg", map.get("oldMsg"));
            model.addAttribute("newMsg", map.get("newMsg"));
            return "/site/setting";
        }

        return "redirect:/index";
    }

    // 个人主页
    @RequestMapping(path = "/profile/{userId}", method = RequestMethod.GET)
    public String getProfile(@PathVariable("userId") int userId, Model model){
        //个人信息
        User user = userService.findUserById(userId);
        model.addAttribute("user", user);

        //收到的赞
        Integer likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount", likeCount);

        return "/site/profile";

    }

}
