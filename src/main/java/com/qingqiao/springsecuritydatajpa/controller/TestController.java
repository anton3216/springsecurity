package com.qingqiao.springsecuritydatajpa.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/admin/test")
    public String testAdmin(){
        return "admin test";
    }

    @GetMapping("/user/test")
    public String testUser(){
        return "user test";
    }

    @GetMapping("/test")
    public String test(){
        return "default test";
    }
}
