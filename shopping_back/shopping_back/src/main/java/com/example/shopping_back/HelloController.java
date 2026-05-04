package com.example.shopping_back;

import com.example.shopping_back.common.dto.ApiResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/api/hello")
    public ApiResult<String> sayHello() {
        return ApiResult.ok("后端连接成功！二手交易平台正式启动！");
    }
}