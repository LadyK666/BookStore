package com.bookstore.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot 启动入口。
 * 说明：
 * - 为了“不影响现有后端”，我们不改动现有 dao/service 逻辑，只是在其之上增加一层 Web API。
 * - 现有 JavaFX 的 MainApp 仍然可以照常使用，这里是额外提供一套 HTTP 接口，供新的 React 前端调用。
 */
@SpringBootApplication(scanBasePackages = "com.bookstore")
public class WebApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebApplication.class, args);
    }
}


