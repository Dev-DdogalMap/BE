package com.ddogalmap.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 실제 파일이 저장된 로컬 경로와 URL 경로를 매핑
        // FileService의 uploadPath와 일치해야 함
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:///E:/백엔드8회차/4차협업프젝/ddogalmap/TestDir/")
                .setCachePeriod(3600);
    }
}
