//package com.bookrecommend.book_recommend_be.config;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.cors.CorsConfiguration;
//import org.springframework.web.cors.CorsConfigurationSource;
//import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
//
//import java.util.Arrays;
//import java.util.List;
//
//@Configuration
//public class CorsConfig {
//
//    @Bean
//    public CorsConfigurationSource corsConfigurationSource(
//            @Value("${cors.allowed-origins}") String allowedOrigins
//    ) {
//        CorsConfiguration cfg = new CorsConfiguration();
//        cfg.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
//        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
//        cfg.setAllowedHeaders(List.of("*"));
//        cfg.setAllowCredentials(true);
//
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", cfg);
//        return source;
//    }
//}
//
package com.bookrecommend.book_recommend_be.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${cors.allowed-origins}")
    private String allowedOrigins;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins.split(","))
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/books/**")
                .addResourceLocations("file:" + uploadDir + "/");
    }
}
