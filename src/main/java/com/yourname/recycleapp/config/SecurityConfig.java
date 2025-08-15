package com.yourname.recycleapp.config;

import com.yourname.recycleapp.security.FirebaseTokenFilter; // FirebaseTokenFilter import
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter; // import 추가

// Spring Security 설정 클래스
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // CSRF 비활성화 (JWT 사용 시)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 세션 사용 안함 (JWT 사용 시)
                .authorizeHttpRequests(authorize -> authorize
                        // 인증이 필요 없는 경로
                        .requestMatchers("/api/auth/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // 프로필 관련 API는 인증 필요
                        .requestMatchers("/api/profile/**", "/api/inquiry/**").authenticated()
                        // 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated()
                )
                // [변경사항 1]: FirebaseTokenFilter를 UsernamePasswordAuthenticationFilter 이전에 추가
                .addFilterBefore(new FirebaseTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // 다른 필요한 Bean들 (PasswordEncoder 등)은 여기에 유지하거나 추가
    // ...
}