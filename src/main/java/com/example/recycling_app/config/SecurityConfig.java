package com.example.recycling_app.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity // 스프링 시큐리티 활성화
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // CSRF 보호 비활성화 (API 서버의 경우 흔함)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/waste-guides/**").permitAll() // /api/waste-guides 경로는 인증 없이 허용
                        .anyRequest().authenticated() // 그 외의 모든 요청은 인증 필요
                );
        return http.build();
    }
}
