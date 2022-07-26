package com.alistair.community.controller;

import com.alistair.community.entity.User;
import com.alistair.community.service.AlphaService;
import com.alistair.community.util.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

@Controller
@RequestMapping("/alpha")/*/alpha 是浏览器访问名*/
public class AlphaController {

    @Autowired
    private AlphaService alphaService;

    @RequestMapping("/data")
    @ResponseBody
    public String getData(){
        return alphaService.find();
    }

    @RequestMapping("/hello")
    @ResponseBody
    public String sayHello(){
        return "Hello Spring Boot";
    }


    @RequestMapping("/http")
    public void http(HttpServletRequest request, HttpServletResponse response) throws IOException {
        //获取请求数据
        System.out.println(request.getMethod());
        System.out.println(request.getServletPath());
        Enumeration<String> enumeration = request.getHeaderNames();
        while(enumeration.hasMoreElements()){
            String name = enumeration.nextElement();
            String value = request.getHeader(name);
            System.out.println(name + ": " + value);
        }
        System.out.println(request.getParameter("code"));

        //返回响应数据
        response.setContentType("text/html;charset=utf-8");
        PrintWriter printWriter  = response.getWriter();
        printWriter.write("<h1>Alistair</h1>");
        printWriter.close();
    }

    // /students?current=1&limit=20
    @RequestMapping(path = "/students", method = RequestMethod.GET)
    @ResponseBody
    public String getStudents(
            @RequestParam(name = "current", required = false, defaultValue = "1") int  current,
            @RequestParam(name = "limit", required = false, defaultValue = "10") int limit){

        System.out.println(current);
        System.out.println(limit);
        return "some students";
    }

    // /student/201908010118
    @RequestMapping(path = "/student/{id}", method = RequestMethod.GET)
    @ResponseBody
    public String getStudent(@PathVariable("id") int id){
        System.out.println(id);
        return "a student  " + id;
    }

    //与post同path的GET请求
    @RequestMapping(path = "/student", method = RequestMethod.GET)
    public String getStudentPage() {
        return "/html/student.html";
    }

    // POST请求
    @RequestMapping(path = "/student", method = RequestMethod.POST)
    @ResponseBody
    public String saveStudent(String name, int age) {
        //一般这个时候会存入数据库
        System.out.println(name);
        System.out.println(age);
        return "success";
    }

    // 响应HTML数据
    @RequestMapping(path = "/teacher", method = RequestMethod.GET)
    public ModelAndView getTeacher(){
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("name","李老师");
        modelAndView.addObject("discipline", "语文");
        modelAndView.setViewName("/demo/teacher");
        return modelAndView;
    }

    //另一种方式响应数据
    @RequestMapping(path = "/school", method = RequestMethod.GET)
    public String getSchool(Model model){
        model.addAttribute("name","hnu");
        model.addAttribute("location","长沙");
        return "/demo/school";
    }

    //响应json数据
    @RequestMapping(path = "/emp", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> getEmp(){

        Map<String, Object> emp = new HashMap<>();
        emp.put("name","Smith");
        emp.put("salary", 800.00);
        emp.put("job", "clerk");

        return emp;

    }

    //响应json数据
    @RequestMapping(path = "/emps", method = RequestMethod.GET)
    @ResponseBody
    public List<Map<String, Object>> getEmps(){

        List<Map<String, Object>> emps = new ArrayList<>() ;
        Map<String, Object> emp1 = new HashMap<>();
        emp1.put("name","Smith");
        emp1.put("salary", 800.00);
        emp1.put("job", "clerk");

        Map<String, Object> emp2 = new HashMap<>();
        emp2.put("name","Bob");
        emp2.put("salary", 5000.00);
        emp2.put("job", "boss");

        emps.add(emp1);
        emps.add(emp2);

        return emps;
    }

    //Cookie示例
    @RequestMapping(path = "/cookie/set", method = RequestMethod.GET)
    @ResponseBody
    public String setCookie(HttpServletResponse response){
        //创建cookie
        Cookie cookie = new Cookie("code", CommunityUtil.generateUUID());
        //设置生效范围
        cookie.setPath("/community/alpha");
        //设置生存时间 10min
        cookie.setMaxAge(60 * 10);
        //将cookie添加到header
        response.addCookie(cookie);

        return "set Cookie";
    }

    @RequestMapping(path = "/cookie/get", method = RequestMethod.GET)
    @ResponseBody
    public String getCookie(@CookieValue("code") String code){
        System.out.println(code);
        return "get cookie";
    }

    //session示例
    @RequestMapping(path = "/session/set", method = RequestMethod.GET)
    @ResponseBody
    public String setSession(HttpSession session){
        session.setAttribute("id", 1);
        session.setAttribute("name", "Test");
        return "set session";
    }

    //session示例
    @RequestMapping(path = "/session/get", method = RequestMethod.GET)
    @ResponseBody
    public String getSession(HttpSession session){
        System.out.println(session.getAttribute("id"));
        System.out.println(session.getAttribute("name"));
        return "get session";
    }

    //ajax示例
    @RequestMapping(path = "/ajax", method = RequestMethod.POST)
    @ResponseBody
    public String ajaxTest(String name, int age) {
        System.out.println(name);
        System.out.println(age);
        return CommunityUtil.getJsonString(0,"操作成功");
    }

    //测试参数处理
    //param/test?username=liweijia&userId=166
    @RequestMapping(path = "/param/test", method = RequestMethod.GET)
    @ResponseBody
    public String paramTest(User user, int intParam) {
        return user.getUsername() + intParam;
    }
}
