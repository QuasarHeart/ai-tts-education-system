package org.example.fuchuang.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. 关闭 CSRF 防御
                // 因为我们现在是用 Postman 这种非浏览器工具测试，开启它会导致 POST 请求被拦截
                .csrf(csrf -> csrf.disable())

                // 2. 配置路径放行规则
                .authorizeHttpRequests(auth -> auth
                        // 让所有以 /api/auth/ 开头的请求都不需要登录即可访问
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        // 剩下的其他请求（如果有的话）依然需要登录
                        .anyRequest().authenticated()
                );

        return http.build();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}