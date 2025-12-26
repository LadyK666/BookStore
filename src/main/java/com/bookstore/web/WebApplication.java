package com.bookstore.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * Spring Boot 启动入口。
 * 说明：
 * - 为了"不影响现有后端"，我们不改动现有 dao/service 逻辑，只是在其之上增加一层 Web API。
 * - 现有 JavaFX 的 MainApp 仍然可以照常使用，这里是额外提供一套 HTTP 接口，供新的 React 前端调用。
 */
@SpringBootApplication(scanBasePackages = "com.bookstore")
public class WebApplication implements WebMvcConfigurer {

    public static void main(String[] args) {
        SpringApplication.run(WebApplication.class, args);
    }

    /**
     * 配置静态资源映射，支持访问本地图片文件。
     * 图片文件应放在项目根目录的 static/images/ 目录下。
     * 访问路径：http://localhost:8080/images/covers/xxx.jpg
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 获取项目根目录
        String projectRoot = System.getProperty("user.dir");
        File imagesDir = new File(projectRoot, "static" + File.separator + "images");
        
        // 确保目录存在
        if (!imagesDir.exists()) {
            imagesDir.mkdirs();
            System.out.println("已创建图片目录: " + imagesDir.getAbsolutePath());
        }
        
        // 配置静态资源映射
        // 访问 /images/** 时，从项目根目录的 static/images/ 目录读取文件
        // 注意：file: 协议需要以 / 结尾（Windows上需要处理）
        String imagesPath = imagesDir.getAbsolutePath().replace("\\", "/");
        if (!imagesPath.endsWith("/")) {
            imagesPath += "/";
        }
        
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + imagesPath);
        
        System.out.println("静态资源映射已配置，图片目录: " + imagesPath);
        System.out.println("访问示例: http://localhost:8080/images/covers/图片文件名.jpg");
    }
}


