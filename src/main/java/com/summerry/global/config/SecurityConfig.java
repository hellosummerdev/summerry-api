package com.summerry.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // CSRF 비활성화
            .authorizeHttpRequests(auth -> auth
                    .anyRequest().permitAll() // 모든 요청 허용
            )
            .formLogin(form -> form.disable()) // 로그인 폼 사용 안 함
            .httpBasic(basic -> basic.disable()); // HTTP 기본 인증 비활성화
        return http.build();
    }
}
