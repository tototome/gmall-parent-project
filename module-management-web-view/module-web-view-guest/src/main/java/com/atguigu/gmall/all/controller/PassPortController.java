package com.atguigu.gmall.all.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PassPortController {
    /*login() {
     window.location.href =
     'http://passport.gmall.com/login.html?originUrl='+window.location.href
     }*/
    //登陆这个过程 是异步发送请求到微服务 为什么还需要 经过weball
    //因为登陆成功后需要返回到刚才他所访问的页面
    //前端页面也需要originUrl
    @RequestMapping("/login.html")
    public String login(@RequestParam String originUrl, Model model){
        model.addAttribute("originUrl",originUrl);
        return "login";
    }
}
