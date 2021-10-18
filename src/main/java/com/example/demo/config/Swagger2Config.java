package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author Zheng kangqiang
 * @createTime 2021/10/18
 * @description Swagger 配置类
 */
@Configuration
@EnableSwagger2
public class Swagger2Config {
    @Bean
    public Docket createRestApi() {

        ApiInfo apiInfo = new ApiInfoBuilder()
                .title("这里是swagger的标题")
                .description("这里是描述内容")
                .contact(new Contact("Z", "", ""))
                .termsOfServiceUrl("http://localhost:18099/") //* 地址 用于显示 不会影响
                .version("1.0")
                .build();

        return new Docket(DocumentationType.SWAGGER_2)
                .host("http://localhost:28099/")//* 地址 用于显示 不会影响
                .groupName("后台接口")
                .apiInfo(apiInfo)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.example.demo.controller"))
                .paths(PathSelectors.any())
                .build();
    }
}
