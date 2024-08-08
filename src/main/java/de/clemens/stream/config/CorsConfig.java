package de.clemens.stream.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        
        List<String> allowedOrigins = Arrays.asList(
                "https://localhost:5173",
                "https://127.0.0.1:5173",
                "https://192.168.2.113:5173",
                "https://192.168.2.106:5173",
                "https://192.168.2.110:5173"
        );

        List<String> allowedMethodes = Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS"
        );
        
        List<String> allowedHeaders = Arrays.asList(
                "Authorization", "Content-Type", "X-CSRF-TOKEN"
        );

        List<String> exposedHeaders = Arrays.asList(
                "Access-Control-Allow-Origin", "CSRF-TOKEN"
        );

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(allowedMethodes);
        configuration.setAllowedHeaders(allowedHeaders);
        configuration.setExposedHeaders(exposedHeaders);
        configuration.setAllowCredentials(true);
        
        source.registerCorsConfiguration("/**", configuration);
        
        return new CorsFilter(source);
    }
}
