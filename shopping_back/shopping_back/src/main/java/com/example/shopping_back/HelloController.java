package com.example.shopping_back;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

@RestController
@CrossOrigin // 允许 uni-app 跨域访问
public class HelloController {

    @GetMapping("/api/hello")
    public String sayHello() {
        return "后端连接成功！二手交易平台正式启动！";
    }
}