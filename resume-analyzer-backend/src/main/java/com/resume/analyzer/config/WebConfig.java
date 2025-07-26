package com.resume.analyzer.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOriginPatterns("http://localhost:8000")  // Use allowedOriginPatterns instead of allowedOrigins
            .allowedMethods("*")
            .allowedHeaders("*")
            .allowCredentials(true);
    }
}
