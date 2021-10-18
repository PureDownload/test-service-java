package com.example.demo.controller;

import com.example.demo.model.Test;
import com.example.demo.model.User;
import org.springframework.web.bind.annotation.*;

/**
 * @author Zheng kangqiang
 * @createTime 2020/6/10
 * @description aa
 */
@RequestMapping("/test")
@RestController
@CrossOrigin
public class TestController {

    @GetMapping
    public String get(){
        return "OK";
    }

    @PostMapping
    public String post(@RequestBody User user,String name,String password){
        System.out.println("user = " +  user+"-----" +name+"-----"+password);
        return "OK";
    }

    @RequestMapping("/{test}")
    public String test(String test){
        System.out.println("test = " + test);
        return test;
    }
}
