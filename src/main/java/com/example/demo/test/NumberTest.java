package com.example.demo.test;

/**
 * @author Zheng kangqiang
 * @createTime 2020/6/24
 * @description
 */
public class NumberTest {
    public static void main(String[] args) {
        for(int i = 0;i<10000;i++){
            int j = i;
            int b = i;
            if(j != b){
                System.out.println("i = " + i);
            }
        }
    }
}
