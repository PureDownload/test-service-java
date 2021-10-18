package com.example.demo.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author Zheng kangqiang
 * @createTime 2020/6/10
 * @description user
 */
@Data
@ApiModel(value = "用户类",description = "用户实体类")
public class User {
    @ApiModelProperty(value = "姓名",name =  "name",required =  true,example = "张三")
    String name;
    String password;
}
