package com.example.progrest.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    @Value("${api.path.show}")
    private String show_path;

    @Override
    public void addResourceHandlers(@SuppressWarnings("null") ResourceHandlerRegistry registry) {
        // ให้ Spring Boot จัดการไฟล์จากโฟลเดอร์ static ของโปรเจกต์
        registry.addResourceHandler("/**").addResourceLocations("classpath:/static/");

        // ใช้ show_path ที่กำหนดใน application.properties เพื่อให้บริการไฟล์จากโฟลเดอร์ภายนอก
        registry.addResourceHandler("/files/**").addResourceLocations(show_path);
    }
}
