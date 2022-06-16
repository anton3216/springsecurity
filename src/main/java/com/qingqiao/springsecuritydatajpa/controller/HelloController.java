package com.qingqiao.springsecuritydatajpa.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class HelloController {

    @GetMapping("hello")
    public String hello(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object details = authentication.getDetails();
        System.out.println(details);
        // 0:0:0:0:0:0:0:1 ipv6
        // 127.0.0.1 localhost
        // 192.168.31.53 ipv4
        return "hello";
    }

    @GetMapping("pay")
    public String admin() {
        return "admin";
    }
    @GetMapping("rememberme")
    public String rememberme() {
        return "rememberme";
    }

}
