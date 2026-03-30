package com.semotone.semotone.config;


import com.semotone.semotone.filter.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                // REST API이므로 CSRF 보안 로직은 끕니다.
                .csrf(AbstractHttpConfigurer::disable)

                // JWT 토큰을 사용할 것이므로 서버 메모리에 세션(상태)을 저장하지 않도록 설정합니다.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // API URL별로 접근 권한을 설정합니다.
                .authorizeHttpRequests(auth -> auth
                        // 일단 테스트를 위해 모든 API를 열어둡니다. (나중에 필요에 따라 "/api/public/**" 등으로 조절)
                        .anyRequest().permitAll()
                )
                // ★ 핵심: Spring의 기본 인증 필터가 작동하기 전에 우리가 만든 JwtFilter를 먼저 실행하게 합니다!
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}