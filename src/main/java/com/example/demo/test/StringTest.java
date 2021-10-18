package com.example.demo.test;

import com.example.demo.util.StringUtil;

/**
 * @author Zheng kangqiang
 * @createTime 2020/6/23
 * @description String 测试类
 */
public class StringTest {

    public static void main(String[] args) {
        String binally = StringUtil.toBinary("http://localhost:8080/test.png");

        System.out.println("binally = " + binally);

        String str = StringUtil.toString(binally);

        System.out.println("str = " + str);
    }
}
