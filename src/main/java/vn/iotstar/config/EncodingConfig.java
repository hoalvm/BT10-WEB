package vn.iotstar.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CharacterEncodingFilter;

/**
 * Explicit UTF-8 encoding filter to ensure Vietnamese characters are handled correctly.
 * Named differently from Spring Boot's auto-configured 'characterEncodingFilter' to avoid conflict.
 */
@Configuration
public class EncodingConfig {

    @Bean(name = "utf8EncodingFilter")
    FilterRegistrationBean<CharacterEncodingFilter> utf8EncodingFilter() {
        CharacterEncodingFilter filter = new CharacterEncodingFilter();
        filter.setEncoding("UTF-8");
        filter.setForceEncoding(true);
        FilterRegistrationBean<CharacterEncodingFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setOrder(Integer.MIN_VALUE);
        return registration;
    }
}
